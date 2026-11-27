package com.dmzscaling;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.Iterator;
import java.util.Map;

/**
 * Pure, immutable-friendly helpers for Apotheosis boss filtering and hybrid DMZ scaling math.
 */
public final class DmzScalingHelpers {

    public static final String TWILIGHT_FOREST_DIM = "twilightforest:twilight_forest";

    public enum ScalingTier {
        NONE,
        VANILLA_MOB,
        VANILLA_MINIBOSS,
        VANILLA_BOSS
    }

    public record PlayerBasePower(int dmzLevel, double baseMaxHp, double baseOffense) {
        public static final PlayerBasePower VANILLA_DEFAULT = new PlayerBasePower(1, 20.0, 4.0);
    }

    public record ComputedScaling(
            ScalingTier tier,
            double hpMultiplier,
            double damageMultiplier,
            double bonusArmor,
            double bonusToughness
    ) {
    }

    private DmzScalingHelpers() {
    }

    // -------------------------------------------------------------------------
    // 1. Apotheosis BossRegistry JSON filter
    // -------------------------------------------------------------------------

    public static void filterBossRegistryMap(Map<ResourceLocation, JsonElement> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        boolean restrictToDmz = DmzScalingConfig.isRestrictBossesToDmz();
        boolean keepTwilight = DmzScalingConfig.isKeepTwilightForestBosses();
        boolean enableExtra = DmzScalingConfig.isEnableExtraDmzOverworldBosses();

        int keptDmz = 0;
        int keptTwilight = 0;
        int removed = 0;

        Iterator<Map.Entry<ResourceLocation, JsonElement>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ResourceLocation, JsonElement> entry = it.next();
            ResourceLocation key = entry.getKey();
            JsonElement elem = entry.getValue();

            if (!enableExtra && key != null && key.m_135815_().startsWith("dmz_extra/")) {
                it.remove();
                removed++;
                continue;
            }

            if (!restrictToDmz) {
                continue;
            }

            if (elem == null || !elem.isJsonObject()) {
                continue;
            }
            JsonObject obj = elem.getAsJsonObject();
            String entityId = obj.has("entity") && obj.get("entity").isJsonPrimitive()
                    ? obj.get("entity").getAsString()
                    : "";

            Decision decision = classifyApotheosisBoss(entityId, extractDimensions(obj), restrictToDmz, keepTwilight);
            if (decision == Decision.KEEP_DMZ) {
                keptDmz++;
            } else if (decision == Decision.KEEP_TWILIGHT) {
                keptTwilight++;
            } else {
                it.remove();
                removed++;
            }
        }

