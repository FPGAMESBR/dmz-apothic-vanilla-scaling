package com.dmzscaling;

import com.dmzscaling.mixin.DmzScalingMixinPlugin;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DmzScalingSelfTest {
    private static int passed = 0;

    private static void assertTrue(String label, boolean cond) {
        if (!cond) throw new AssertionError("FAILED (expected true): " + label);
        passed++;
    }

    private static void assertFalse(String label, boolean cond) {
        if (cond) throw new AssertionError("FAILED (expected false): " + label);
        passed++;
    }

    private static void assertNear(String label, double actual, double expected, double eps) {
        if (Math.abs(actual - expected) > eps) {
            throw new AssertionError("FAILED " + label + ": actual=" + actual + " expected=" + expected);
        }
        passed++;
    }

    public static void main(String[] args) {
        DmzScalingConfig.bakeCaches();

        // 1. Apotheosis BossRegistry filter classification & configurable namespaces
        assertTrue("DMZ Raditz kept",
                DmzScalingHelpers.classifyApotheosisBoss("dragonminez:saga_raditz", false, true, true) == DmzScalingHelpers.Decision.KEEP_DMZ);
        assertTrue("DMZ Bandit kept",
                DmzScalingHelpers.classifyApotheosisBoss("dragonminez:bandit", false, true, true) == DmzScalingHelpers.Decision.KEEP_DMZ);
        assertTrue("DMZ Ultra entity kept via allowedBossNamespaces",
                DmzScalingHelpers.classifyApotheosisBoss("dmzultra:arrancar", false, true, true) == DmzScalingHelpers.Decision.KEEP_DMZ);
        assertTrue("DMZ Revamp entity kept via allowedBossNamespaces",
                DmzScalingHelpers.classifyApotheosisBoss("dmzrevamp:saga_gogeta_base", false, true, true) == DmzScalingHelpers.Decision.KEEP_DMZ);
        assertTrue("Vanilla Zombie removed",
                DmzScalingHelpers.classifyApotheosisBoss("minecraft:zombie", false, true, true) == DmzScalingHelpers.Decision.REMOVE);
        assertTrue("Vanilla Vindicator removed",
                DmzScalingHelpers.classifyApotheosisBoss("minecraft:vindicator", false, true, true) == DmzScalingHelpers.Decision.REMOVE);
        assertTrue("Vanilla Blaze removed",
                DmzScalingHelpers.classifyApotheosisBoss("minecraft:blaze", false, true, true) == DmzScalingHelpers.Decision.REMOVE);
        assertTrue("Vanilla Enderman removed",
                DmzScalingHelpers.classifyApotheosisBoss("minecraft:enderman", false, true, true) == DmzScalingHelpers.Decision.REMOVE);
        assertTrue("Twilight Kobold kept when keepTwilight=true",
                DmzScalingHelpers.classifyApotheosisBoss("twilightforest:kobold", true, true, true) == DmzScalingHelpers.Decision.KEEP_TWILIGHT);
        assertTrue("Twilight Kobold removed when keepTwilight=false",
                DmzScalingHelpers.classifyApotheosisBoss("twilightforest:kobold", true, true, false) == DmzScalingHelpers.Decision.REMOVE);

        // 2. End-to-end map filtering + Empty-Dimension Fallback Protection
        Map<ResourceLocation, JsonElement> bossMap = new LinkedHashMap<>();
        bossMap.put(new ResourceLocation("apotheosis", "overworld/zombie"),
                JsonParser.parseString("{\"entity\":\"minecraft:zombie\",\"dimensions\":[\"minecraft:overworld\"]}"));
        bossMap.put(new ResourceLocation("apotheosis", "the_nether/blaze"),
                JsonParser.parseString("{\"entity\":\"minecraft:blaze\",\"dimensions\":[\"minecraft:the_nether\"]}"));
        bossMap.put(new ResourceLocation("apotheosis", "the_end/enderman"),
                JsonParser.parseString("{\"entity\":\"minecraft:enderman\",\"dimensions\":[\"minecraft:the_end\"]}"));
        bossMap.put(new ResourceLocation("apotheosis", "twilight/kobold"),
                JsonParser.parseString("{\"entity\":\"twilightforest:kobold\",\"dimensions\":[\"twilightforest:twilight_forest\"]}"));
        bossMap.put(new ResourceLocation("apotheosis", "dmz/saga_raditz"),
                JsonParser.parseString("{\"entity\":\"dragonminez:saga_raditz\",\"dimensions\":[\"minecraft:overworld\"]}"));
        bossMap.put(new ResourceLocation("apotheosis", "dmz_extra/bandit"),
                JsonParser.parseString("{\"entity\":\"dragonminez:bandit\",\"dimensions\":[\"minecraft:overworld\"]}"));
        bossMap.put(new ResourceLocation("apotheosis", "dmz_extra/namek_zarbon"),
                JsonParser.parseString("{\"entity\":\"dragonminez:saga_zarbon\",\"dimensions\":[\"dragonminez:namek\"]}"));
        bossMap.put(new ResourceLocation("apotheosis", "dmz_extra/nether_dabura"),
                JsonParser.parseString("{\"entity\":\"dragonminez:saga_dabura\",\"dimensions\":[\"minecraft:the_nether\",\"dragonminez:demon_realm\"]}"));
        bossMap.put(new ResourceLocation("apotheosis", "dmz_extra/end_cooler"),
                JsonParser.parseString("{\"entity\":\"dragonminez:saga_cooler\",\"dimensions\":[\"minecraft:the_end\",\"dragonminez:otherworld\"]}"));

        DmzScalingHelpers.filterBossRegistryMap(bossMap);
        assertFalse("overworld/zombie filtered out", bossMap.containsKey(new ResourceLocation("apotheosis", "overworld/zombie")));
        assertFalse("the_nether/blaze filtered out because nether_dabura covers nether", bossMap.containsKey(new ResourceLocation("apotheosis", "the_nether/blaze")));
        assertFalse("the_end/enderman filtered out because end_cooler covers end", bossMap.containsKey(new ResourceLocation("apotheosis", "the_end/enderman")));
        assertTrue("twilight/kobold preserved", bossMap.containsKey(new ResourceLocation("apotheosis", "twilight/kobold")));
        assertTrue("dmz/saga_raditz preserved", bossMap.containsKey(new ResourceLocation("apotheosis", "dmz/saga_raditz")));
        assertTrue("dmz_extra/bandit preserved", bossMap.containsKey(new ResourceLocation("apotheosis", "dmz_extra/bandit")));
        assertTrue("dmz_extra/namek_zarbon preserved", bossMap.containsKey(new ResourceLocation("apotheosis", "dmz_extra/namek_zarbon")));
        assertTrue("dmz_extra/nether_dabura preserved", bossMap.containsKey(new ResourceLocation("apotheosis", "dmz_extra/nether_dabura")));
        assertTrue("dmz_extra/end_cooler preserved", bossMap.containsKey(new ResourceLocation("apotheosis", "dmz_extra/end_cooler")));

        // 2b. Verify Empty-Dimension Fallback when a dimension would otherwise become empty
        Map<ResourceLocation, JsonElement> fallbackMap = new LinkedHashMap<>();
        fallbackMap.put(new ResourceLocation("apotheosis", "overworld/zombie"),
                JsonParser.parseString("{\"entity\":\"minecraft:zombie\",\"dimensions\":[\"minecraft:overworld\"]}"));
        fallbackMap.put(new ResourceLocation("apotheosis", "dmz/saga_raditz"),
                JsonParser.parseString("{\"entity\":\"dragonminez:saga_raditz\",\"dimensions\":[\"minecraft:overworld\"]}"));
        fallbackMap.put(new ResourceLocation("apotheosis", "custom_dim/boss"),
                JsonParser.parseString("{\"entity\":\"minecraft:wither_skeleton\",\"dimensions\":[\"custommod:void_dim\"]}"));

        DmzScalingHelpers.filterBossRegistryMap(
                fallbackMap, true, true, true, true, true, true, true,
                Set.of("dragonminez"), Set.of("twilightforest:twilight_forest")
        );
        assertFalse("overworld/zombie removed", fallbackMap.containsKey(new ResourceLocation("apotheosis", "overworld/zombie")));
        assertTrue("custom_dim/boss restored by empty-dimension fallback", fallbackMap.containsKey(new ResourceLocation("apotheosis", "custom_dim/boss")));

        // 2c. Bundled extra toggle logic by prefix (overworld, namek_, nether_, end_)
        assertTrue("namek_ disabled when enableNamek=false",
                DmzScalingHelpers.isBundledExtraDisabled("dmz_extra/namek_cui", true, false, true, true));
        assertFalse("namek_ enabled when enableNamek=true",
                DmzScalingHelpers.isBundledExtraDisabled("dmz_extra/namek_cui", false, true, false, false));
        assertTrue("nether_ disabled when enableNether=false",
                DmzScalingHelpers.isBundledExtraDisabled("dmz_extra/nether_dabura", true, true, false, true));
        assertTrue("end_ disabled when enableEnd=false",
                DmzScalingHelpers.isBundledExtraDisabled("dmz_extra/end_cooler", true, true, true, false));

        // 3. Miniboss filtering (FinalizeSpawn + MinibossRegistry)
        assertTrue("Vanilla miniboss blocked in overworld",
                DmzScalingHelpers.shouldBlockApotheosisMiniboss("minecraft:skeleton", "minecraft:overworld"));
        assertFalse("Twilight miniboss allowed in twilight_forest",
                DmzScalingHelpers.shouldBlockApotheosisMiniboss("minecraft:skeleton", "twilightforest:twilight_forest"));
        assertFalse("DMZ entity allowed everywhere",
                DmzScalingHelpers.shouldBlockApotheosisMiniboss("dragonminez:bandit", "minecraft:overworld"));

        Map<ResourceLocation, JsonElement> minibossMap = new LinkedHashMap<>();
        minibossMap.put(new ResourceLocation("apotheosis", "overworld/craig"),
                JsonParser.parseString("{\"entities\":[\"minecraft:goat\"],\"dimensions\":[\"minecraft:overworld\"]}"));
        minibossMap.put(new ResourceLocation("apotheosis", "twilight/druid"),
                JsonParser.parseString("{\"entities\":[\"twilightforest:skeleton_druid\"],\"dimensions\":[\"twilightforest:twilight_forest\"]}"));
        DmzScalingHelpers.filterMinibossRegistryMap(minibossMap);
        assertFalse("Craig filtered from MinibossRegistry", minibossMap.containsKey(new ResourceLocation("apotheosis", "overworld/craig")));
        assertTrue("Twilight miniboss preserved in MinibossRegistry", minibossMap.containsKey(new ResourceLocation("apotheosis", "twilight/druid")));

        // 4. Optional Apotheosis MixinConfigPlugin
        DmzScalingMixinPlugin plugin = new DmzScalingMixinPlugin();
        assertTrue("Plugin finds existing Apotheosis BossRegistry on test classpath",
                plugin.shouldApplyMixin("dev.shadowsoffire.apotheosis.adventure.boss.BossRegistry", "com.dmzscaling.mixin.BossRegistryMixin"));
        assertFalse("Plugin cleanly skips absent target class when mod is missing",
                plugin.shouldApplyMixin("com.nonexistent.mod.FakeClass", "com.dmzscaling.mixin.FakeMixin"));

        // 5. Entity Blacklist (including minecraft:silverfish!) & Modded Boss/Miniboss Presets
        assertTrue("minecraft:silverfish is blacklisted by default",
                DmzScalingConfig.isBlacklistedEntity("minecraft:silverfish"));
        assertTrue("Silverfish classifies as NONE even if Enemy",
                DmzScalingHelpers.classifyEntityId("minecraft:silverfish", true) == DmzScalingHelpers.ScalingTier.NONE);
        assertTrue("Endermite classifies as NONE",
                DmzScalingHelpers.classifyEntityId("minecraft:endermite", true) == DmzScalingHelpers.ScalingTier.NONE);
        assertTrue("Zombie classifies as VANILLA_MOB",
                DmzScalingHelpers.classifyEntityId("minecraft:zombie", true) == DmzScalingHelpers.ScalingTier.VANILLA_MOB);
        assertTrue("Warden classifies as VANILLA_MINIBOSS",
                DmzScalingHelpers.classifyEntityId("minecraft:warden", true) == DmzScalingHelpers.ScalingTier.VANILLA_MINIBOSS);
        assertTrue("Iron's Spellbooks Dead King classifies as VANILLA_MINIBOSS preset",
                DmzScalingHelpers.classifyEntityId("irons_spellbooks:dead_king", true) == DmzScalingHelpers.ScalingTier.VANILLA_MINIBOSS);
        assertTrue("Alex's Caves Tremorzilla classifies as VANILLA_MINIBOSS preset",
                DmzScalingHelpers.classifyEntityId("alexscaves:tremorzilla", false) == DmzScalingHelpers.ScalingTier.VANILLA_MINIBOSS);
        assertTrue("Ender Dragon classifies as VANILLA_BOSS",
                DmzScalingHelpers.classifyEntityId("minecraft:ender_dragon", false) == DmzScalingHelpers.ScalingTier.VANILLA_BOSS);
        assertTrue("Twilight Forest Hydra classifies as VANILLA_BOSS preset",
                DmzScalingHelpers.classifyEntityId("twilightforest:hydra", false) == DmzScalingHelpers.ScalingTier.VANILLA_BOSS);
        assertTrue("Ice & Fire Dragon classifies as VANILLA_BOSS preset",
                DmzScalingHelpers.classifyEntityId("iceandfire:fire_dragon", false) == DmzScalingHelpers.ScalingTier.VANILLA_BOSS);
        assertTrue("Cataclysm Ignis excluded by default because Cataclysm has own damageCap",
                DmzScalingHelpers.classifyEntityId("cataclysm:ignis", false) == DmzScalingHelpers.ScalingTier.NONE);

        // 6. Party Scaling (STRONGEST, AVERAGE, NEAREST + Bonus HP per Extra Player)
        List<DmzScalingHelpers.PlayerBasePower> party = List.of(
                new DmzScalingHelpers.PlayerBasePower(20, 100.0, 25.0),
                new DmzScalingHelpers.PlayerBasePower(200, 900.0, 225.0),
                new DmzScalingHelpers.PlayerBasePower(80, 500.0, 110.0)
        );
        DmzScalingHelpers.PlayerBasePower nearest = DmzScalingHelpers.aggregatePartyPower(party, "NEAREST");
        assertTrue("NEAREST picks index 0 (Lv.20)", nearest.dmzLevel() == 20);

        DmzScalingHelpers.PlayerBasePower strongest = DmzScalingHelpers.aggregatePartyPower(party, "STRONGEST");
        assertTrue("STRONGEST picks Lv.200", strongest.dmzLevel() == 200);

        DmzScalingHelpers.PlayerBasePower avg = DmzScalingHelpers.aggregatePartyPower(party, "AVERAGE");
        assertTrue("AVERAGE computes mean level 100", avg.dmzLevel() == 100);
        assertNear("AVERAGE computes mean HP 500", avg.baseMaxHp(), 500.0, 0.01);

        DmzScalingHelpers.ComputedScaling soloDragon = DmzScalingHelpers.computeVanillaBossScaling(
                200.0, new DmzScalingHelpers.PlayerBasePower(200, 600.0, 100.0));
        DmzScalingHelpers.ComputedScaling trioDragon = DmzScalingHelpers.applyPartyHpBonus(
                soloDragon, 3, 0.35, 2.5, 250.0);
        // 3 players -> 1 + 2 * 0.35 = 1.70x HP
        assertNear("3-player party boosts Dragon HP mult by 1.70x", trioDragon.hpMultiplier(), soloDragon.hpMultiplier() * 1.70, 0.01);

        // 7. Hybrid Scaling — Tier 3, Tier 2, Tier 1
        DmzScalingHelpers.ComputedScaling mobLv1 = DmzScalingHelpers.computeVanillaMobScaling(
                new DmzScalingHelpers.PlayerBasePower(1, 20.0, 4.0));
        assertNear("Mob Lv1 HP mult", mobLv1.hpMultiplier(), 1.5, 0.001);
        assertNear("Mob Lv1 Dmg mult", mobLv1.damageMultiplier(), 1.25, 0.001);

        DmzScalingHelpers.ComputedScaling mobLv999 = DmzScalingHelpers.computeVanillaMobScaling(
                new DmzScalingHelpers.PlayerBasePower(999, 5000.0, 3000.0));
        assertNear("Mob Lv999 HP capped at 6.0x", mobLv999.hpMultiplier(), 6.0, 0.001);
        assertNear("Mob Lv999 Dmg capped at 3.5x", mobLv999.damageMultiplier(), 3.5, 0.001);

        DmzScalingHelpers.ComputedScaling wardenLate = DmzScalingHelpers.computeVanillaMinibossScaling(
                500.0, new DmzScalingHelpers.PlayerBasePower(500, 1800.0, 500.0));
        assertNear("Warden late HP = 20x", wardenLate.hpMultiplier(), 20.0, 0.001);

        DmzScalingHelpers.ComputedScaling dragonGodKi = DmzScalingHelpers.computeVanillaBossScaling(
                200.0, new DmzScalingHelpers.PlayerBasePower(2000, 5000.0, 5000.0));
        assertNear("Dragon GodKi HP capped at 250x", dragonGodKi.hpMultiplier(), 250.0, 0.001);
        assertNear("Dragon GodKi Dmg capped at 25x", dragonGodKi.damageMultiplier(), 25.0, 0.001);

        // 8. Two-Stage Damage Scaling (Pre-DEF @ HIGHEST + Post-DEF Soft Cap @ HIGH) & DoT Cap
        float rawHit = 15.0f;
        float preDef = DmzScalingHelpers.computePreDefenseScaledDamage(rawHit, 20.0, false);
        assertNear("Stage 1 scales 15 -> 300 before DMZ DEF", preDef, 300.0, 0.01);
        // Simulate DMZ DEF reducing 300 -> 180 on a 400 HP player; Stage 2 soft-caps at 28% of 400 = 112
        float postDefCapped = DmzScalingHelpers.applyPostDefenseSoftCap(180.0f, rawHit, 400.0f, 0.28);
        assertNear("Stage 2 caps post-DEF 180 to 112 (28% of 400 HP)", postDefCapped, 112.0, 0.01);
        // Simulate tanky DMZ DEF reducing 300 -> 65 on a 400 HP player; stays 65!
        float postDefTank = DmzScalingHelpers.applyPostDefenseSoftCap(65.0f, rawHit, 400.0f, 0.28);
        assertNear("Stage 2 preserves 65 when tanky DEF already reduced hit below cap", postDefTank, 65.0, 0.01);

        // Verify attribute-scaled melee (alreadyScaled = true, e.g. Warden melee 30 * 10x = 300) is ALSO soft-capped!
        float wardenAttrMelee = DmzScalingHelpers.computeScaledHitDamage(300.0f, 10.0, true, 400.0f, 0.22);
        assertNear("Warden attribute-scaled 300 melee soft-capped to 22% of 400 HP = 88", wardenAttrMelee, 88.0, 0.01);

        // Continuous DoT detection & lower DoT soft cap (8% vs 28%)
        assertTrue("dragonBreath detected as DoT", DmzScalingHelpers.isContinuousDotMsgId("dragonBreath"));
        assertTrue("wither detected as DoT", DmzScalingHelpers.isContinuousDotMsgId("wither"));
        assertFalse("mob melee not detected as DoT", DmzScalingHelpers.isContinuousDotMsgId("mob"));
        float dotCapped = DmzScalingHelpers.applyPostDefenseSoftCap(90.0f, 6.0f, 400.0f, 0.08);
        assertNear("Dragon breath DoT capped at 8% of 400 HP = 32", dotCapped, 32.0, 0.01);

        // 9. Scaled Boss Mechanics: Ender Dragon End Crystal Heal & Wither Passive Regen
        float crystalBonus = DmzScalingHelpers.computeBonusCrystalHealPer10Ticks(100.0, 20000.0f, 0.65, 0.012);
        assertTrue("Scaled Dragon crystal bonus heal > 50 HP per 10 ticks", crystalBonus > 50.0f && crystalBonus <= 119.0f);

        float witherHeal = DmzScalingHelpers.computeScaledWitherPassiveHeal(1.0f, 80.0, 24000.0f, 0.50, 0.005);
        assertTrue("Scaled Wither passive regen > 20 HP/s and <= 120 HP/s (0.5% cap)", witherHeal > 20.0f && witherHeal <= 120.0f);

        System.out.println("ALL " + passed + " UNIT ASSERTIONS PASSED!");
    }
}
