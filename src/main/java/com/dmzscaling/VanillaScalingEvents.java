package com.dmzscaling;

import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsData;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.UUID;

/**
 * Handles hybrid scaling for vanilla Minecraft hostile mobs, mini-bosses, and main bosses
 * (Ender Dragon, Wither, Warden, Elder Guardian, etc.) using Dragon Mine Z BASE player power.
 */
public final class VanillaScalingEvents {

    private static final String TAG_SCALED = "dmzscaling.scaled";
    private static final String TAG_TIER = "dmzscaling.tier";
    private static final String TAG_DMG_MULT = "dmzscaling.dmg_mult";
    private static final String TAG_WITHER_TOPPED = "dmzscaling.wither_topped";
    private static final String APOTH_BOSS_TAG = "apoth.boss";

    private static final UUID HP_MOD_ID = UUID.fromString("91d4a7c2-4f10-4b28-9c12-1048a0c10001");
    private static final UUID DMG_MOD_ID = UUID.fromString("91d4a7c2-4f10-4b28-9c12-1048a0c10002");
    private static final UUID ARMOR_MOD_ID = UUID.fromString("91d4a7c2-4f10-4b28-9c12-1048a0c10003");
    private static final UUID TOUGH_MOD_ID = UUID.fromString("91d4a7c2-4f10-4b28-9c12-1048a0c10004");

    private VanillaScalingEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().m_5776_() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Entity raw = event.getEntity();
        if (!(raw instanceof LivingEntity living) || raw instanceof Player) {
            return;
        }
        CompoundTag pdata = living.getPersistentData();
        // Never double-scale on chunk reload and never interfere with Apotheosis bosses
        if (pdata.m_128471_(TAG_SCALED) || pdata.m_128471_(APOTH_BOSS_TAG)) {
            return;
        }

        ResourceLocation entityId = EntityType.m_20613_(living.m_6095_());
        if (entityId == null) {
            return;
        }
        String idStr = entityId.toString();
        DmzScalingHelpers.ScalingTier tier = classifyEntity(living, entityId, idStr);
        if (tier == DmzScalingHelpers.ScalingTier.NONE) {
            return;
        }

        double scanRange = (tier == DmzScalingHelpers.ScalingTier.VANILLA_MOB)
                ? DmzScalingConfig.getMobScanRange()
                : DmzScalingConfig.getBossScanRange();
        boolean fallbackAnyInDimension = (tier != DmzScalingHelpers.ScalingTier.VANILLA_MOB);

        DmzScalingHelpers.PlayerBasePower power = sampleNearestPlayerBasePower(level, living, scanRange, fallbackAnyInDimension);

        AttributeInstance maxHpAttr = living.m_21051_(Attributes.f_22276_);
        double vanillaHp = (maxHpAttr != null) ? maxHpAttr.m_22115_() : 20.0;

        DmzScalingHelpers.ComputedScaling scaling = switch (tier) {
            case VANILLA_BOSS -> DmzScalingHelpers.computeVanillaBossScaling(vanillaHp, power);
            case VANILLA_MINIBOSS -> DmzScalingHelpers.computeVanillaMinibossScaling(vanillaHp, power);
            case VANILLA_MOB -> DmzScalingHelpers.computeVanillaMobScaling(power);
            default -> null;
        };
        if (scaling == null) {
            return;
        }

        // 1. Scale Max Health (MULTIPLY_TOTAL avoids conflicting with additive modifiers; base * (mult - 1))
        if (maxHpAttr != null && scaling.hpMultiplier() > 1.001) {
            replacePermanentModifier(maxHpAttr, HP_MOD_ID, "dmzscaling_hp", scaling.hpMultiplier() - 1.0, AttributeModifier.Operation.MULTIPLY_BASE);
            // Set health cleanly: keep Wither's iconic 1/3 spawn charge bar, top off when invuln finishes!
            float newMax = living.m_21233_();
            if (living instanceof WitherBoss wither && wither.m_31502_() > 0) {
                living.m_21153_(Math.max(1.0f, newMax / 3.0f));
            } else {
                living.m_21153_(newMax);
            }
        }