        DmzScalingMod.LOGGER.info(
                "[DMZ Scaling] Filtered Apotheosis BossRegistry: kept {} DMZ bosses, {} Twilight Forest bosses, removed {} non-DMZ bosses.",
                keptDmz, keptTwilight, removed
        );
    }

    public enum Decision {
        KEEP_DMZ,
        KEEP_TWILIGHT,
        REMOVE
    }

    public static Decision classifyApotheosisBoss(
            String entityId,
            boolean hasTwilightDimension,
            boolean restrictToDmz,
            boolean keepTwilight
    ) {
        if (entityId != null && entityId.startsWith("dragonminez:")) {
            return Decision.KEEP_DMZ;
        }
        if (!restrictToDmz) {
            return Decision.KEEP_DMZ;
        }
        if (keepTwilight && hasTwilightDimension) {
            return Decision.KEEP_TWILIGHT;
        }
        return Decision.REMOVE;
    }

    private static boolean extractDimensions(JsonObject obj) {
        if (!obj.has("dimensions") || !obj.get("dimensions").isJsonArray()) {
            return false;
        }
        JsonArray arr = obj.getAsJsonArray("dimensions");
        for (JsonElement e : arr) {
            if (e != null && e.isJsonPrimitive() && TWILIGHT_FOREST_DIM.equals(e.getAsString())) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldBlockApotheosisMiniboss(String entityId, String dimensionId) {
        if (!DmzScalingConfig.isDisableVanillaMinibosses()) {
            return false;
        }
        if (entityId != null && entityId.startsWith("dragonminez:")) {
            return false;
        }
        if (DmzScalingConfig.isKeepTwilightForestBosses() && TWILIGHT_FOREST_DIM.equals(dimensionId)) {
            return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // 2. Hybrid Vanilla Mob & Boss Scaling Math
    // -------------------------------------------------------------------------

    public static ComputedScaling computeVanillaMobScaling(PlayerBasePower power) {
        int lvl = Math.max(1, power.dmzLevel());
        double baseHp = DmzScalingConfig.getMobBaseHpMult();
        double maxHp = Math.max(baseHp, DmzScalingConfig.getMobMaxHpMult());
        double hpMult = clamp(baseHp + (lvl - 1) * DmzScalingConfig.getMobHpPerLevel(), baseHp, maxHp);

        double baseDmg = DmzScalingConfig.getMobBaseDmgMult();
        double maxDmg = Math.max(baseDmg, DmzScalingConfig.getMobMaxDmgMult());
        double dmgMult = clamp(baseDmg + (lvl - 1) * DmzScalingConfig.getMobDmgPerLevel(), baseDmg, maxDmg);

        double armorProgress = clamp((lvl - 1) / 250.0, 0.0, 1.0);
        double bonusArmor = DmzScalingConfig.getMobMaxBonusArmor() * armorProgress;

        return new ComputedScaling(ScalingTier.VANILLA_MOB, hpMult, dmgMult, bonusArmor, 0.0);
    }

    public static ComputedScaling computeVanillaMinibossScaling(double vanillaBaseHp, PlayerBasePower power) {
        double safeVanillaHp = Math.max(1.0, vanillaBaseHp);
        double minHpMult = DmzScalingConfig.getMinibossMinHpMult();
        double maxHpMult = Math.max(minHpMult, DmzScalingConfig.getMinibossMaxHpMult());
        double desiredHp = Math.max(
                safeVanillaHp * minHpMult,
                Math.max(4.0, power.baseOffense()) * DmzScalingConfig.getMinibossTargetBaseHits()
        );
        double hpMult = clamp(desiredHp / safeVanillaHp, minHpMult, maxHpMult);

        double minDmg = DmzScalingConfig.getMinibossMinDmgMult();
        double maxDmg = Math.max(minDmg, DmzScalingConfig.getMinibossMaxDmgMult());
        double hpRatio = Math.max(1.0, power.baseMaxHp() / 20.0);
        double dmgMult = clamp(minDmg + (Math.sqrt(hpRatio) - 1.0) * 1.15, minDmg, maxDmg);

        return new ComputedScaling(
                ScalingTier.VANILLA_MINIBOSS,
                hpMult,
                dmgMult,
                DmzScalingConfig.getMinibossBonusArmor(),
                DmzScalingConfig.getMinibossBonusToughness()
        );
    }

    public static ComputedScaling computeVanillaBossScaling(double vanillaBaseHp, PlayerBasePower power) {
        double safeVanillaHp = Math.max(1.0, vanillaBaseHp);
        double minHpMult = DmzScalingConfig.getBossMinHpMult();
        double maxHpMult = Math.max(minHpMult, DmzScalingConfig.getBossMaxHpMult());
        double desiredHp = Math.max(
                safeVanillaHp * minHpMult,
                Math.max(4.0, power.baseOffense()) * DmzScalingConfig.getBossTargetBaseHits()
        );
        double hpMult = clamp(desiredHp / safeVanillaHp, minHpMult, maxHpMult);

        double minDmg = DmzScalingConfig.getBossMinDmgMult();
        double maxDmg = Math.max(minDmg, DmzScalingConfig.getBossMaxDmgMult());
        double hpRatio = Math.max(1.0, power.baseMaxHp() / 20.0);
        // Scale smoothly with player's DMZ Max HP so Ender Dragon & Wither stay genuinely threatening
        double dmgMult = clamp(minDmg + (Math.pow(hpRatio, 0.62) - 1.0) * 1.35, minDmg, maxDmg);

        return new ComputedScaling(
                ScalingTier.VANILLA_BOSS,
                hpMult,
                dmgMult,
                DmzScalingConfig.getBossBonusArmor(),
                DmzScalingConfig.getBossBonusToughness()
        );
    }

    /**
     * Scales outgoing boss/miniboss/mob hit damage and applies an optional per-hit soft cap
     * as a fraction of the victim player's Max HP (preventing unfair 1-shots).
     */
    public static float computeScaledHitDamage(
            float rawDamage,
            double damageMultiplier,
            boolean alreadyScaledByAttackAttribute,
            float victimMaxHp,
            double maxHitPercentOfVictimHp
    ) {
        if (rawDamage <= 0.0f) {
            return rawDamage;
        }
        double effective = alreadyScaledByAttackAttribute ? rawDamage : (rawDamage * Math.max(1.0, damageMultiplier));
        if (maxHitPercentOfVictimHp > 0.0 && victimMaxHp > 0.0f) {
            double softCap = victimMaxHp * maxHitPercentOfVictimHp;
            if (effective > softCap) {
                // Keep at least rawDamage so vanilla damage is never nerfed below unscaled value
                effective = Math.max(rawDamage, softCap);
            }
        }
        return (float) effective;
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
