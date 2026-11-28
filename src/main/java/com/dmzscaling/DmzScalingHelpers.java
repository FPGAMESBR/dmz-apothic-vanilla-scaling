package com.dmzscaling;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Pure, immutable-friendly helpers for Apotheosis boss/miniboss filtering, empty-dimension protection,
 * party aggregation, 2-stage DMZ Defense-aware scaling, DoT soft caps, and boss regen scaling.
 */
public final class DmzScalingHelpers {

    public static final String TWILIGHT_FOREST_DIM = "twilightforest:twilight_forest";

    private static volatile Map<String, Integer> lastLoadedBossCountsByDimension = Collections.emptyMap();
    private static volatile int lastLoadedBossTotal = 0;

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

    public static Map<String, Integer> getLastLoadedBossCountsByDimension() {
        return lastLoadedBossCountsByDimension;
    }

    public static int getLastLoadedBossTotal() {
        return lastLoadedBossTotal;
    }

    // -------------------------------------------------------------------------
    // 1. Apotheosis BossRegistry & MinibossRegistry JSON Filters
    // -------------------------------------------------------------------------

    public static void filterBossRegistryMap(Map<ResourceLocation, JsonElement> map) {
        filterBossRegistryMap(
                map,
                DmzScalingConfig.isRestrictBossesToDmz(),
                DmzScalingConfig.isKeepTwilightForestBosses(),
                DmzScalingConfig.isPreserveDimensionIfEmpty(),
                DmzScalingConfig.isEnableExtraDmzOverworldBosses(),
                DmzScalingConfig.isEnableExtraDmzNamekBosses(),
                DmzScalingConfig.isEnableExtraDmzNetherAndDemonBosses(),
                DmzScalingConfig.isEnableExtraDmzEndAndOtherworldBosses(),
                DmzScalingConfig.getAllowedBossNamespacesSet(),
                DmzScalingConfig.getPreservedBossDimensionsSet()
        );
    }

    public static void filterBossRegistryMap(
            Map<ResourceLocation, JsonElement> map,
            boolean restrictToDmz,
            boolean keepPreservedDims,
            boolean preserveDimIfEmpty,
            boolean enableExtraOverworld,
            boolean enableExtraNamek,
            boolean enableExtraNetherDemon,
            boolean enableExtraEndOtherworld,
            Set<String> allowedNamespaces,
            Set<String> preservedDimensions
    ) {
        if (map == null || map.isEmpty()) {
            return;
        }

        // Snapshot original non-bundled entries per dimension for empty-dimension fallback protection
        Map<String, Map<ResourceLocation, JsonElement>> originalByDim = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation key = entry.getKey();
            JsonElement elem = entry.getValue();
            if (key != null && isBundledExtraDisabled(
                    key.m_135815_(),
                    enableExtraOverworld,
                    enableExtraNamek,
                    enableExtraNetherDemon,
                    enableExtraEndOtherworld
            )) {
                continue;
            }
            if (elem != null && elem.isJsonObject()) {
                for (String dim : extractDimensionList(elem.getAsJsonObject())) {
                    originalByDim.computeIfAbsent(dim, k -> new LinkedHashMap<>()).put(key, elem);
                }
            }
        }

        int keptDmz = 0;
        int keptPreserved = 0;
        int removed = 0;

        Iterator<Map.Entry<ResourceLocation, JsonElement>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ResourceLocation, JsonElement> entry = it.next();
            ResourceLocation key = entry.getKey();
            JsonElement elem = entry.getValue();

            if (key != null && isBundledExtraDisabled(
                    key.m_135815_(),
                    enableExtraOverworld,
                    enableExtraNamek,
                    enableExtraNetherDemon,
                    enableExtraEndOtherworld
            )) {
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
            List<String> dims = extractDimensionList(obj);
            boolean hasPreservedDim = hasAnyPreservedDimension(dims, preservedDimensions);

            Decision decision = classifyApotheosisBoss(
                    entityId,
                    hasPreservedDim,
                    restrictToDmz,
                    keepPreservedDims,
                    allowedNamespaces
            );
            if (decision == Decision.KEEP_DMZ) {
                keptDmz++;
            } else if (decision == Decision.KEEP_TWILIGHT) {
                keptPreserved++;
            } else {
                it.remove();
                removed++;
            }
        }