        // 2. Scale generic Attack Damage attribute for melee mobs/minibosses (EnderDragon & Wither deal custom damage in LivingHurtEvent)
        AttributeInstance atkAttr = living.m_21051_(Attributes.f_22281_);
        if (atkAttr != null && scaling.damageMultiplier() > 1.001) {
            replacePermanentModifier(atkAttr, DMG_MOD_ID, "dmzscaling_dmg", scaling.damageMultiplier() - 1.0, AttributeModifier.Operation.MULTIPLY_BASE);
        }

        // 3. Bonus Armor & Armor Toughness
        if (scaling.bonusArmor() > 0.01) {
            AttributeInstance armorAttr = living.m_21051_(Attributes.f_22284_);
            if (armorAttr != null) {
                replacePermanentModifier(armorAttr, ARMOR_MOD_ID, "dmzscaling_armor", scaling.bonusArmor(), AttributeModifier.Operation.ADDITION);
            }
        }
        if (scaling.bonusToughness() > 0.01) {
            AttributeInstance toughAttr = living.m_21051_(Attributes.f_22285_);
            if (toughAttr != null) {
                replacePermanentModifier(toughAttr, TOUGH_MOD_ID, "dmzscaling_toughness", scaling.bonusToughness(), AttributeModifier.Operation.ADDITION);
            }
        }

