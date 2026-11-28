package com.dmzscaling;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class DmzScalingConfig {
    public static final ForgeConfigSpec SPEC;

    // --- [ApotheosisBossFilter] ---
    public static final ForgeConfigSpec.BooleanValue RESTRICT_BOSSES_TO_DMZ;
    public static final ForgeConfigSpec.BooleanValue KEEP_TWILIGHT_FOREST_BOSSES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_BOSS_NAMESPACES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> PRESERVED_BOSS_DIMENSIONS;
    public static final ForgeConfigSpec.BooleanValue PRESERVE_DIMENSION_IF_EMPTY;
    public static final ForgeConfigSpec.BooleanValue ENABLE_EXTRA_DMZ_OVERWORLD_BOSSES;
    public static final ForgeConfigSpec.BooleanValue ENABLE_EXTRA_DMZ_NAMEK_BOSSES;
    public static final ForgeConfigSpec.BooleanValue ENABLE_EXTRA_DMZ_NETHER_AND_DEMON_BOSSES;
    public static final ForgeConfigSpec.BooleanValue ENABLE_EXTRA_DMZ_END_AND_OTHERWORLD_BOSSES;
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
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITY_BLACKLIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXTRA_MOB_NAMESPACES;

    // --- [VanillaMinibossScaling] (Dynamic DMZ Base Power) ---
    public static final ForgeConfigSpec.BooleanValue ENABLE_VANILLA_MINIBOSS_SCALING;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VANILLA_MINIBOSS_IDS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MODDED_MINIBOSS_PRESETS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MODDED_MINIBOSS_IDS;
    public static final ForgeConfigSpec.DoubleValue MINIBOSS_MIN_HP_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MINIBOSS_MAX_HP_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MINIBOSS_TARGET_BASE_HITS;
    public static final ForgeConfigSpec.DoubleValue MINIBOSS_MIN_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MINIBOSS_MAX_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue MINIBOSS_MAX_HIT_PERCENT_PLAYER_HP;
    public static final ForgeConfigSpec.DoubleValue MINIBOSS_MAX_DOT_HIT_PERCENT_PLAYER_HP;
    public static final ForgeConfigSpec.DoubleValue MINIBOSS_BONUS_ARMOR;
    public static final ForgeConfigSpec.DoubleValue MINIBOSS_BONUS_TOUGHNESS;

    // --- [VanillaBossScaling] (Ender Dragon, Wither & Optional Modded Bosses) ---
    public static final ForgeConfigSpec.BooleanValue ENABLE_VANILLA_BOSS_SCALING;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VANILLA_BOSS_IDS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MODDED_BOSS_PRESETS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MODDED_BOSS_IDS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_CATACLYSM_BOSS_SCALING;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CATACLYSM_BOSS_IDS;
    public static final ForgeConfigSpec.DoubleValue BOSS_MIN_HP_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue BOSS_MAX_HP_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue BOSS_TARGET_BASE_HITS;
    public static final ForgeConfigSpec.DoubleValue BOSS_MIN_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue BOSS_MAX_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue BOSS_MAX_HIT_PERCENT_PLAYER_HP;
    public static final ForgeConfigSpec.DoubleValue BOSS_MAX_DOT_HIT_PERCENT_PLAYER_HP;
    public static final ForgeConfigSpec.DoubleValue BOSS_BONUS_ARMOR;
    public static final ForgeConfigSpec.DoubleValue BOSS_BONUS_TOUGHNESS;
    public static final ForgeConfigSpec.DoubleValue BOSS_PLAYER_SCAN_RANGE;
    public static final ForgeConfigSpec.BooleanValue SCALE_ENDER_DRAGON_CRYSTAL_HEALING;
    public static final ForgeConfigSpec.DoubleValue CRYSTAL_HEAL_SCALE_FACTOR;
    public static final ForgeConfigSpec.DoubleValue MAX_CRYSTAL_HEAL_PERCENT_PER_SECOND;
    public static final ForgeConfigSpec.BooleanValue SCALE_WITHER_PASSIVE_REGEN;
    public static final ForgeConfigSpec.DoubleValue WITHER_REGEN_SCALE_FACTOR;
    public static final ForgeConfigSpec.DoubleValue MAX_WITHER_REGEN_PERCENT_PER_SECOND;

    // --- [PartyAndCombatScaling] ---
    public static final ForgeConfigSpec.ConfigValue<String> PARTY_SCALING_MODE;
    public static final ForgeConfigSpec.DoubleValue PARTY_SCAN_RANGE;
    public static final ForgeConfigSpec.DoubleValue PARTY_BONUS_HP_PER_EXTRA_PLAYER;
    public static final ForgeConfigSpec.DoubleValue MAX_PARTY_HP_BONUS_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_LAZY_COMBAT_RESCALE;
    public static final ForgeConfigSpec.BooleanValue ANNOUNCE_SCALED_BOSSES;

    // Default Lists
    public static final List<String> DEFAULT_ALLOWED_BOSS_NAMESPACES = List.of("dragonminez", "dmzultra", "dmzrevamp");
    public static final List<String> DEFAULT_PRESERVED_BOSS_DIMS = List.of("twilightforest:twilight_forest");
    public static final List<String> DEFAULT_ENTITY_BLACKLIST = List.of("minecraft:silverfish", "minecraft:endermite");
    public static final List<String> DEFAULT_EXTRA_MOB_NAMESPACES = List.of();
    public static final List<String> DEFAULT_VANILLA_MINIBOSS_IDS = List.of(
            "minecraft:warden",
            "minecraft:elder_guardian",
            "minecraft:ravager",
            "minecraft:evoker",
            "minecraft:piglin_brute"
    );
    public static final List<String> DEFAULT_MODDED_MINIBOSS_IDS = List.of(
            "irons_spellbooks:dead_king",
            "irons_spellbooks:citadel_keeper",
            "irons_spellbooks:archevoker",
            "irons_spellbooks:cryomancer",
            "irons_spellbooks:pyromancer",
            "irons_spellbooks:apothecarist",
            "alexscaves:hullbreaker",
            "alexscaves:forsaken",
            "alexscaves:tremorzilla"
    );
    public static final List<String> DEFAULT_VANILLA_BOSS_IDS = List.of(
            "minecraft:ender_dragon",
            "minecraft:wither"
    );
    public static final List<String> DEFAULT_MODDED_BOSS_IDS = List.of(
            "twilightforest:naga",
            "twilightforest:lich",
            "twilightforest:minoshroom",
            "twilightforest:hydra",
            "twilightforest:knight_phantom",
            "twilightforest:ur_ghast",
            "twilightforest:alpha_yeti",
            "twilightforest:snow_queen",
            "iceandfire:fire_dragon",
            "iceandfire:ice_dragon",
            "iceandfire:lightning_dragon",
            "alexscaves:luxtructosaurus"
    );
    public static final List<String> DEFAULT_CATACLYSM_BOSS_IDS = List.of(
            "cataclysm:ignis",
            "cataclysm:netherite_monstrosity",
            "cataclysm:ender_guardian",
            "cataclysm:the_harbinger",
            "cataclysm:the_leviathan",
            "cataclysm:ancient_remnant",
            "cataclysm:maledictus",
            "cataclysm:scylla"
    );

    // Immutable O(1) runtime caches (re-baked on ModConfigEvent)
    private static volatile Set<String> cachedAllowedBossNamespaces = null;
    private static volatile Set<String> cachedPreservedBossDimensions = null;
    private static volatile Set<String> cachedEntityBlacklist = null;
    private static volatile Set<String> cachedAllowedMobNamespaces = null;
    private static volatile Set<String> cachedMinibossIds = null;
    private static volatile Set<String> cachedBossIds = null;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.comment(
                "Controls which Apotheosis world invaders and minibosses can spawn.",
                "Works alongside Apothic DMZ (23 Saga bosses) and bundles 26 Dragon Mine Z enemies across Overworld, Namek, Nether, Demon Realm, The End, and Otherworld."
        ).push("ApotheosisBossFilter");

        RESTRICT_BOSSES_TO_DMZ = b
                .comment("When true, removes non-allowed Apotheosis bosses (vanilla zombies, skeletons, vindicators, witches, blazes, endermen, etc.) from BossRegistry.")
                .define("restrictBossesToDmz", true);

        KEEP_TWILIGHT_FOREST_BOSSES = b
                .comment("When true, preserves dimension-specific Apotheosis bosses inside dimensions listed in preservedBossDimensions.")
                .define("keepTwilightForestBosses", true);

        ALLOWED_BOSS_NAMESPACES = b
                .comment("Entity namespaces always kept as Apotheosis bosses and minibosses when restrictBossesToDmz is true.")
                .defineList("allowedBossNamespaces", DEFAULT_ALLOWED_BOSS_NAMESPACES, o -> o instanceof String && !((String) o).isBlank());

        PRESERVED_BOSS_DIMENSIONS = b
                .comment("Dimension IDs whose native Apotheosis bosses and minibosses are preserved when keepTwilightForestBosses is true (e.g. twilightforest:twilight_forest, aether:the_aether, deeperdarker:otherside).")
                .defineList("preservedBossDimensions", DEFAULT_PRESERVED_BOSS_DIMS, o -> o instanceof String && ((String) o).contains(":"));

        PRESERVE_DIMENSION_IF_EMPTY = b
                .comment("Safety fallback: if filtering would leave a dimension with 0 Apotheosis bosses (e.g. when running without Apothic DMZ and extra bosses are disabled), keep that dimension's original entries so invader spawns never break.")
                .define("preserveDimensionIfEmpty", true);

        ENABLE_EXTRA_DMZ_OVERWORLD_BOSSES = b
                .comment("When true, enables the 10 bundled Dragon Mine Z Overworld Apotheosis bosses (Bandit, Red Ribbon Soldier, Robots 1-3, Evil Clones Goku/Krillin/Nappa/Piccolo/Vegeta).")
                .define("enableExtraDmzOverworldBosses", true);

        ENABLE_EXTRA_DMZ_NAMEK_BOSSES = b
                .comment("When true, enables the 8 bundled Dragon Mine Z Namek Apotheosis bosses (Frieza Soldier, Cui, Dodoria, Zarbon, Transformed Zarbon, Recoome, Burter, Jeice) in dragonminez:namek.")
                .define("enableExtraDmzNamekBosses", true);

        ENABLE_EXTRA_DMZ_NETHER_AND_DEMON_BOSSES = b
                .comment("When true, enables the 4 bundled Dragon Mine Z Nether & Demon Realm Apotheosis bosses (Dr. Gero, Android 19, Cell Jr., Dabura) in minecraft:the_nether and dragonminez:demon_realm.")
                .define("enableExtraDmzNetherAndDemonBosses", true);

        ENABLE_EXTRA_DMZ_END_AND_OTHERWORLD_BOSSES = b
                .comment("When true, enables the 4 bundled Dragon Mine Z End & Otherworld Apotheosis bosses (Cooler, Fifth Form Cooler, Full Power Bojack, Hirudegarn) in minecraft:the_end and dragonminez:otherworld.")
                .define("enableExtraDmzEndAndOtherworldBosses", true);

        DISABLE_VANILLA_MINIBOSSES = b
                .comment("When true, prevents non-DMZ Apotheosis minibosses (Craig, Undead Knight, Honeyed Archer, Fast Enderman, Withering Archer) from loading or spawning outside preservedBossDimensions.")
                .define("disableVanillaApotheosisMinibosses", true);

        b.pop();

        b.comment(
                "Light, capped scaling for standard hostile mobs (minecraft:* and optional extraMobNamespaces, excluding bosses, mini-bosses, blacklisted mobs, and Apotheosis invaders).",
                "Scales gently with the nearest player's Dragon Mine Z level up to a strict cap."
        ).push("VanillaMobScaling");

        ENABLE_VANILLA_MOB_SCALING = b
                .comment("Enable light capped scaling for normal hostile mobs.")
                .define("enabled", true);

        MOB_BASE_HP_MULTIPLIER = b
                .comment("Minimum HP multiplier for hostile mobs at DMZ level 1.")
                .defineInRange("baseHpMultiplier", 1.5, 1.0, 50.0);

        MOB_HP_PER_DMZ_LEVEL = b
                .comment("Additional HP multiplier added per DMZ player level (e.g. 0.015 = +1.5x per 100 levels).")
                .defineInRange("hpMultiplierPerDmzLevel", 0.015, 0.0, 5.0);

        MOB_MAX_HP_MULTIPLIER = b
                .comment("Hard cap on hostile mob HP multiplier (default 6.0x -> 20 HP zombie caps at 120 HP).")
                .defineInRange("maxHpMultiplier", 6.0, 1.0, 200.0);

        MOB_BASE_DAMAGE_MULTIPLIER = b
                .comment("Minimum attack/projectile damage multiplier for hostile mobs at DMZ level 1.")
                .defineInRange("baseDamageMultiplier", 1.25, 1.0, 20.0);

        MOB_DAMAGE_PER_DMZ_LEVEL = b
                .comment("Additional damage multiplier added per DMZ player level.")
                .defineInRange("damageMultiplierPerDmzLevel", 0.008, 0.0, 2.0);

        MOB_MAX_DAMAGE_MULTIPLIER = b
                .comment("Hard cap on hostile mob damage multiplier.")
                .defineInRange("maxDamageMultiplier", 3.5, 1.0, 100.0);

        MOB_MAX_BONUS_ARMOR = b
                .comment("Maximum flat bonus armor granted to hostile mobs at high DMZ levels.")
                .defineInRange("maxBonusArmor", 6.0, 0.0, 30.0);

        MOB_PLAYER_SCAN_RANGE = b
                .comment("Radius (in blocks) to find the nearest player when scaling a spawning mob.")
                .defineInRange("playerScanRange", 96.0, 16.0, 512.0);

        ENTITY_BLACKLIST = b
                .comment("Entity IDs that must NEVER be scaled by any tier (includes minecraft:silverfish and minecraft:endermite by default).")
                .defineList("entityBlacklist", DEFAULT_ENTITY_BLACKLIST, o -> o instanceof String && ((String) o).contains(":"));

        EXTRA_MOB_NAMESPACES = b
                .comment("Optional extra mod namespaces whose normal hostile Enemy mobs should also receive light capped VanillaMobScaling (e.g. \"alexscaves\", \"irons_spellbooks\", \"twilightforest\", \"iceandfire\").")
                .defineList("extraMobNamespaces", DEFAULT_EXTRA_MOB_NAMESPACES, o -> o instanceof String && !((String) o).isBlank());

        b.pop();

        b.comment(
                "Dynamic Dragon Mine Z BASE-power scaling for vanilla & optional modded mini-bosses (Warden, Elder Guardian, Ravager, Evoker, Piglin Brute, Iron's Spellbooks Dead King/Citadel Keeper, Alex's Caves Hullbreaker/Forsaken/Tremorzilla).",
                "Uses the player's untransformed BASE offense & max health so transforming still gives a clear advantage."
        ).push("VanillaMinibossScaling");

        ENABLE_VANILLA_MINIBOSS_SCALING = b
                .comment("Enable dynamic DMZ BASE-power scaling for mini-bosses.")
                .define("enabled", true);

        VANILLA_MINIBOSS_IDS = b
                .comment("Vanilla entity IDs treated as mini-bosses.")
                .defineList("entityIds", DEFAULT_VANILLA_MINIBOSS_IDS, o -> o instanceof String && ((String) o).contains(":"));

        ENABLE_MODDED_MINIBOSS_PRESETS = b
                .comment("When true, also includes moddedMinibossIds (Iron's Spellbooks & Alex's Caves mini-bosses) with zero required mod dependencies.")
                .define("enableModdedMinibossPresets", true);

        MODDED_MINIBOSS_IDS = b
                .comment("Optional modded mini-boss entity IDs scaled when enableModdedMinibossPresets is true.")
                .defineList("moddedMinibossIds", DEFAULT_MODDED_MINIBOSS_IDS, o -> o instanceof String && ((String) o).contains(":"));

        MINIBOSS_MIN_HP_MULTIPLIER = b
                .comment("Minimum HP multiplier for mini-bosses.")
                .defineInRange("minHpMultiplier", 2.5, 1.0, 500.0);

        MINIBOSS_MAX_HP_MULTIPLIER = b
                .comment("Maximum HP multiplier cap for mini-bosses.")
                .defineInRange("maxHpMultiplier", 40.0, 1.0, 2000.0);

        MINIBOSS_TARGET_BASE_HITS = b
                .comment("Target number of untransformed DMZ BASE-power attacks needed to defeat a mini-boss.")
                .defineInRange("targetBasePowerHits", 20.0, 1.0, 500.0);

        MINIBOSS_MIN_DAMAGE_MULTIPLIER = b
                .comment("Minimum damage multiplier for mini-boss attacks (including Warden sonic boom & Guardian beams).")
                .defineInRange("minDamageMultiplier", 1.5, 1.0, 100.0);

        MINIBOSS_MAX_DAMAGE_MULTIPLIER = b
                .comment("Maximum damage multiplier for mini-boss attacks.")
                .defineInRange("maxDamageMultiplier", 12.0, 1.0, 200.0);

        MINIBOSS_MAX_HIT_PERCENT_PLAYER_HP = b
                .comment("Post-DEF soft cap on a single scaled mini-boss direct hit as a fraction of the victim player's max HP (0.22 = 22%). Set 0 to disable.")
                .defineInRange("maxHitPercentOfPlayerHp", 0.22, 0.0, 1.0);

        MINIBOSS_MAX_DOT_HIT_PERCENT_PLAYER_HP = b
                .comment("Post-DEF soft cap on continuous/DoT mini-boss damage (AreaEffectCloud, poison/magic clouds) per tick as a fraction of the victim player's max HP (0.06 = 6%).")
                .defineInRange("maxDotHitPercentOfPlayerHp", 0.06, 0.0, 1.0);

        MINIBOSS_BONUS_ARMOR = b
                .comment("Flat bonus armor granted to scaled mini-bosses.")
                .defineInRange("bonusArmor", 8.0, 0.0, 30.0);

        MINIBOSS_BONUS_TOUGHNESS = b
                .comment("Flat bonus armor toughness granted to scaled mini-bosses.")
                .defineInRange("bonusToughness", 4.0, 0.0, 20.0);

        b.pop();

        b.comment(
                "Dynamic Dragon Mine Z BASE-power scaling for main bosses (Ender Dragon, Wither, and optional Twilight Forest / Ice & Fire / Alex's Caves / Cataclysm bosses).",
                "Scales Max HP, Armor, Toughness, Wither post-charge full heal, End Crystal healing, Wither passive regen, and real attack damage."
        ).push("VanillaBossScaling");

        ENABLE_VANILLA_BOSS_SCALING = b
                .comment("Enable dynamic DMZ BASE-power scaling for main bosses.")
                .define("enabled", true);

        VANILLA_BOSS_IDS = b
                .comment("Vanilla entity IDs treated as main bosses.")
                .defineList("entityIds", DEFAULT_VANILLA_BOSS_IDS, o -> o instanceof String && ((String) o).contains(":"));

        ENABLE_MODDED_BOSS_PRESETS = b
                .comment("When true, also includes moddedBossIds (Twilight Forest, Ice & Fire dragons, Alex's Caves Luxtructosaurus) with zero required mod dependencies.")
                .define("enableModdedBossPresets", true);

        MODDED_BOSS_IDS = b
                .comment("Optional modded boss entity IDs scaled when enableModdedBossPresets is true.")
                .defineList("moddedBossIds", DEFAULT_MODDED_BOSS_IDS, o -> o instanceof String && ((String) o).contains(":"));

        ENABLE_CATACLYSM_BOSS_SCALING = b
                .comment("When true, also scales L_Ender's Cataclysm bosses listed in cataclysmBossIds (disabled by default because Cataclysm bosses already have built-in per-hit damage caps).")
                .define("enableCataclysmBossScaling", false);

        CATACLYSM_BOSS_IDS = b
                .comment("L_Ender's Cataclysm boss entity IDs scaled when enableCataclysmBossScaling is true.")
                .defineList("cataclysmBossIds", DEFAULT_CATACLYSM_BOSS_IDS, o -> o instanceof String && ((String) o).contains(":"));

        BOSS_MIN_HP_MULTIPLIER = b
                .comment("Minimum HP multiplier for main bosses (default 5.0x -> Ender Dragon >= 1000 HP, Wither >= 1500 HP).")
                .defineInRange("minHpMultiplier", 5.0, 1.0, 1000.0);

        BOSS_MAX_HP_MULTIPLIER = b
                .comment("Maximum HP multiplier cap for main bosses (default 250.0x -> Ender Dragon up to 50,000 HP, Wither up to 75,000 HP).")
                .defineInRange("maxHpMultiplier", 250.0, 1.0, 5000.0);

        BOSS_TARGET_BASE_HITS = b
                .comment("Target number of untransformed DMZ BASE-power hits needed to defeat a main boss (transforming makes it faster).")
                .defineInRange("targetBasePowerHits", 45.0, 5.0, 1000.0);

        BOSS_MIN_DAMAGE_MULTIPLIER = b
                .comment("Minimum damage multiplier for main boss attacks.")
                .defineInRange("minDamageMultiplier", 2.0, 1.0, 200.0);

        BOSS_MAX_DAMAGE_MULTIPLIER = b
                .comment("Maximum damage multiplier for main boss attacks.")
                .defineInRange("maxDamageMultiplier", 25.0, 1.0, 500.0);

        BOSS_MAX_HIT_PERCENT_PLAYER_HP = b
                .comment("Post-DEF soft cap on a single scaled boss hit as a fraction of the victim player's max HP (0.28 = 28%). Prevents unfair 1-shots. Set 0 to disable.")
                .defineInRange("maxHitPercentOfPlayerHp", 0.28, 0.0, 1.0);

        BOSS_MAX_DOT_HIT_PERCENT_PLAYER_HP = b
                .comment("Post-DEF soft cap on continuous/DoT boss damage (Ender Dragon breath AreaEffectCloud, magic clouds) per tick as a fraction of the victim player's max HP (0.08 = 8%).")
                .defineInRange("maxDotHitPercentOfPlayerHp", 0.08, 0.0, 1.0);

        BOSS_BONUS_ARMOR = b
                .comment("Flat bonus armor granted to scaled main bosses.")
                .defineInRange("bonusArmor", 14.0, 0.0, 30.0);

        BOSS_BONUS_TOUGHNESS = b
                .comment("Flat bonus armor toughness granted to scaled main bosses.")
                .defineInRange("bonusToughness", 8.0, 0.0, 20.0);

        BOSS_PLAYER_SCAN_RANGE = b
                .comment("Radius (in blocks) to find nearby players when scaling a main boss (falls back to any player in the dimension).")
                .defineInRange("playerScanRange", 256.0, 32.0, 2048.0);

        SCALE_ENDER_DRAGON_CRYSTAL_HEALING = b
                .comment("When true, scales End Crystal healing on the Ender Dragon proportionally to its HP multiplier so End Crystals stay relevant at high DMZ levels.")
                .define("scaleEnderDragonCrystalHealing", true);

        CRYSTAL_HEAL_SCALE_FACTOR = b
                .comment("Proportional factor for End Crystal bonus healing relative to the Ender Dragon's HP multiplier.")
                .defineInRange("crystalHealScaleFactor", 0.65, 0.0, 5.0);

        MAX_CRYSTAL_HEAL_PERCENT_PER_SECOND = b
                .comment("Cap on total End Crystal healing per second as a fraction of the Ender Dragon's scaled Max HP (0.012 = 1.2%/sec, matching vanilla's 2 HP/s on 200 HP).")
                .defineInRange("maxCrystalHealPercentPerSecond", 0.012, 0.001, 0.20);

        SCALE_WITHER_PASSIVE_REGEN = b
                .comment("When true, scales the Wither's post-spawn passive 1 HP/s regeneration proportionally to its HP multiplier.")
                .define("scaleWitherPassiveRegen", true);

        WITHER_REGEN_SCALE_FACTOR = b
                .comment("Proportional factor for Wither passive regeneration relative to its HP multiplier.")
                .defineInRange("witherRegenScaleFactor", 0.50, 0.0, 5.0);

        MAX_WITHER_REGEN_PERCENT_PER_SECOND = b
                .comment("Cap on Wither passive regeneration per second as a fraction of its scaled Max HP (0.005 = 0.5%/sec).")
                .defineInRange("maxWitherRegenPercentPerSecond", 0.005, 0.0005, 0.10);

        b.pop();

        b.comment(
                "Multiplayer party scaling, lazy combat re-scaling, and boss engagement announcements."
        ).push("PartyAndCombatScaling");

        PARTY_SCALING_MODE = b
                .comment("How to combine multiple nearby players' DMZ BASE power for bosses & mini-bosses: STRONGEST, AVERAGE, or NEAREST.")
                .define("partyScalingMode", "STRONGEST");

        PARTY_SCAN_RANGE = b
                .comment("Radius (in blocks) around a boss or mini-boss to count active party members for co-op scaling.")
                .defineInRange("partyScanRange", 96.0, 16.0, 512.0);

        PARTY_BONUS_HP_PER_EXTRA_PLAYER = b
                .comment("Additional HP multiplier bonus per extra nearby player beyond the first (0.35 = +35% HP per extra co-op player).")
                .defineInRange("partyBonusHpPerExtraPlayer", 0.35, 0.0, 5.0);

        MAX_PARTY_HP_BONUS_MULTIPLIER = b
                .comment("Maximum total party HP multiplier applied on top of base boss/miniboss HP (2.5 = up to 2.5x HP for large groups).")
                .defineInRange("maxPartyHpBonusMultiplier", 2.5, 1.0, 20.0);

        ENABLE_LAZY_COMBAT_RESCALE = b
                .comment("When true, bosses & mini-bosses that spawned while no loaded player was nearby automatically re-scale when a player approaches or starts combat (while still at 100% HP).")
                .define("enableLazyCombatRescale", true);

        ANNOUNCE_SCALED_BOSSES = b
                .comment("When true, displays a concise actionbar status banner to nearby players when a scaled Boss or Mini-Boss enters combat.")
                .define("announceScaledBosses", true);

        b.pop();

        SPEC = b.build();
    }

    private DmzScalingConfig() {
    }

    public static void bakeCaches() {
        cachedAllowedBossNamespaces = toImmutableLowerSet(safeList(ALLOWED_BOSS_NAMESPACES, DEFAULT_ALLOWED_BOSS_NAMESPACES));
        cachedPreservedBossDimensions = toImmutableLowerSet(safeList(PRESERVED_BOSS_DIMENSIONS, DEFAULT_PRESERVED_BOSS_DIMS));
        cachedEntityBlacklist = toImmutableLowerSet(safeList(ENTITY_BLACKLIST, DEFAULT_ENTITY_BLACKLIST));

        Set<String> mobNs = new HashSet<>();
        mobNs.add("minecraft");
        for (String ns : safeList(EXTRA_MOB_NAMESPACES, DEFAULT_EXTRA_MOB_NAMESPACES)) {
            if (ns != null && !ns.isBlank()) {
                mobNs.add(ns.trim().toLowerCase(Locale.ROOT));
            }
        }
        cachedAllowedMobNamespaces = Collections.unmodifiableSet(mobNs);

        Set<String> minibosses = new HashSet<>(toImmutableLowerSet(safeList(VANILLA_MINIBOSS_IDS, DEFAULT_VANILLA_MINIBOSS_IDS)));
        if (isEnableModdedMinibossPresets()) {
            minibosses.addAll(toImmutableLowerSet(safeList(MODDED_MINIBOSS_IDS, DEFAULT_MODDED_MINIBOSS_IDS)));
        }
        cachedMinibossIds = Collections.unmodifiableSet(minibosses);

        Set<String> bosses = new HashSet<>(toImmutableLowerSet(safeList(VANILLA_BOSS_IDS, DEFAULT_VANILLA_BOSS_IDS)));
        if (isEnableModdedBossPresets()) {
            bosses.addAll(toImmutableLowerSet(safeList(MODDED_BOSS_IDS, DEFAULT_MODDED_BOSS_IDS)));
        }
        if (isEnableCataclysmBossScaling()) {
            bosses.addAll(toImmutableLowerSet(safeList(CATACLYSM_BOSS_IDS, DEFAULT_CATACLYSM_BOSS_IDS)));
        }
        cachedBossIds = Collections.unmodifiableSet(bosses);
    }

    private static Set<String> toImmutableLowerSet(List<? extends String> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> set = new HashSet<>(list.size() * 2);
        for (String s : list) {
            if (s != null && !s.isBlank()) {
                set.add(s.trim().toLowerCase(Locale.ROOT));
            }
        }
        return Collections.unmodifiableSet(set);
    }

    public static boolean isRestrictBossesToDmz() { return safeBool(RESTRICT_BOSSES_TO_DMZ, true); }
    public static boolean isKeepTwilightForestBosses() { return safeBool(KEEP_TWILIGHT_FOREST_BOSSES, true); }
    public static boolean isPreserveDimensionIfEmpty() { return safeBool(PRESERVE_DIMENSION_IF_EMPTY, true); }
    public static boolean isEnableExtraDmzOverworldBosses() { return safeBool(ENABLE_EXTRA_DMZ_OVERWORLD_BOSSES, true); }
    public static boolean isEnableExtraDmzNamekBosses() { return safeBool(ENABLE_EXTRA_DMZ_NAMEK_BOSSES, true); }
    public static boolean isEnableExtraDmzNetherAndDemonBosses() { return safeBool(ENABLE_EXTRA_DMZ_NETHER_AND_DEMON_BOSSES, true); }
    public static boolean isEnableExtraDmzEndAndOtherworldBosses() { return safeBool(ENABLE_EXTRA_DMZ_END_AND_OTHERWORLD_BOSSES, true); }
    public static boolean isDisableVanillaMinibosses() { return safeBool(DISABLE_VANILLA_MINIBOSSES, true); }

    public static Set<String> getAllowedBossNamespacesSet() {
        Set<String> c = cachedAllowedBossNamespaces;
        if (c == null) {
            bakeCaches();
            c = cachedAllowedBossNamespaces;
        }
        return c;
    }

    public static Set<String> getPreservedBossDimensionsSet() {
        Set<String> c = cachedPreservedBossDimensions;
        if (c == null) {
            bakeCaches();
            c = cachedPreservedBossDimensions;
        }
        return c;
    }

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
    public static boolean isEnableModdedMinibossPresets() { return safeBool(ENABLE_MODDED_MINIBOSS_PRESETS, true); }
    public static double getMinibossMinHpMult() { return safeDouble(MINIBOSS_MIN_HP_MULTIPLIER, 2.5); }
    public static double getMinibossMaxHpMult() { return safeDouble(MINIBOSS_MAX_HP_MULTIPLIER, 40.0); }
    public static double getMinibossTargetBaseHits() { return safeDouble(MINIBOSS_TARGET_BASE_HITS, 20.0); }
    public static double getMinibossMinDmgMult() { return safeDouble(MINIBOSS_MIN_DAMAGE_MULTIPLIER, 1.5); }
    public static double getMinibossMaxDmgMult() { return safeDouble(MINIBOSS_MAX_DAMAGE_MULTIPLIER, 12.0); }
    public static double getMinibossMaxHitPct() { return safeDouble(MINIBOSS_MAX_HIT_PERCENT_PLAYER_HP, 0.22); }
    public static double getMinibossMaxDotHitPct() { return safeDouble(MINIBOSS_MAX_DOT_HIT_PERCENT_PLAYER_HP, 0.06); }
    public static double getMinibossBonusArmor() { return safeDouble(MINIBOSS_BONUS_ARMOR, 8.0); }
    public static double getMinibossBonusToughness() { return safeDouble(MINIBOSS_BONUS_TOUGHNESS, 4.0); }

    public static boolean isBossScalingEnabled() { return safeBool(ENABLE_VANILLA_BOSS_SCALING, true); }
    public static boolean isEnableModdedBossPresets() { return safeBool(ENABLE_MODDED_BOSS_PRESETS, true); }
    public static boolean isEnableCataclysmBossScaling() { return safeBool(ENABLE_CATACLYSM_BOSS_SCALING, false); }
    public static double getBossMinHpMult() { return safeDouble(BOSS_MIN_HP_MULTIPLIER, 5.0); }
    public static double getBossMaxHpMult() { return safeDouble(BOSS_MAX_HP_MULTIPLIER, 250.0); }
    public static double getBossTargetBaseHits() { return safeDouble(BOSS_TARGET_BASE_HITS, 45.0); }
    public static double getBossMinDmgMult() { return safeDouble(BOSS_MIN_DAMAGE_MULTIPLIER, 2.0); }
    public static double getBossMaxDmgMult() { return safeDouble(BOSS_MAX_DAMAGE_MULTIPLIER, 25.0); }
    public static double getBossMaxHitPct() { return safeDouble(BOSS_MAX_HIT_PERCENT_PLAYER_HP, 0.28); }
    public static double getBossMaxDotHitPct() { return safeDouble(BOSS_MAX_DOT_HIT_PERCENT_PLAYER_HP, 0.08); }
    public static double getBossBonusArmor() { return safeDouble(BOSS_BONUS_ARMOR, 14.0); }
    public static double getBossBonusToughness() { return safeDouble(BOSS_BONUS_TOUGHNESS, 8.0); }
    public static double getBossScanRange() { return safeDouble(BOSS_PLAYER_SCAN_RANGE, 256.0); }
    public static boolean isScaleEnderDragonCrystalHealing() { return safeBool(SCALE_ENDER_DRAGON_CRYSTAL_HEALING, true); }
    public static double getCrystalHealScaleFactor() { return safeDouble(CRYSTAL_HEAL_SCALE_FACTOR, 0.65); }
    public static double getMaxCrystalHealPercentPerSecond() { return safeDouble(MAX_CRYSTAL_HEAL_PERCENT_PER_SECOND, 0.012); }
    public static boolean isScaleWitherPassiveRegen() { return safeBool(SCALE_WITHER_PASSIVE_REGEN, true); }
    public static double getWitherRegenScaleFactor() { return safeDouble(WITHER_REGEN_SCALE_FACTOR, 0.50); }
    public static double getMaxWitherRegenPercentPerSecond() { return safeDouble(MAX_WITHER_REGEN_PERCENT_PER_SECOND, 0.005); }

    public static String getPartyScalingMode() { return safeString(PARTY_SCALING_MODE, "STRONGEST"); }
    public static double getPartyScanRange() { return safeDouble(PARTY_SCAN_RANGE, 96.0); }
    public static double getPartyBonusHpPerExtraPlayer() { return safeDouble(PARTY_BONUS_HP_PER_EXTRA_PLAYER, 0.35); }
    public static double getMaxPartyHpBonusMultiplier() { return safeDouble(MAX_PARTY_HP_BONUS_MULTIPLIER, 2.5); }
    public static boolean isEnableLazyCombatRescale() { return safeBool(ENABLE_LAZY_COMBAT_RESCALE, true); }
    public static boolean isAnnounceScaledBosses() { return safeBool(ANNOUNCE_SCALED_BOSSES, true); }

    public static boolean isBlacklistedEntity(String entityId) {
        if (entityId == null || entityId.isEmpty()) {
            return false;
        }
        Set<String> c = cachedEntityBlacklist;
        if (c == null) {
            bakeCaches();
            c = cachedEntityBlacklist;
        }
        return c.contains(entityId.toLowerCase(Locale.ROOT));
    }

    public static boolean isAllowedMobNamespace(String namespace) {
        if (namespace == null || namespace.isEmpty()) {
            return false;
        }
        Set<String> c = cachedAllowedMobNamespaces;
        if (c == null) {
            bakeCaches();
            c = cachedAllowedMobNamespaces;
        }
        return c.contains(namespace.toLowerCase(Locale.ROOT));
    }

    public static boolean isBossEntityId(String entityId) {
        if (entityId == null || entityId.isEmpty()) {
            return false;
        }
        Set<String> c = cachedBossIds;
        if (c == null) {
            bakeCaches();
            c = cachedBossIds;
        }
        return c.contains(entityId.toLowerCase(Locale.ROOT));
    }

    public static boolean isMinibossEntityId(String entityId) {
        if (entityId == null || entityId.isEmpty()) {
            return false;
        }
        Set<String> c = cachedMinibossIds;
        if (c == null) {
            bakeCaches();
            c = cachedMinibossIds;
        }
        return c.contains(entityId.toLowerCase(Locale.ROOT));
    }

    public static Set<String> getActiveBossIdsSet() {
        Set<String> c = cachedBossIds;
        if (c == null) {
            bakeCaches();
            c = cachedBossIds;
        }
        return c;
    }

    public static Set<String> getActiveMinibossIdsSet() {
        Set<String> c = cachedMinibossIds;
        if (c == null) {
            bakeCaches();
            c = cachedMinibossIds;
        }
        return c;
    }

    private static boolean safeBool(ForgeConfigSpec.BooleanValue v, boolean fallback) {
        try { return v.get(); } catch (IllegalStateException ignored) { return fallback; }
    }

    private static double safeDouble(ForgeConfigSpec.DoubleValue v, double fallback) {
        try { return v.get(); } catch (IllegalStateException ignored) { return fallback; }
    }

    private static String safeString(ForgeConfigSpec.ConfigValue<String> v, String fallback) {
        try { return v.get(); } catch (IllegalStateException ignored) { return fallback; }
    }

    private static List<? extends String> safeList(ForgeConfigSpec.ConfigValue<List<? extends String>> v, List<String> fallback) {
        try { return v.get(); } catch (IllegalStateException ignored) { return fallback; }
    }
}
