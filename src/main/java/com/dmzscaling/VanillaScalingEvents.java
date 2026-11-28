package com.dmzscaling;

import com.dragonminez.common.stats.StatsCapability;
import com.dragonminez.common.stats.StatsData;
import com.dragonminez.common.stats.StatsProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles 2-stage DMZ Defense-aware scaling, lazy combat rescaling, party co-op HP scaling,
 * End Crystal / Wither passive regeneration scaling, and DoT soft caps.
 */
public final class VanillaScalingEvents {

    public static final String TAG_SCALED = "dmzscaling.scaled";
    public static final String TAG_NEEDS_RESCALE = "dmzscaling.needs_rescale";
    public static final String TAG_COMBAT_LOCKED = "dmzscaling.combat_locked";
    public static final String TAG_ANNOUNCED = "dmzscaling.announced";
    public static final String TAG_TIER = "dmzscaling.tier";
    public static final String TAG_HP_MULT = "dmzscaling.hp_mult";
    public static final String TAG_DMG_MULT = "dmzscaling.dmg_mult";
    public static final String TAG_SAMPLE_LVL = "dmzscaling.sample_lvl";
    public static final String TAG_PARTY_SIZE = "dmzscaling.party_size";
    public static final String TAG_WITHER_TOPPED = "dmzscaling.wither_topped";
    public static final String TAG_LAST_RAW_DMG = "dmzscaling.last_raw_dmg";
    private static final String APOTH_BOSS_TAG = "apoth.boss";

    private static final UUID HP_MOD_ID = UUID.fromString("91d4a7c2-4f10-4b28-9c12-1048a0c10001");
    private static final UUID DMG_MOD_ID = UUID.fromString("91d4a7c2-4f10-4b28-9c12-1048a0c10002");
    private static final UUID ARMOR_MOD_ID = UUID.fromString("91d4a7c2-4f10-4b28-9c12-1048a0c10003");
    private static final UUID TOUGH_MOD_ID = UUID.fromString("91d4a7c2-4f10-4b28-9c12-1048a0c10004");

    // Fast O(1) active boss/miniboss tracker so onLivingTick has zero overhead on normal mobs
    private static final Set<UUID> TRACKED_BOSSES = ConcurrentHashMap.newKeySet();

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
        if (pdata.m_128471_(APOTH_BOSS_TAG)) {
            return;
        }

        ResourceLocation entityId = EntityType.m_20613_(living.m_6095_());
        if (entityId == null) {
            return;
        }
        String idStr = entityId.toString();
        DmzScalingHelpers.ScalingTier tier = classifyEntity(living, idStr);
        if (tier == DmzScalingHelpers.ScalingTier.NONE) {
            return;
        }

        if (tier == DmzScalingHelpers.ScalingTier.VANILLA_BOSS || tier == DmzScalingHelpers.ScalingTier.VANILLA_MINIBOSS) {
            TRACKED_BOSSES.add(living.m_20148_());
        }

        // Already scaled and does not need lazy rescale
        if (pdata.m_128471_(TAG_SCALED) && !pdata.m_128471_(TAG_NEEDS_RESCALE)) {
            return;
        }