        // Empty-dimension safety fallback: never leave a dimension with 0 bosses if it had bosses before
        int restoredFallback = 0;
        if (restrictToDmz && preserveDimIfEmpty && !originalByDim.isEmpty()) {
            Set<String> survivingDims = new HashSet<>();
            for (JsonElement elem : map.values()) {
                if (elem != null && elem.isJsonObject()) {
                    survivingDims.addAll(extractDimensionList(elem.getAsJsonObject()));
                }
            }
            for (Map.Entry<String, Map<ResourceLocation, JsonElement>> dimEntry : originalByDim.entrySet()) {
                String dim = dimEntry.getKey();
                if (!survivingDims.contains(dim)) {
                    for (Map.Entry<ResourceLocation, JsonElement> restore : dimEntry.getValue().entrySet()) {
                        if (!map.containsKey(restore.getKey())) {
                            map.put(restore.getKey(), restore.getValue());
                            restoredFallback++;
                            removed = Math.max(0, removed - 1);
                        }
                    }
                }
            }
        }

        // Record dimension summary for /dmzscaling bosses command
        Map<String, Integer> dimCounts = new LinkedHashMap<>();
        for (JsonElement elem : map.values()) {
            if (elem != null && elem.isJsonObject()) {
                for (String d : extractDimensionList(elem.getAsJsonObject())) {
                    dimCounts.put(d, dimCounts.getOrDefault(d, 0) + 1);
                }
            }
        }
        lastLoadedBossCountsByDimension = Collections.unmodifiableMap(dimCounts);
        lastLoadedBossTotal = map.size();