        pdata.m_128379_(TAG_SCALED, true);
        pdata.m_128359_(TAG_TIER, tier.name());
        pdata.m_128347_(TAG_DMG_MULT, scaling.damageMultiplier());
    }

    /**
     * Ensures WitherBoss enters combat at 100% of its scaled Max HP right when its spawn invulnerability finishes.
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();
        if (!(living instanceof WitherBoss wither) || living.m_9236_().m_5776_()) {
            return;
        }
        CompoundTag pdata = wither.getPersistentData();
        if (!pdata.m_128471_(TAG_SCALED) || pdata.m_128471_(TAG_WITHER_TOPPED)) {
            return;
        }
        int invul = wither.m_31502_();
        if (invul <= 1) {
            wither.m_21153_(wither.m_21233_());
            pdata.m_128379_(TAG_WITHER_TOPPED, true);
        }
    }

    /**
     * Scales special/ranged/boss attacks that ignore generic.attack_damage in vanilla Minecraft
     * (Ender Dragon charge/breath/fireball, Wither skulls/explosion, Warden sonic boom, Guardian beams,
     * Skeleton arrows, Blaze fireballs, Creeper explosions) and enforces the per-hit soft cap.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.m_9236_().m_5776_()) {
            return;
        }
        DamageSource source = event.getSource();
        if (source == null) {
            return;
        }
        Entity attacker = source.m_7639_();
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }
        CompoundTag pdata = livingAttacker.getPersistentData();
        if (!pdata.m_128471_(TAG_SCALED)) {
            return;
        }
        double dmgMult = pdata.m_128459_(TAG_DMG_MULT);
        if (dmgMult <= 1.001) {
            return;
        }
        String tierName = pdata.m_128461_(TAG_TIER);
        Entity direct = source.m_7640_();

        // Determine if vanilla already applied generic.attack_damage to this hit:
        // - EnderDragon & Wither NEVER read generic.attack_damage (all hits are hardcoded constants)
        // - Projectiles / AreaEffectCloud / Explosions / Sonic Boom have direct != attacker or non-mob_attack
        boolean isBossWithHardcodedDamage = (livingAttacker instanceof EnderDragon || livingAttacker instanceof WitherBoss);
        boolean isDirectMelee = (direct == livingAttacker) && !isBossWithHardcodedDamage && isMeleeDamageType(source);
        boolean alreadyScaled = isDirectMelee && livingAttacker.m_21051_(Attributes.f_22281_) != null;

        double maxHitPct = 0.0;
        if (victim instanceof Player) {
            if (DmzScalingHelpers.ScalingTier.VANILLA_BOSS.name().equals(tierName)) {
                maxHitPct = DmzScalingConfig.getBossMaxHitPct();
            } else if (DmzScalingHelpers.ScalingTier.VANILLA_MINIBOSS.name().equals(tierName)) {
                maxHitPct = DmzScalingConfig.getMinibossMaxHitPct();
            }
        }

        float adjusted = DmzScalingHelpers.computeScaledHitDamage(
                event.getAmount(),
                dmgMult,
                alreadyScaled,
                victim.m_21233_(),
                maxHitPct
        );
        if (adjusted != event.getAmount()) {
            event.setAmount(adjusted);
        }
    }

    private static boolean isMeleeDamageType(DamageSource source) {
        String msgId = source.m_19385_();
        return "mob".equals(msgId) || "sting".equals(msgId);
    }

    private static DmzScalingHelpers.ScalingTier classifyEntity(LivingEntity living, ResourceLocation entityId, String idStr) {
        if (DmzScalingConfig.isBossScalingEnabled() && DmzScalingConfig.getVanillaBossIds().contains(idStr)) {
            return DmzScalingHelpers.ScalingTier.VANILLA_BOSS;
        }
        if (DmzScalingConfig.isMinibossScalingEnabled() && DmzScalingConfig.getVanillaMinibossIds().contains(idStr)) {
            return DmzScalingHelpers.ScalingTier.VANILLA_MINIBOSS;
        }
        if (DmzScalingConfig.isMobScalingEnabled()
                && "minecraft".equals(entityId.m_135827_())
                && living instanceof Enemy) {
            return DmzScalingHelpers.ScalingTier.VANILLA_MOB;
        }
        return DmzScalingHelpers.ScalingTier.NONE;
    }

    private static DmzScalingHelpers.PlayerBasePower sampleNearestPlayerBasePower(
            ServerLevel level,
            LivingEntity entity,
            double maxRange,
            boolean fallbackAnyInDimension
    ) {
        List<ServerPlayer> players = level.m_6907_();
        if (players.isEmpty()) {
            return DmzScalingHelpers.PlayerBasePower.VANILLA_DEFAULT;
        }
        double maxDistSq = maxRange * maxRange;
        ServerPlayer nearest = null;
        double bestDistSq = Double.MAX_VALUE;
        ServerPlayer strongestFallback = null;
        int strongestLvl = -1;

        for (ServerPlayer sp : players) {
            if (!sp.m_6084_() || sp.m_5833_()) {
                continue;
            }
            double distSq = sp.m_20280_(entity);
            if (distSq <= maxDistSq && distSq < bestDistSq) {
                bestDistSq = distSq;
                nearest = sp;
            }
            if (fallbackAnyInDimension) {
                DmzScalingHelpers.PlayerBasePower p = readDmzBasePower(sp);
                if (p.dmzLevel() > strongestLvl) {
                    strongestLvl = p.dmzLevel();
                    strongestFallback = sp;
                }
            }
        }

        ServerPlayer chosen = (nearest != null) ? nearest : strongestFallback;
        return (chosen != null) ? readDmzBasePower(chosen) : DmzScalingHelpers.PlayerBasePower.VANILLA_DEFAULT;
    }

    private static DmzScalingHelpers.PlayerBasePower readDmzBasePower(ServerPlayer player) {
        try {
            StatsData data = StatsProvider.get(StatsCapability.INSTANCE, player).orElse(null);
            if (data == null || !data.isDataLoaded()) {
                return DmzScalingHelpers.PlayerBasePower.VANILLA_DEFAULT;
            }
            int dmzLevel = Math.max(1, data.getLevel());
            double baseMaxHp = Math.max(20.0, data.getMaxHealth());
            double melee = data.getMeleeDamageNoMultipliers();
            double strike = data.getStrikeDamageNoForms();
            double ki = data.getKiDamageNoForms();
            double baseOffense = Math.max(4.0, Math.max(melee, Math.max(strike, ki)));
            return new DmzScalingHelpers.PlayerBasePower(dmzLevel, baseMaxHp, baseOffense);
        } catch (Throwable ignored) {
            return DmzScalingHelpers.PlayerBasePower.VANILLA_DEFAULT;
        }
    }

    private static void replacePermanentModifier(
            AttributeInstance inst,
            UUID uuid,
            String name,
            double amount,
            AttributeModifier.Operation op
    ) {
        if (inst.m_22111_(uuid) != null) {
            inst.m_22120_(uuid);
        }
        inst.m_22125_(new AttributeModifier(uuid, name, amount, op));
    }
}
