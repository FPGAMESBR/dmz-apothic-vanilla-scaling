package com.dmzscaling;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

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
        // 1. Apotheosis BossRegistry filter classification
        assertTrue("DMZ Raditz kept",
                DmzScalingHelpers.classifyApotheosisBoss("dragonminez:saga_raditz", false, true, true) == DmzScalingHelpers.Decision.KEEP_DMZ);
        assertTrue("DMZ Bandit kept",
                DmzScalingHelpers.classifyApotheosisBoss("dragonminez:bandit", false, true, true) == DmzScalingHelpers.Decision.KEEP_DMZ);
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

        // 2. End-to-end map filtering (simulate BossRegistry.prepare return map!)
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

        DmzScalingHelpers.filterBossRegistryMap(bossMap);
        assertFalse("overworld/zombie filtered out", bossMap.containsKey(new ResourceLocation("apotheosis", "overworld/zombie")));
        assertFalse("the_nether/blaze filtered out", bossMap.containsKey(new ResourceLocation("apotheosis", "the_nether/blaze")));
        assertFalse("the_end/enderman filtered out", bossMap.containsKey(new ResourceLocation("apotheosis", "the_end/enderman")));
        assertTrue("twilight/kobold preserved", bossMap.containsKey(new ResourceLocation("apotheosis", "twilight/kobold")));
        assertTrue("dmz/saga_raditz preserved", bossMap.containsKey(new ResourceLocation("apotheosis", "dmz/saga_raditz")));
        assertTrue("dmz_extra/bandit preserved", bossMap.containsKey(new ResourceLocation("apotheosis", "dmz_extra/bandit")));

        // 3. Miniboss filtering
        assertTrue("Vanilla miniboss blocked in overworld",
                DmzScalingHelpers.shouldBlockApotheosisMiniboss("minecraft:skeleton", "minecraft:overworld"));
        assertFalse("Twilight miniboss allowed in twilight_forest",
                DmzScalingHelpers.shouldBlockApotheosisMiniboss("minecraft:skeleton", "twilightforest:twilight_forest"));
        assertFalse("DMZ entity allowed everywhere",
                DmzScalingHelpers.shouldBlockApotheosisMiniboss("dragonminez:bandit", "minecraft:overworld"));

        // 4. Hybrid Scaling — Tier 3 Vanilla Hostile Mobs (capped!)
        DmzScalingHelpers.ComputedScaling mobLv1 = DmzScalingHelpers.computeVanillaMobScaling(
                new DmzScalingHelpers.PlayerBasePower(1, 20.0, 4.0));
        assertNear("Mob Lv1 HP mult", mobLv1.hpMultiplier(), 1.5, 0.001);
        assertNear("Mob Lv1 Dmg mult", mobLv1.damageMultiplier(), 1.25, 0.001);
        assertNear("Mob Lv1 Armor", mobLv1.bonusArmor(), 0.0, 0.001);

        DmzScalingHelpers.ComputedScaling mobLv101 = DmzScalingHelpers.computeVanillaMobScaling(
                new DmzScalingHelpers.PlayerBasePower(101, 200.0, 80.0));
        assertNear("Mob Lv101 HP mult", mobLv101.hpMultiplier(), 3.0, 0.001);
        assertNear("Mob Lv101 Dmg mult", mobLv101.damageMultiplier(), 2.05, 0.001);

        DmzScalingHelpers.ComputedScaling mobLv999 = DmzScalingHelpers.computeVanillaMobScaling(
                new DmzScalingHelpers.PlayerBasePower(999, 5000.0, 3000.0));
        assertNear("Mob Lv999 HP capped at 6.0x", mobLv999.hpMultiplier(), 6.0, 0.001);
        assertNear("Mob Lv999 Dmg capped at 3.5x", mobLv999.damageMultiplier(), 3.5, 0.001);
        assertNear("Mob Lv999 Armor capped at 6.0", mobLv999.bonusArmor(), 6.0, 0.001);

        // 5. Hybrid Scaling — Tier 2 Vanilla Mini-Bosses (Warden 500 HP)
        DmzScalingHelpers.ComputedScaling wardenEarly = DmzScalingHelpers.computeVanillaMinibossScaling(
                500.0, new DmzScalingHelpers.PlayerBasePower(10, 40.0, 15.0));
        assertNear("Warden early HP mult floor 2.5x", wardenEarly.hpMultiplier(), 2.5, 0.001);

        DmzScalingHelpers.ComputedScaling wardenLate = DmzScalingHelpers.computeVanillaMinibossScaling(
                500.0, new DmzScalingHelpers.PlayerBasePower(500, 1800.0, 500.0));
        assertNear("Warden late HP = 500*20 = 10000 -> 20x", wardenLate.hpMultiplier(), 20.0, 0.001);
        assertTrue("Warden late dmg > 5x", wardenLate.damageMultiplier() > 5.0 && wardenLate.damageMultiplier() <= 12.0);

        // 6. Hybrid Scaling — Tier 1 Vanilla Main Bosses (Ender Dragon 200 HP, Wither 300 HP)
        DmzScalingHelpers.ComputedScaling dragonEarly = DmzScalingHelpers.computeVanillaBossScaling(
                200.0, new DmzScalingHelpers.PlayerBasePower(10, 40.0, 15.0));
        assertNear("Dragon early HP floor 5.0x (1000 HP)", dragonEarly.hpMultiplier(), 5.0, 0.001);

        DmzScalingHelpers.ComputedScaling dragonMid = DmzScalingHelpers.computeVanillaBossScaling(
                200.0, new DmzScalingHelpers.PlayerBasePower(200, 600.0, 100.0));
        assertNear("Dragon mid HP = 100*45 = 4500 -> 22.5x", dragonMid.hpMultiplier(), 22.5, 0.001);

        DmzScalingHelpers.ComputedScaling dragonGodKi = DmzScalingHelpers.computeVanillaBossScaling(
                200.0, new DmzScalingHelpers.PlayerBasePower(2000, 5000.0, 5000.0));
        assertNear("Dragon GodKi HP capped at 250x (50000 HP)", dragonGodKi.hpMultiplier(), 250.0, 0.001);
        assertNear("Dragon GodKi Dmg capped at 25x", dragonGodKi.damageMultiplier(), 25.0, 0.001);

        // 7. Per-hit soft cap prevents unfair 1-shots while preserving real threat
        float cappedDragonHit = DmzScalingHelpers.computeScaledHitDamage(15.0f, 20.0, false, 400.0f, 0.28);
        assertNear("Dragon 15*20=300 soft-capped to 28% of 400 HP = 112", cappedDragonHit, 112.0, 0.01);

        float uncappedLowHit = DmzScalingHelpers.computeScaledHitDamage(10.0f, 4.0, false, 400.0f, 0.28);
        assertNear("Dragon 10*4=40 below 112 stays 40", uncappedLowHit, 40.0, 0.01);

        float neverNerfVanilla = DmzScalingHelpers.computeScaledHitDamage(30.0f, 2.0, false, 20.0f, 0.22);
        assertNear("Warden 30 raw dmg on 20 HP player never nerfed below 30", neverNerfVanilla, 30.0, 0.01);

        System.out.println("ALL " + passed + " UNIT ASSERTIONS PASSED!");
    }
}