        DmzScalingMod.LOGGER.info(
                "[DMZ Scaling] Filtered Apotheosis BossRegistry: kept {} DMZ bosses, {} preserved-dimension bosses, restored {} empty-dimension fallback entries, removed {} non-DMZ bosses.",
                keptDmz, keptPreserved, restoredFallback, removed
        );
    }

    public static boolean isBundledExtraDisabled(
            String resourcePath,
            boolean enableOverworld,
            boolean enableNamek,
            boolean enableNetherDemon,
            boolean enableEndOtherworld
    ) {
        if (resourcePath == null || !resourcePath.startsWith("dmz_extra/")) {
            return false;
        }
        String file = resourcePath.substring("dmz_extra/".length());
        if (file.startsWith("namek_")) {
            return !enableNamek;
        }
        if (file.startsWith("nether_")) {
            return !enableNetherDemon;
        }
        if (file.startsWith("end_")) {
            return !enableEndOtherworld;
        }
        return !enableOverworld;
    }

    public static void filterMinibossRegistryMap(Map<ResourceLocation, JsonElement> map) {
        if (map == null || map.isEmpty() || !DmzScalingConfig.isDisableVanillaMinibosses()) {
            return;
        }
        boolean keepPreservedDims = DmzScalingConfig.isKeepTwilightForestBosses();
        Set<String> allowedNamespaces = DmzScalingConfig.getAllowedBossNamespacesSet();
        Set<String> preservedDims = DmzScalingConfig.getPreservedBossDimensionsSet();

        int removed = 0;
        Iterator<Map.Entry<ResourceLocation, JsonElement>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ResourceLocation, JsonElement> entry = it.next();
            JsonElement elem = entry.getValue();
            if (elem == null || !elem.isJsonObject()) {
                continue;
            }
            JsonObject obj = elem.getAsJsonObject();
            List<String> dims = extractDimensionList(obj);
            if (keepPreservedDims && hasAnyPreservedDimension(dims, preservedDims)) {
                continue;
            }
            if (hasAllowedMinibossEntity(obj, allowedNamespaces)) {
                continue;
            }
            it.remove();
            removed++;
        }
        if (removed > 0) {
            DmzScalingMod.LOGGER.info("[DMZ Scaling] Filtered {} non-DMZ entries from Apotheosis MinibossRegistry.", removed);
        }
    }

    private static boolean hasAllowedMinibossEntity(JsonObject obj, Set<String> allowedNamespaces) {
        if (obj.has("entities") && obj.get("entities").isJsonArray()) {
            for (JsonElement e : obj.getAsJsonArray("entities")) {
                if (e != null && e.isJsonPrimitive() && isAllowedBossEntity(e.getAsString(), allowedNamespaces)) {
                    return true;
                }
            }
        }
        if (obj.has("entity") && obj.get("entity").isJsonPrimitive()) {
            return isAllowedBossEntity(obj.get("entity").getAsString(), allowedNamespaces);
        }
        return false;
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
        return classifyApotheosisBoss(
                entityId,
                hasTwilightDimension,
                restrictToDmz,
                keepTwilight,
                DmzScalingConfig.getAllowedBossNamespacesSet()
        );
    }

    public static Decision classifyApotheosisBoss(
            String entityId,
            boolean hasPreservedDimension,
            boolean restrictToDmz,
            boolean keepPreservedDims,
            Set<String> allowedNamespaces
    ) {
        if (isAllowedBossEntity(entityId, allowedNamespaces)) {
            return Decision.KEEP_DMZ;
        }
        if (!restrictToDmz) {
            return Decision.KEEP_DMZ;
        }
        if (keepPreservedDims && hasPreservedDimension) {
            return Decision.KEEP_TWILIGHT;
        }
        return Decision.REMOVE;
    }

    public static boolean isAllowedBossEntity(String entityId, Set<String> allowedNamespaces) {
        if (entityId == null || entityId.isEmpty()) {
            return false;
        }
        int colon = entityId.indexOf(':');
        if (colon <= 0) {
            return false;
        }
        String ns = entityId.substring(0, colon).toLowerCase(Locale.ROOT);
        if (allowedNamespaces == null || allowedNamespaces.isEmpty()) {
            return "dragonminez".equals(ns) || "dmzultra".equals(ns) || "dmzrevamp".equals(ns);
        }
        return allowedNamespaces.contains(ns);
    }

    public static List<String> extractDimensionList(JsonObject obj) {
        if (obj == null || !obj.has("dimensions") || !obj.get("dimensions").isJsonArray()) {
            return Collections.emptyList();
        }
        JsonArray arr = obj.getAsJsonArray("dimensions");
        List<String> dims = new ArrayList<>(arr.size());
        for (JsonElement e : arr) {
            if (e != null && e.isJsonPrimitive()) {
                dims.add(e.getAsString().toLowerCase(Locale.ROOT));
            }
        }
        return dims;
    }

    private static boolean hasAnyPreservedDimension(List<String> dims, Set<String> preservedDimensions) {
        if (dims == null || dims.isEmpty()) {
            return false;
        }
        Set<String> target = (preservedDimensions != null && !preservedDimensions.isEmpty())
                ? preservedDimensions
                : Set.of(TWILIGHT_FOREST_DIM);
        for (String d : dims) {
            if (target.contains(d.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldBlockApotheosisMiniboss(String entityId, String dimensionId) {
        if (!DmzScalingConfig.isDisableVanillaMinibosses()) {
            return false;
        }
        if (isAllowedBossEntity(entityId, DmzScalingConfig.getAllowedBossNamespacesSet())) {
            return false;
        }
        if (DmzScalingConfig.isKeepTwilightForestBosses() && dimensionId != null) {
            Set<String> preserved = DmzScalingConfig.getPreservedBossDimensionsSet();
            if (preserved.contains(dimensionId.toLowerCase(Locale.ROOT))) {
                return false;
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // 2. Entity Tier Classification & Party Aggregation
    // -------------------------------------------------------------------------

    public static ScalingTier classifyEntityId(
            String entityId,
            boolean isEnemyInstance
    ) {
        if (entityId == null || entityId.isEmpty()) {
            return ScalingTier.NONE;
        }
        if (DmzScalingConfig.isBlacklistedEntity(entityId)) {
            return ScalingTier.NONE;
        }
        if (DmzScalingConfig.isBossScalingEnabled() && DmzScalingConfig.isBossEntityId(entityId)) {
            return ScalingTier.VANILLA_BOSS;
        }
        if (DmzScalingConfig.isMinibossScalingEnabled() && DmzScalingConfig.isMinibossEntityId(entityId)) {
            return ScalingTier.VANILLA_MINIBOSS;
        }
        int colon = entityId.indexOf(':');
        String ns = (colon > 0) ? entityId.substring(0, colon) : "minecraft";
        if (DmzScalingConfig.isMobScalingEnabled() && isEnemyInstance && DmzScalingConfig.isAllowedMobNamespace(ns)) {
            return ScalingTier.VANILLA_MOB;
        }
        return ScalingTier.NONE;
    }

    /**
     * Combines nearby players' DMZ Base Power according to partyScalingMode (STRONGEST, AVERAGE, NEAREST).
     * The first element in partyPowers is assumed to be the nearest player.
     */
    public static PlayerBasePower aggregatePartyPower(List<PlayerBasePower> partyPowers, String mode) {
        if (partyPowers == null || partyPowers.isEmpty()) {
            return PlayerBasePower.VANILLA_DEFAULT;
        }
        if (partyPowers.size() == 1) {
            return partyPowers.get(0);
        }
        String norm = (mode != null) ? mode.trim().toUpperCase(Locale.ROOT) : "STRONGEST";
        if ("NEAREST".equals(norm)) {
            return partyPowers.get(0);
        }
        if ("AVERAGE".equals(norm)) {
            long sumLvl = 0;
            double sumHp = 0.0;
            double sumOff = 0.0;
            for (PlayerBasePower p : partyPowers) {
                sumLvl += Math.max(1, p.dmzLevel());
                sumHp += Math.max(20.0, p.baseMaxHp());
                sumOff += Math.max(4.0, p.baseOffense());
            }
            int n = partyPowers.size();
            return new PlayerBasePower((int) Math.round((double) sumLvl / n), sumHp / n, sumOff / n);
        }
        // Default: STRONGEST
        PlayerBasePower best = partyPowers.get(0);
        double bestScore = best.baseOffense() * 100.0 + best.baseMaxHp() + best.dmzLevel();
        for (int i = 1; i < partyPowers.size(); i++) {
            PlayerBasePower cand = partyPowers.get(i);
            double score = cand.baseOffense() * 100.0 + cand.baseMaxHp() + cand.dmzLevel();
            if (score > bestScore) {
                bestScore = score;
                best = cand;
            }
        }
        return best;
    }

    public static ComputedScaling applyPartyHpBonus(
            ComputedScaling base,
            int nearbyPartyCount,
            double bonusHpPerExtraPlayer,
            double maxPartyHpMultiplier,
            double tierMaxHpMultiplier
    ) {
        if (base == null || nearbyPartyCount <= 1 || bonusHpPerExtraPlayer <= 0.0) {
            return base;
        }
        double partyFactor = clamp(1.0 + (nearbyPartyCount - 1) * bonusHpPerExtraPlayer, 1.0, Math.max(1.0, maxPartyHpMultiplier));
        double boostedHpMult = clamp(base.hpMultiplier() * partyFactor, base.hpMultiplier(), Math.max(base.hpMultiplier(), tierMaxHpMultiplier * partyFactor));
        return new ComputedScaling(
                base.tier(),
                boostedHpMult,
                base.damageMultiplier(),
                base.bonusArmor(),
                base.bonusToughness()
        );
    }

    // -------------------------------------------------------------------------
    // 3. Hybrid Vanilla Mob & Boss Scaling Math
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
     * Stage 1 (LivingHurtEvent @ HIGHEST — BEFORE DragonMineZ DEF/Blocking/Parry):
     * Scales raw outgoing damage when vanilla did not already apply generic.attack_damage.
     */
    public static float computePreDefenseScaledDamage(
            float rawDamage,
            double damageMultiplier,
            boolean alreadyScaledByAttackAttribute
    ) {
        if (rawDamage <= 0.0f || alreadyScaledByAttackAttribute) {
            return rawDamage;
        }
        return (float) (rawDamage * Math.max(1.0, damageMultiplier));
    }

    /**
     * Stage 2 (LivingDamageEvent @ HIGH — AFTER DragonMineZ DEF & Armor):
     * Enforces the per-hit or DoT soft cap as a fraction of the victim player's Max HP,
     * while never nerfing damage below the unscaled vanilla rawDamage floor.
     */
    public static float applyPostDefenseSoftCap(
            float postDefenseDamage,
            float unscaledFloor,
            float victimMaxHp,
            double maxHitPercentOfVictimHp
    ) {
        if (postDefenseDamage <= 0.0f || maxHitPercentOfVictimHp <= 0.0 || victimMaxHp <= 0.0f) {
            return postDefenseDamage;
        }
        double softCap = victimMaxHp * maxHitPercentOfVictimHp;
        if (postDefenseDamage > softCap) {
            return (float) Math.max(Math.min(postDefenseDamage, unscaledFloor), softCap);
        }
        return postDefenseDamage;
    }

    /**
     * Combined helper for single-step unit evaluation & backward compatibility.
     */
    public static float computeScaledHitDamage(
            float rawDamage,
            double damageMultiplier,
            boolean alreadyScaledByAttackAttribute,
            float victimMaxHp,
            double maxHitPercentOfVictimHp
    ) {
        float preDef = computePreDefenseScaledDamage(rawDamage, damageMultiplier, alreadyScaledByAttackAttribute);
        float unscaledFloor = alreadyScaledByAttackAttribute
                ? (float) (rawDamage / Math.max(1.0, damageMultiplier))
                : rawDamage;
        return applyPostDefenseSoftCap(preDef, unscaledFloor, victimMaxHp, maxHitPercentOfVictimHp);
    }

    public static boolean isContinuousDotDamage(DamageSource source, Entity directEntity) {
        if (directEntity instanceof AreaEffectCloud) {
            return true;
        }
        if (source == null) {
            return false;
        }
        return isContinuousDotMsgId(source.m_19385_());
    }

    public static boolean isContinuousDotMsgId(String msgId) {
        if (msgId == null || msgId.isEmpty()) {
            return false;
        }
        return "dragonBreath".equalsIgnoreCase(msgId)
                || "dragon_breath".equalsIgnoreCase(msgId)
                || "wither".equalsIgnoreCase(msgId)
                || "onFire".equalsIgnoreCase(msgId)
                || "inFire".equalsIgnoreCase(msgId)
                || "lava".equalsIgnoreCase(msgId)
                || "hotFloor".equalsIgnoreCase(msgId)
                || "cramming".equalsIgnoreCase(msgId);
    }

    /**
     * Computes the bonus End Crystal heal amount applied every 10 ticks (0.5s) on top of vanilla's +1.0F heal.
     */
    public static float computeBonusCrystalHealPer10Ticks(
            double hpMultiplier,
            float dragonMaxHp,
            double scaleFactor,
            double maxPercentPerSecond
    ) {
        if (hpMultiplier <= 1.01 || dragonMaxHp <= 0.0f || scaleFactor <= 0.0) {
            return 0.0f;
        }
        // Vanilla heals 1.0 HP every 10 ticks (2.0 HP/s). Bonus per 10 ticks:
        double desiredBonus = (hpMultiplier - 1.0) * 1.0 * scaleFactor;
        // Max total per 10 ticks = 0.5 * maxPercentPerSecond * dragonMaxHp
        double maxTotalPer10Ticks = Math.max(1.0, dragonMaxHp * Math.max(0.0005, maxPercentPerSecond) * 0.5);
        double cappedBonus = Math.max(0.0, Math.min(desiredBonus, maxTotalPer10Ticks - 1.0));
        return (float) cappedBonus;
    }

    /**
     * Computes the scaled Wither passive heal amount (triggered when vanilla heals 1.0F every 20 ticks = 1s).
     */
    public static float computeScaledWitherPassiveHeal(
            float vanillaHealAmount,
            double hpMultiplier,
            float witherMaxHp,
            double scaleFactor,
            double maxPercentPerSecond
    ) {
        if (vanillaHealAmount <= 0.0f || hpMultiplier <= 1.01 || witherMaxHp <= 0.0f || scaleFactor <= 0.0) {
            return vanillaHealAmount;
        }
        double desired = vanillaHealAmount * (1.0 + (hpMultiplier - 1.0) * scaleFactor);
        double maxPerSec = Math.max(vanillaHealAmount, witherMaxHp * Math.max(0.0005, maxPercentPerSecond));
        return (float) Math.min(desired, maxPerSec);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