        applyOrRefreshScaling(level, living, tier, false);
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().m_5776_()) {
            return;
        }
        Entity e = event.getEntity();
        if (e != null) {
            TRACKED_BOSSES.remove(e.m_20148_());
        }
    }

    /**
     * Applies or refreshes DMZ scaling on a target entity. Returns true if scaled with a valid loaded player.
     */
    public static boolean applyOrRefreshScaling(
            ServerLevel level,
            LivingEntity living,
            DmzScalingHelpers.ScalingTier tier,
            boolean forceAnnounce
    ) {
        double scanRange = (tier == DmzScalingHelpers.ScalingTier.VANILLA_MOB)
                ? DmzScalingConfig.getMobScanRange()
                : DmzScalingConfig.getBossScanRange();
        boolean fallbackAnyInDimension = (tier != DmzScalingHelpers.ScalingTier.VANILLA_MOB);

        PartySample sample = samplePartyBasePower(
                level,
                living,
                scanRange,
                DmzScalingConfig.getPartyScanRange(),
                fallbackAnyInDimension
        );

        AttributeInstance maxHpAttr = living.m_21051_(Attributes.f_22276_);
        double vanillaHp = (maxHpAttr != null) ? maxHpAttr.m_22115_() : 20.0;

        DmzScalingHelpers.ComputedScaling baseScaling = switch (tier) {
            case VANILLA_BOSS -> DmzScalingHelpers.computeVanillaBossScaling(vanillaHp, sample.aggregatedPower());
            case VANILLA_MINIBOSS -> DmzScalingHelpers.computeVanillaMinibossScaling(vanillaHp, sample.aggregatedPower());
            case VANILLA_MOB -> DmzScalingHelpers.computeVanillaMobScaling(sample.aggregatedPower());
            default -> null;
        };
        if (baseScaling == null) {
            return false;
        }

        double tierMaxHpMult = (tier == DmzScalingHelpers.ScalingTier.VANILLA_BOSS)
                ? DmzScalingConfig.getBossMaxHpMult()
                : (tier == DmzScalingHelpers.ScalingTier.VANILLA_MINIBOSS ? DmzScalingConfig.getMinibossMaxHpMult() : DmzScalingConfig.getMobMaxHpMult());

        DmzScalingHelpers.ComputedScaling scaling = (tier == DmzScalingHelpers.ScalingTier.VANILLA_MOB)
                ? baseScaling
                : DmzScalingHelpers.applyPartyHpBonus(
                        baseScaling,
                        sample.partyCount(),
                        DmzScalingConfig.getPartyBonusHpPerExtraPlayer(),
                        DmzScalingConfig.getMaxPartyHpBonusMultiplier(),
                        tierMaxHpMult
                );

        if (maxHpAttr != null && scaling.hpMultiplier() > 1.001) {
            replacePermanentModifier(maxHpAttr, HP_MOD_ID, "dmzscaling_hp", scaling.hpMultiplier() - 1.0, AttributeModifier.Operation.MULTIPLY_BASE);
            float newMax = living.m_21233_();
            if (living instanceof WitherBoss wither && wither.m_31502_() > 0) {
                living.m_21153_(Math.max(1.0f, newMax / 3.0f));
            } else {
                living.m_21153_(newMax);
            }
        }

        AttributeInstance atkAttr = living.m_21051_(Attributes.f_22281_);
        if (atkAttr != null && scaling.damageMultiplier() > 1.001) {
            replacePermanentModifier(atkAttr, DMG_MOD_ID, "dmzscaling_dmg", scaling.damageMultiplier() - 1.0, AttributeModifier.Operation.MULTIPLY_BASE);
        }

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

        CompoundTag pdata = living.getPersistentData();
        pdata.m_128379_(TAG_SCALED, true);
        pdata.m_128379_(TAG_NEEDS_RESCALE, !sample.foundLoadedPlayer());
        pdata.m_128359_(TAG_TIER, tier.name());
        pdata.m_128347_(TAG_HP_MULT, scaling.hpMultiplier());
        pdata.m_128347_(TAG_DMG_MULT, scaling.damageMultiplier());
        pdata.m_128405_(TAG_SAMPLE_LVL, sample.aggregatedPower().dmzLevel());
        pdata.m_128405_(TAG_PARTY_SIZE, Math.max(1, sample.partyCount()));

        if (forceAnnounce && sample.foundLoadedPlayer()) {
            maybeAnnounceBoss(level, living, pdata, true);
        }
        return sample.foundLoadedPlayer();
    }

    /**
     * O(1) fast-exit tick handler for tracked bosses/minibosses ONLY:
     * 1. Tops off WitherBoss HP when spawn invulnerability finishes.
     * 2. Scales EnderDragon EndCrystal healing every 10 ticks.
     * 3. Executes lazy combat re-scaling when a loaded player approaches an unengaged boss.
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (TRACKED_BOSSES.isEmpty()) {
            return;
        }
        LivingEntity living = event.getEntity();
        if (living.m_9236_().m_5776_() || !TRACKED_BOSSES.contains(living.m_20148_())) {
            return;
        }
        if (!(living.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        CompoundTag pdata = living.getPersistentData();

        // 1. Wither post-charge full heal
        if (living instanceof WitherBoss wither && pdata.m_128471_(TAG_SCALED) && !pdata.m_128471_(TAG_WITHER_TOPPED)) {
            if (wither.m_31502_() <= 1) {
                wither.m_21153_(wither.m_21233_());
                pdata.m_128379_(TAG_WITHER_TOPPED, true);
            }
        }

        // 2. Ender Dragon End Crystal scaled healing (every 10 ticks when connected to an alive EndCrystal)
        if (living instanceof EnderDragon dragon
                && DmzScalingConfig.isScaleEnderDragonCrystalHealing()
                && dragon.f_31086_ != null
                && !dragon.f_31086_.m_213877_()
                && dragon.f_19797_ % 10 == 0
                && dragon.m_21223_() > 0.0f
                && dragon.m_21223_() < dragon.m_21233_()) {
            double hpMult = pdata.m_128459_(TAG_HP_MULT);
            float bonusHeal = DmzScalingHelpers.computeBonusCrystalHealPer10Ticks(
                    hpMult,
                    dragon.m_21233_(),
                    DmzScalingConfig.getCrystalHealScaleFactor(),
                    DmzScalingConfig.getMaxCrystalHealPercentPerSecond()
            );
            if (bonusHeal > 0.01f) {
                dragon.m_21153_(Math.min(dragon.m_21233_(), dragon.m_21223_() + bonusHeal));
            }
        }

        // 3. Periodic lazy combat rescale check (every 20 ticks while not yet combat-locked)
        if (living.f_19797_ % 20 == 0
                && DmzScalingConfig.isEnableLazyCombatRescale()
                && !pdata.m_128471_(TAG_COMBAT_LOCKED)) {
            boolean atFullHp = !(living instanceof WitherBoss w && w.m_31502_() > 0)
                    && (living.m_21223_() >= living.m_21233_() * 0.99f);
            if (pdata.m_128471_(TAG_NEEDS_RESCALE) || atFullHp) {
                ResourceLocation key = EntityType.m_20613_(living.m_6095_());
                if (key != null) {
                    DmzScalingHelpers.ScalingTier tier = classifyEntity(living, key.toString());
                    if (tier == DmzScalingHelpers.ScalingTier.VANILLA_BOSS || tier == DmzScalingHelpers.ScalingTier.VANILLA_MINIBOSS) {
                        if (hasLoadedPlayerWithin(level, living, 64.0)) {
                            if (applyOrRefreshScaling(level, living, tier, false)) {
                                pdata.m_128379_(TAG_COMBAT_LOCKED, true);
                                maybeAnnounceBoss(level, living, pdata, false);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Scales WitherBoss post-charge passive regeneration (1.0 HP every 20 ticks in vanilla).
     */
    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity living = event.getEntity();
        if (!(living instanceof WitherBoss wither) || living.m_9236_().m_5776_()) {
            return;
        }
        if (!DmzScalingConfig.isScaleWitherPassiveRegen() || wither.m_31502_() > 0) {
            return;
        }
        // Vanilla WitherBoss.customServerAiStep calls this.heal(1.0F) every 20 ticks
        if (event.getAmount() > 1.05f) {
            return;
        }
        CompoundTag pdata = wither.getPersistentData();
        if (!pdata.m_128471_(TAG_SCALED)) {
            return;
        }
        double hpMult = pdata.m_128459_(TAG_HP_MULT);
        float scaledHeal = DmzScalingHelpers.computeScaledWitherPassiveHeal(
                event.getAmount(),
                hpMult,
                wither.m_21233_(),
                DmzScalingConfig.getWitherRegenScaleFactor(),
                DmzScalingConfig.getMaxWitherRegenPercentPerSecond()
        );
        if (scaledHeal > event.getAmount()) {
            event.setAmount(scaledHeal);
        }
    }

    /**
     * STAGE 1 (EventPriority.HIGHEST — runs BEFORE DragonMineZ CombatEvent.onLivingHurt @ HIGH):
     * Scales the raw outgoing attack of scaled mobs/minibosses/bosses so DragonMineZ's DEF,
     * blocking, parry, speed dodge, and evasion operate naturally on the real scaled attack.
     * Also triggers lazy combat rescale if a boss is hit or attacks before being locked.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurtPreDefense(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.m_9236_().m_5776_() || !(victim.m_9236_() instanceof ServerLevel level)) {
            return;
        }
        DamageSource source = event.getSource();
        if (source == null) {
            return;
        }
        Entity attacker = source.m_7639_();

        // If a player hits an un-locked boss/miniboss first, lock & announce its scaling
        if (attacker instanceof Player && TRACKED_BOSSES.contains(victim.m_20148_())) {
            lockAndAnnounceOnFirstCombat(level, victim);
        }

        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }
        if (TRACKED_BOSSES.contains(livingAttacker.m_20148_())) {
            lockAndAnnounceOnFirstCombat(level, livingAttacker);
        }

        victim.getPersistentData().m_128473_(TAG_LAST_RAW_DMG);

        CompoundTag pdata = livingAttacker.getPersistentData();
        if (!pdata.m_128471_(TAG_SCALED)) {
            return;
        }
        double dmgMult = pdata.m_128459_(TAG_DMG_MULT);
        if (dmgMult <= 1.001) {
            return;
        }

        Entity direct = source.m_7640_();
        boolean isBossWithHardcodedDamage = (livingAttacker instanceof EnderDragon || livingAttacker instanceof WitherBoss);
        boolean isDirectMelee = (direct == livingAttacker) && !isBossWithHardcodedDamage && isMeleeDamageType(source);
        boolean alreadyScaled = isDirectMelee && livingAttacker.m_21051_(Attributes.f_22281_) != null;

        float rawAmount = event.getAmount();
        float unscaledFloor = alreadyScaled ? (float) (rawAmount / Math.max(1.0, dmgMult)) : rawAmount;
        victim.getPersistentData().m_128350_(TAG_LAST_RAW_DMG, unscaledFloor);

        float scaledPreDef = DmzScalingHelpers.computePreDefenseScaledDamage(rawAmount, dmgMult, alreadyScaled);
        if (scaledPreDef != rawAmount) {
            event.setAmount(scaledPreDef);
        }
    }

    /**
     * STAGE 2 (LivingDamageEvent @ EventPriority.HIGH — runs AFTER DragonMineZ DEF & Armor,
     * and BEFORE DMZ's LOWEST final damage listeners):
     * Enforces the per-hit soft cap (or lower DoT soft cap for Dragon Breath / AreaEffectCloud)
     * as a fraction of the victim player's Max HP so glass-cannon builds are never unfairly 1-shot.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamagePostDefense(LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim instanceof Player) || victim.m_9236_().m_5776_()) {
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

        String tierName = pdata.m_128461_(TAG_TIER);
        boolean isDot = DmzScalingHelpers.isContinuousDotDamage(source, source.m_7640_());
        double maxHitPct = 0.0;
        if (DmzScalingHelpers.ScalingTier.VANILLA_BOSS.name().equals(tierName)) {
            maxHitPct = isDot ? DmzScalingConfig.getBossMaxDotHitPct() : DmzScalingConfig.getBossMaxHitPct();
        } else if (DmzScalingHelpers.ScalingTier.VANILLA_MINIBOSS.name().equals(tierName)) {
            maxHitPct = isDot ? DmzScalingConfig.getMinibossMaxDotHitPct() : DmzScalingConfig.getMinibossMaxHitPct();
        }
        if (maxHitPct <= 0.0) {
            return;
        }

        float rawFloor = victim.getPersistentData().m_128457_(TAG_LAST_RAW_DMG);
        victim.getPersistentData().m_128473_(TAG_LAST_RAW_DMG);
        double dmgMult = Math.max(1.0, pdata.m_128459_(TAG_DMG_MULT));
        float fallbackFloor = (float) (event.getAmount() / dmgMult);
        float capped = DmzScalingHelpers.applyPostDefenseSoftCap(
                event.getAmount(),
                rawFloor > 0.0f ? rawFloor : fallbackFloor,
                victim.m_21233_(),
                maxHitPct
        );
        if (capped != event.getAmount()) {
            event.setAmount(capped);
        }
    }

    private static void lockAndAnnounceOnFirstCombat(ServerLevel level, LivingEntity boss) {
        CompoundTag pdata = boss.getPersistentData();
        if (pdata.m_128471_(TAG_COMBAT_LOCKED)) {
            return;
        }
        if (DmzScalingConfig.isEnableLazyCombatRescale() && pdata.m_128471_(TAG_NEEDS_RESCALE)) {
            ResourceLocation key = EntityType.m_20613_(boss.m_6095_());
            if (key != null) {
                DmzScalingHelpers.ScalingTier tier = classifyEntity(boss, key.toString());
                if (tier != DmzScalingHelpers.ScalingTier.NONE) {
                    applyOrRefreshScaling(level, boss, tier, false);
                }
            }
        }
        pdata.m_128379_(TAG_COMBAT_LOCKED, true);
        maybeAnnounceBoss(level, boss, pdata, false);
    }

    private static void maybeAnnounceBoss(ServerLevel level, LivingEntity boss, CompoundTag pdata, boolean force) {
        if (!DmzScalingConfig.isAnnounceScaledBosses()) {
            return;
        }
        if (!force && pdata.m_128471_(TAG_ANNOUNCED)) {
            return;
        }
        pdata.m_128379_(TAG_ANNOUNCED, true);

        double hpMult = pdata.m_128459_(TAG_HP_MULT);
        double dmgMult = pdata.m_128459_(TAG_DMG_MULT);
        int sampleLvl = Math.max(1, pdata.m_128451_(TAG_SAMPLE_LVL));
        int partySize = Math.max(1, pdata.m_128451_(TAG_PARTY_SIZE));
        int maxHp = Math.round(boss.m_21233_());
        String bossName = boss.m_5446_().getString();

        String msg = String.format(
                "§5[DMZ Scaling] §d%s §7-> §c%,d HP §8(x%.1f HP | x%.1f DMG | DMZ Lv.%d | %dP)",
                bossName, maxHp, hpMult, dmgMult, sampleLvl, partySize
        );
        Component comp = Component.m_237113_(msg);
        double rangeSq = 96.0 * 96.0;
        for (ServerPlayer sp : level.m_6907_()) {
            if (sp.m_6084_() && sp.m_20280_(boss) <= rangeSq) {
                sp.m_5661_(comp, true);
            }
        }
    }

    private static boolean isMeleeDamageType(DamageSource source) {
        String msgId = source.m_19385_();
        return "mob".equals(msgId) || "sting".equals(msgId);
    }

    public static DmzScalingHelpers.ScalingTier classifyEntity(LivingEntity living, String idStr) {
        return DmzScalingHelpers.classifyEntityId(idStr, living instanceof Enemy);
    }

    private static boolean hasLoadedPlayerWithin(ServerLevel level, LivingEntity entity, double range) {
        double maxSq = range * range;
        for (ServerPlayer sp : level.m_6907_()) {
            if (sp.m_6084_() && !sp.m_5833_() && sp.m_20280_(entity) <= maxSq) {
                if (readLoadedDmzBasePower(sp) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public record PartySample(
            DmzScalingHelpers.PlayerBasePower aggregatedPower,
            int partyCount,
            boolean foundLoadedPlayer
    ) {
    }

    public static PartySample samplePartyBasePower(
            ServerLevel level,
            LivingEntity entity,
            double maxScanRange,
            double partyRange,
            boolean fallbackAnyInDimension
    ) {
        List<ServerPlayer> players = level.m_6907_();
        if (players.isEmpty()) {
            return new PartySample(DmzScalingHelpers.PlayerBasePower.VANILLA_DEFAULT, 1, false);
        }
        double maxDistSq = maxScanRange * maxScanRange;
        double partyDistSq = partyRange * partyRange;

        ServerPlayer nearest = null;
        DmzScalingHelpers.PlayerBasePower nearestPower = null;
        double bestDistSq = Double.MAX_VALUE;

        List<DmzScalingHelpers.PlayerBasePower> partyPowers = new ArrayList<>();
        DmzScalingHelpers.PlayerBasePower strongestFallback = null;
        int strongestLvl = -1;

        for (ServerPlayer sp : players) {
            if (!sp.m_6084_() || sp.m_5833_()) {
                continue;
            }
            DmzScalingHelpers.PlayerBasePower p = readLoadedDmzBasePower(sp);
            if (p == null) {
                continue;
            }
            double distSq = sp.m_20280_(entity);
            if (distSq <= maxDistSq && distSq < bestDistSq) {
                bestDistSq = distSq;
                nearest = sp;
                nearestPower = p;
            }
            if (distSq <= partyDistSq) {
                partyPowers.add(p);
            }
            if (fallbackAnyInDimension && p.dmzLevel() > strongestLvl) {
                strongestLvl = p.dmzLevel();
                strongestFallback = p;
            }
        }

        if (!partyPowers.isEmpty()) {
            // Ensure nearest player is at index 0 for NEAREST mode
            if (nearestPower != null) {
                partyPowers.remove(nearestPower);
                partyPowers.add(0, nearestPower);
            }
            DmzScalingHelpers.PlayerBasePower agg = DmzScalingHelpers.aggregatePartyPower(
                    partyPowers,
                    DmzScalingConfig.getPartyScalingMode()
            );
            return new PartySample(agg, partyPowers.size(), true);
        }

        if (nearest != null && nearestPower != null) {
            return new PartySample(nearestPower, 1, true);
        }
        if (strongestFallback != null) {
            return new PartySample(strongestFallback, 1, true);
        }
        return new PartySample(DmzScalingHelpers.PlayerBasePower.VANILLA_DEFAULT, 1, false);
    }

    public static DmzScalingHelpers.PlayerBasePower readDmzBasePower(ServerPlayer player) {
        DmzScalingHelpers.PlayerBasePower loaded = readLoadedDmzBasePower(player);
        return (loaded != null) ? loaded : DmzScalingHelpers.PlayerBasePower.VANILLA_DEFAULT;
    }

    public static DmzScalingHelpers.PlayerBasePower readLoadedDmzBasePower(ServerPlayer player) {
        try {
            StatsData data = StatsProvider.get(StatsCapability.INSTANCE, player).orElse(null);
            if (data == null || !data.isDataLoaded()) {
                return null;
            }
            int dmzLevel = Math.max(1, data.getLevel());
            double baseMaxHp = Math.max(20.0, data.getMaxHealth());
            double melee = data.getMeleeDamageNoMultipliers();
            double strike = data.getStrikeDamageNoForms();
            double ki = data.getKiDamageNoForms();
            double baseOffense = Math.max(4.0, Math.max(melee, Math.max(strike, ki)));
            return new DmzScalingHelpers.PlayerBasePower(dmzLevel, baseMaxHp, baseOffense);
        } catch (Throwable ignored) {
            return null;
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
