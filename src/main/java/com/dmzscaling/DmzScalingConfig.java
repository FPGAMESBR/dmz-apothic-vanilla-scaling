package com.dmzscaling;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public final class DmzScalingConfig {
    public static final ForgeConfigSpec SPEC;

    // --- [ApotheosisBossFilter] ---
    public static final ForgeConfigSpec.BooleanValue RESTRICT_BOSSES_TO_DMZ;
    public static final ForgeConfigSpec.BooleanValue KEEP_TWILIGHT_FOREST_BOSSES;
    public static final ForgeConfigSpec.BooleanValue ENABLE_EXTRA_DMZ_OVERWORLD_BOSSES;
    public static final ForgeConfigSpec.BooleanValue DISABLE_VANILLA_MINIBOSSES;

    // --- [VanillaMobScaling] (Light scaling with cap) ---
    public static final ForgeConfigSpec.BooleanValue ENABLE_VANILLA_MOB_SCALING;
    public static final ForgeConfigSpec.DoubleValue MOB_BASE_HP_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MOB_HP_PER_DMZ_LEVEL;
    public static final ForgeConfigSpec.DoubleValue MOB_MAX_HP_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MOB_BASE_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MOB_DAMAGE_PER_DMZ_LEVEL;
    public static final ForgeConfigSpec.DoubleValue MOB_MAX_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MOB_MAX_BONUS_ARMOR;
    public static final ForgeConfigSpec.DoubleValue MOB_PLAYER_SCAN_RANGE;

    // --- [VanillaMinibossScaling] (Dynamic DMZ Base Power) ---
    public static final ForgeConfigSpec.BooleanValue ENABLE_VANILLA_MINIBOSS_SCALING;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VANILLA_MINIBOSS_IDS;
    public static final ForgeConfigSpec.DoubleValue MINIBOSS_MIN_HP_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MINIBOSS_MAX_HP_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MINIBOSS_TARGET_BASE_HITS;
    public static final ForgeConfigSpec.DoubleValue MINIBOSS_MIN_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MINIBOSS_MAX_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MINIBOSS_MAX_HIT_PERCENT_PLAYER_HP;
    public static final ForgeConfigSpec.DoubleValue MINIBOSS_BONUS_ARMOR;
    public static final ForgeConfigSpec.DoubleValue MINIBOSS_BONUS_TOUGHNESS;

    // --- [VanillaBossScaling] (Ender Dragon, Wither — Dynamic DMZ Base Power) ---
    public static final ForgeConfigSpec.BooleanValue ENABLE_VANILLA_BOSS_SCALING;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VANILLA_BOSS_IDS;
    public static final ForgeConfigSpec.DoubleValue BOSS_MIN_HP_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue BOSS_MAX_HP_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue BOSS_TARGET_BASE_HITS;
    public static final ForgeConfigSpec.DoubleValue BOSS_MIN_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue BOSS_MAX_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue BOSS_MAX_HIT_PERCENT_PLAYER_HP;
    public static final ForgeConfigSpec.DoubleValue BOSS_BONUS_ARMOR;
    public static final ForgeConfigSpec.DoubleValue BOSS_BONUS_TOUGHNESS;
    public static final ForgeConfigSpec.DoubleValue BOSS_PLAYER_SCAN_RANGE;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment(
                "Controls which Apotheosis world invaders and minibosses can spawn.",
                "Works alongside Apothic DMZ (23 Saga bosses) and bundles 10 additional DMZ Overworld enemies."
        ).push("ApotheosisBossFilter");

        RESTRICT_BOSSES_TO_DMZ = b
                .comment("When true, removes non-DragonMineZ Apotheosis bosses (vanilla zombies, skeletons, vindicators, witches, blazes, endermen, etc.) from BossRegistry.")
                .define("restrictBossesToDmz", true);

        KEEP_TWILIGHT_FOREST_BOSSES = b
                .comment("When true, keeps Twilight Forest's native Apotheosis bosses inside twilightforest:twilight_forest.")
                .define("keepTwilightForestBosses", true);

        ENABLE_EXTRA_DMZ_OVERWORLD_BOSSES = b
                .comment("When true, enables the 10 bundled Dragon Mine Z Overworld Apotheosis bosses (Bandit, Red Ribbon Soldier, Robots 1-3, Evil Clones Goku/Krillin/Nappa/Piccolo/Vegeta).")
                .define("enableExtraDmzOverworldBosses", true);

        DISABLE_VANILLA_MINIBOSSES = b
                .comment("When true, prevents vanilla Apotheosis minibosses (Craig, Undead Knight, Honeyed Archer, Fast Enderman, Withering Archer) from spawning outside Twilight Forest.")
                .define("disableVanillaApotheosisMinibosses", true);

        b.pop();

        b.comment(
                "Light, capped scaling for standard vanilla hostile mobs (minecraft:* monsters/enemies, excluding bosses & Apotheosis invaders).",
                "Scales gently with the nearest player's Dragon Mine Z level up to a strict cap."
        ).push("VanillaMobScaling");

        ENABLE_VANILLA_MOB_SCALING = b
                .comment("Enable light capped scaling for normal vanilla hostile mobs.")
                .define("enabled", true);

        MOB_BASE_HP_MULTIPLIER = b
                .comment("Minimum HP multiplier for vanilla hostile mobs at DMZ level 1.")
                .defineInRange("baseHpMultiplier", 1.5, 1.0, 50.0);

        MOB_HP_PER_DMZ_LEVEL = b
                .comment("Additional HP multiplier added per DMZ player level (e.g. 0.015 = +1.5x per 100 levels).")
                .defineInRange("hpMultiplierPerDmzLevel", 0.015, 0.0, 5.0);

        MOB_MAX_HP_MULTIPLIER = b
                .comment("Hard cap on vanilla hostile mob HP multiplier (default 6.0x -> 20 HP zombie caps at 120 HP).")
                .defineInRange("maxHpMultiplier", 6.0, 1.0, 200.0);

        MOB_BASE_DAMAGE_MULTIPLIER = b
                .comment("Minimum attack/projectile damage multiplier for vanilla hostile mobs at DMZ level 1.")
                .defineInRange("baseDamageMultiplier", 1.25, 1.0, 20.0);

        MOB_DAMAGE_PER_DMZ_LEVEL = b
                .comment("Additional damage multiplier added per DMZ player level.")
                .defineInRange("damageMultiplierPerDmzLevel", 0.008, 0.0, 2.0);

        MOB_MAX_DAMAGE_MULTIPLIER = b
                .comment("Hard cap on vanilla hostile mob damage multiplier.")
                .defineInRange("maxDamageMultiplier", 3.5, 1.0, 100.0);

        MOB_MAX_BONUS_ARMOR = b
                .comment("Maximum flat bonus armor granted to vanilla hostile mobs at high DMZ levels.")
                .defineInRange("maxBonusArmor", 6.0, 0.0, 30.0);

        MOB_PLAYER_SCAN_RANGE = b
                .comment("Radius (in blocks) to find the nearest player when scaling a spawning vanilla mob.")
                .defineInRange("playerScanRange", 96.0, 16.0, 512.0);

        b.pop();

        b.comment(
                "Dynamic Dragon Mine Z BASE-power scaling for vanilla mini-bosses (Warden, Elder Guardian, Ravager, Evoker, Piglin Brute).",
                "Uses the player's untransformed BASE offense & max health so transforming still gives a clear advantage."
        ).push("VanillaMinibossScaling");

        ENABLE_VANILLA_MINIBOSS_SCALING = b
                .comment("Enable dynamic DMZ BASE-power scaling for vanilla mini-bosses.")
                .define("enabled", true);

        VANILLA_MINIBOSS_IDS = b
                .comment("Entity IDs treated as vanilla mini-bosses.")
                .defineList("entityIds", List.of(
                        "minecraft:warden",
                        "minecraft:elder_guardian",
                        "minecraft:ravager",
                        "minecraft:evoker",
                        "minecraft:piglin_brute"
                ), o -> o instanceof String && ((String) o).contains(":"));

        MINIBOSS_MIN_HP_MULTIPLIER = b
                .comment("Minimum HP multiplier for vanilla mini-bosses.")
                .defineInRange("minHpMultiplier", 2.5, 1.0, 500.0);

        MINIBOSS_MAX_HP_MULTIPLIER = b
                .comment("Maximum HP multiplier cap for vanilla mini-bosses.")
                .defineInRange("maxHpMultiplier", 40.0, 1.0, 2000.0);

        MINIBOSS_TARGET_BASE_HITS = b
                .comment("Target number of untransformed DMZ BASE-power attacks needed to defeat a vanilla mini-boss.")
                .defineInRange("targetBasePowerHits", 20.0, 1.0, 500.0);

        MINIBOSS_MIN_DAMAGE_MULTIPLIER = b
                .comment("Minimum damage multiplier for vanilla mini-boss attacks (including Warden sonic boom & Guardian beams).")
                .defineInRange("minDamageMultiplier", 1.5, 1.0, 100.0);

        MINIBOSS_MAX_DAMAGE_MULTIPLIER = b
                .comment("Maximum damage multiplier for vanilla mini-boss attacks.")
                .defineInRange("maxDamageMultiplier", 12.0, 1.0, 200.0);

        MINIBOSS_MAX_HIT_PERCENT_PLAYER_HP = b
                .comment("Soft cap on a single scaled mini-boss hit as a fraction of the victim player's max HP (0.22 = 22%). Set 0 to disable.")
                .defineInRange("maxHitPercentOfPlayerHp", 0.22, 0.0, 1.0);

        MINIBOSS_BONUS_ARMOR = b
                .comment("Flat bonus armor granted to scaled vanilla mini-bosses.")
                .defineInRange("bonusArmor", 8.0, 0.0, 30.0);

        MINIBOSS_BONUS_TOUGHNESS = b
                .comment("Flat bonus armor toughness granted to scaled vanilla mini-bosses.")
                .defineInRange("bonusToughness", 4.0, 0.0, 20.0);

        b.pop();

        b.comment(
                "Dynamic Dragon Mine Z BASE-power scaling for vanilla main bosses (Ender Dragon & Wither).",
                "Scales Max HP, Armor, Toughness, Wither post-charge full heal, and real attack damage (Dragon charge/breath/fireball, Wither skulls/explosion)."
        ).push("VanillaBossScaling");

        ENABLE_VANILLA_BOSS_SCALING = b
                .comment("Enable dynamic DMZ BASE-power scaling for vanilla main bosses.")
                .define("enabled", true);

        VANILLA_BOSS_IDS = b
                .comment("Entity IDs treated as vanilla main bosses.")
                .defineList("entityIds", List.of(
                        "minecraft:ender_dragon",
                        "minecraft:wither"
                ), o -> o instanceof String && ((String) o).contains(":"));

        BOSS_MIN_HP_MULTIPLIER = b
                .comment("Minimum HP multiplier for vanilla main bosses (default 5.0x -> Ender Dragon >= 1000 HP, Wither >= 1500 HP).")
                .defineInRange("minHpMultiplier", 5.0, 1.0, 1000.0);

        BOSS_MAX_HP_MULTIPLIER = b
                .comment("Maximum HP multiplier cap for vanilla main bosses (default 250.0x -> Ender Dragon up to 50,000 HP, Wither up to 75,000 HP).")
                .defineInRange("maxHpMultiplier", 250.0, 1.0, 5000.0);

        BOSS_TARGET_BASE_HITS = b
                .comment("Target number of untransformed DMZ BASE-power hits needed to defeat a main vanilla boss (transforming makes it faster).")
                .defineInRange("targetBasePowerHits", 45.0, 5.0, 1000.0);

        BOSS_MIN_DAMAGE_MULTIPLIER = b
                .comment("Minimum damage multiplier for Ender Dragon & Wither attacks.")
                .defineInRange("minDamageMultiplier", 2.0, 1.0, 200.0);

        BOSS_MAX_DAMAGE_MULTIPLIER = b
                .comment("Maximum damage multiplier for Ender Dragon & Wither attacks.")
                .defineInRange("maxDamageMultiplier", 25.0, 1.0, 500.0);

        BOSS_MAX_HIT_PERCENT_PLAYER_HP = b
                .comment("Soft cap on a single scaled boss hit as a fraction of the victim player's max HP (0.28 = 28%). Prevents unfair 1-shots. Set 0 to disable.")
                .defineInRange("maxHitPercentOfPlayerHp", 0.28, 0.0, 1.0);

        BOSS_BONUS_ARMOR = b
                .comment("Flat bonus armor granted to scaled main vanilla bosses.")
                .defineInRange("bonusArmor", 14.0, 0.0, 30.0);

        BOSS_BONUS_TOUGHNESS = b
                .comment("Flat bonus armor toughness granted to scaled main vanilla bosses.")
                .defineInRange("bonusToughness", 8.0, 0.0, 20.0);

        BOSS_PLAYER_SCAN_RANGE = b
                .comment("Radius (in blocks) to find the nearest player when scaling a main boss (falls back to any player in the dimension).")
                .defineInRange("playerScanRange", 256.0, 32.0, 2048.0);

        b.pop();

        SPEC = b.build();
    }

    private DmzScalingConfig() {
    }

    public static boolean isRestrictBossesToDmz() { return safeBool(RESTRICT_BOSSES_TO_DMZ, true); }
    public static boolean isKeepTwilightForestBosses() { return safeBool(KEEP_TWILIGHT_FOREST_BOSSES, true); }
    public static boolean isEnableExtraDmzOverworldBosses() { return safeBool(ENABLE_EXTRA_DMZ_OVERWORLD_BOSSES, true); }
    public static boolean isDisableVanillaMinibosses() { return safeBool(DISABLE_VANILLA_MINIBOSSES, true); }

    public static boolean isMobScalingEnabled() { return safeBool(ENABLE_VANILLA_MOB_SCALING, true); }
    public static double getMobBaseHpMult() { return safeDouble(MOB_BASE_HP_MULTIPLIER, 1.5); }
    public static double getMobHpPerLevel() { return safeDouble(MOB_HP_PER_DMZ_LEVEL, 0.015); }
    public static double getMobMaxHpMult() { return safeDouble(MOB_MAX_HP_MULTIPLIER, 6.0); }
    public static double getMobBaseDmgMult() { return safeDouble(MOB_BASE_DAMAGE_MULTIPLIER, 1.25); }
    public static double getMobDmgPerLevel() { return safeDouble(MOB_DAMAGE_PER_DMZ_LEVEL, 0.008); }
    public static double getMobMaxDmgMult() { return safeDouble(MOB_MAX_DAMAGE_MULTIPLIER, 3.5); }
    public static double getMobMaxBonusArmor() { return safeDouble(MOB_MAX_BONUS_ARMOR, 6.0); }
    public static double getMobScanRange() { return safeDouble(MOB_PLAYER_SCAN_RANGE, 96.0); }

    public static boolean isMinibossScalingEnabled() { return safeBool(ENABLE_VANILLA_MINIBOSS_SCALING, true); }
    public static double getMinibossMinHpMult() { return safeDouble(MINIBOSS_MIN_HP_MULTIPLIER, 2.5); }
    public static double getMinibossMaxHpMult() { return safeDouble(MINIBOSS_MAX_HP_MULTIPLIER, 40.0); }
    public static double getMinibossTargetBaseHits() { return safeDouble(MINIBOSS_TARGET_BASE_HITS, 20.0); }
    public static double getMinibossMinDmgMult() { return safeDouble(MINIBOSS_MIN_DAMAGE_MULTIPLIER, 1.5); }
    public static double getMinibossMaxDmgMult() { return safeDouble(MINIBOSS_MAX_DAMAGE_MULTIPLIER, 12.0); }
    public static double getMinibossMaxHitPct() { return safeDouble(MINIBOSS_MAX_HIT_PERCENT_PLAYER_HP, 0.22); }
    public static double getMinibossBonusArmor() { return safeDouble(MINIBOSS_BONUS_ARMOR, 8.0); }
    public static double getMinibossBonusToughness() { return safeDouble(MINIBOSS_BONUS_TOUGHNESS, 4.0); }

    public static boolean isBossScalingEnabled() { return safeBool(ENABLE_VANILLA_BOSS_SCALING, true); }
    public static double getBossMinHpMult() { return safeDouble(BOSS_MIN_HP_MULTIPLIER, 5.0); }
    public static double getBossMaxHpMult() { return safeDouble(BOSS_MAX_HP_MULTIPLIER, 250.0); }
    public static double getBossTargetBaseHits() { return safeDouble(BOSS_TARGET_BASE_HITS, 45.0); }
    public static double getBossMinDmgMult() { return safeDouble(BOSS_MIN_DAMAGE_MULTIPLIER, 2.0); }
    public static double getBossMaxDmgMult() { return safeDouble(BOSS_MAX_DAMAGE_MULTIPLIER, 25.0); }
    public static double getBossMaxHitPct() { return safeDouble(BOSS_MAX_HIT_PERCENT_PLAYER_HP, 0.28); }
    public static double getBossBonusArmor() { return safeDouble(BOSS_BONUS_ARMOR, 14.0); }
    public static double getBossBonusToughness() { return safeDouble(BOSS_BONUS_TOUGHNESS, 8.0); }
    public static double getBossScanRange() { return safeDouble(BOSS_PLAYER_SCAN_RANGE, 256.0); }

    public static List<? extends String> getVanillaBossIds() {
        try {
            return VANILLA_BOSS_IDS.get();
        } catch (IllegalStateException ignored) {
            return List.of("minecraft:ender_dragon", "minecraft:wither");
        }
    }

    public static List<? extends String> getVanillaMinibossIds() {
        try {
            return VANILLA_MINIBOSS_IDS.get();
        } catch (IllegalStateException ignored) {
            return List.of("minecraft:warden", "minecraft:elder_guardian", "minecraft:ravager", "minecraft:evoker", "minecraft:piglin_brute");
        }
    }

    private static boolean safeBool(ForgeConfigSpec.BooleanValue v, boolean fallback) {
        try { return v.get(); } catch (IllegalStateException ignored) { return fallback; }
    }

    private static double safeDouble(ForgeConfigSpec.DoubleValue v, double fallback) {
        try { return v.get(); } catch (IllegalStateException ignored) { return fallback; }
    }
}
