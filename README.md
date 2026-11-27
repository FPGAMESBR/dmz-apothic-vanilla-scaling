# DMZ Apothic & Vanilla Scaling

[![CurseForge](https://img.shields.io/badge/CurseForge-Download-F16436?style=for-the-badge&logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/dmz-apothic-vanilla-scaling)
[![Minecraft 1.20.1](https://img.shields.io/badge/Minecraft-1.20.1-62B47A?style=for-the-badge)](https://www.curseforge.com/minecraft/mc-mods/dmz-apothic-vanilla-scaling)
[![Forge 47+](https://img.shields.io/badge/Loader-Forge%2047%2B-1E3A5F?style=for-the-badge)](https://files.minecraftforge.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

> **Official Download (CurseForge):** https://www.curseforge.com/minecraft/mc-mods/dmz-apothic-vanilla-scaling

A Minecraft **Forge 1.20.1** companion mod (`modId: dmzscaling`) for **Dragon Mine Z** and **Apotheosis** that aligns Apotheosis boss invasions and vanilla combat progression with Dragon Mine Z's power curve.

---

## Why This Mod Exists

When combining **Dragon Mine Z** with **Apotheosis** in a modpack, two progression problems show up quickly:
1. Random vanilla Apotheosis invaders (zombies, skeletons, vindicators, witches, blazes, endermen) and vanilla minibosses clash thematically with Dragon Ball progression and clutter the Overworld/Nether/End boss pool.
2. As players train their Dragon Mine Z attributes and reach thousands of HP and attack damage, vanilla hostile mobs, mini-bosses (Warden, Elder Guardian), and iconic bosses (**Ender Dragon**, **Wither**) become trivial 1-hit kills.

**DMZ Apothic & Vanilla Scaling** fixes both cleanly through Mixin filtering, bundled DMZ boss datapacks, and dynamic **untransformed BASE-power** scaling so transforming (Super Saiyan, Potential Unleashed, etc.) remains genuinely rewarding.

---

## Features

### 1. Dragon Mine Z Apotheosis Invader Filter (`BossRegistryMixin` & `BossEventsMixin`)
- Filters `BossRegistry` so Overworld, Nether, and End Apotheosis invaders consist strictly of **Dragon Mine Z** enemies, preserving **Apothic DMZ's 23 Saga bosses**.
- Optionally preserves native **Twilight Forest** Apotheosis bosses inside `twilightforest:twilight_forest`.
- Optionally disables vanilla Apotheosis minibosses (Craig, Undead Knight, Honeyed Archer, Fast Enderman, Withering Archer) outside Twilight Forest.
- Bundles **10 additional Dragon Mine Z Overworld Apotheosis boss definitions** in `data/apotheosis/bosses/dmz_extra/`:
  - **Uncommon:** `dragonminez:bandit`, `dragonminez:red_ribbon_soldier`
  - **Uncommon – Rare:** `dragonminez:robot1`, `dragonminez:robot2`, `dragonminez:robot3`
  - **Rare:** `dragonminez:evil_clone_krillin`, `dragonminez:evil_clone_piccolo`, `dragonminez:evil_clone_nappa`, `dragonminez:evil_clone_goku`, `dragonminez:evil_clone_vegeta` *(includes `dragonminez:ki_blast_damage` scaling)*

### 2. Three-Tier Vanilla Combat Scaling (`VanillaScalingEvents` & `DmzScalingHelpers`)
- **Standard Vanilla Hostile Mobs:** Gentle, strictly capped scaling based on the nearest player's Dragon Mine Z level (HP up to `6.0×`, damage up to `3.5×`, bonus armor up to `+6`).
- **Vanilla Mini-Bosses** *(Warden, Elder Guardian, Ravager, Evoker, Piglin Brute)*: Dynamic scaling anchored to the nearby player's **untransformed DMZ BASE offense and max HP** (`targetBasePowerHits = 20`), covering special attacks such as Warden sonic booms and Guardian beams, with a single-hit damage soft cap (`22%` of player max HP) to prevent unfair one-shots.
- **Vanilla Main Bosses** *(Ender Dragon, Wither)*: Dynamic DMZ BASE-power scaling (`targetBasePowerHits = 45`, HP up to `250×`, damage up to `25×`), including Wither post-charge full heal, Dragon breath/fireball/charge, Wither skulls/explosions, and a `28%` single-hit soft cap.

---

## Configuration (`config/dmz_apothic_vanilla_scaling-common.toml`)

### `[ApotheosisBossFilter]`
| Option | Default | Description |
|---|---:|---|
| `restrictBossesToDmz` | `true` | Removes non-DragonMineZ bosses from Apotheosis `BossRegistry` |
| `keepTwilightForestBosses` | `true` | Keeps Twilight Forest's native Apotheosis bosses in its dimension |
| `enableExtraDmzOverworldBosses` | `true` | Enables the 10 bundled DMZ Overworld boss definitions |
| `disableVanillaApotheosisMinibosses` | `true` | Prevents vanilla Apotheosis minibosses outside Twilight Forest |

### `[VanillaMobScaling]`
| Option | Default | Description |
|---|---:|---|
| `enabled` | `true` | Enables light capped scaling for normal `minecraft:*` hostile mobs |
| `baseHpMultiplier` / `hpMultiplierPerDmzLevel` / `maxHpMultiplier` | `1.5` / `0.015` / `6.0` | Linear HP scaling by DMZ level up to hard cap |
| `baseDamageMultiplier` / `damageMultiplierPerDmzLevel` / `maxDamageMultiplier` | `1.25` / `0.008` / `3.5` | Linear attack/projectile damage scaling up to hard cap |
| `maxBonusArmor` / `playerScanRange` | `6.0` / `96.0` | Max flat bonus armor and nearest-player scan radius (blocks) |

### `[VanillaMinibossScaling]` & `[VanillaBossScaling]`
| Option | Mini-Boss Default | Main Boss Default | Description |
|---|---:|---:|---|
| `enabled` | `true` | `true` | Enables dynamic DMZ BASE-power scaling |
| `minHpMultiplier` / `maxHpMultiplier` | `2.5` / `40.0` | `5.0` / `250.0` | Min/max HP multiplier bounds |
| `targetBasePowerHits` | `20.0` | `45.0` | Target untransformed DMZ BASE hits to defeat |
| `minDamageMultiplier` / `maxDamageMultiplier` | `1.5` / `12.0` | `2.0` / `25.0` | Min/max attack damage multiplier bounds |
| `maxHitPercentOfPlayerHp` | `0.22` | `0.28` | Soft cap per hit as fraction of victim's max HP (`0` disables) |
| `bonusArmor` / `bonusToughness` | `8.0` / `4.0` | `14.0` / `8.0` | Flat bonus armor and armor toughness |
| `playerScanRange` *(main bosses)* | — | `256.0` | Nearest-player scan radius (falls back to any player in dimension) |

---

## Requirements & Compatibility

- **Minecraft:** `1.20.1`
- **Mod Loader:** Forge `47+`
- **Required Mods:** Dragon Mine Z (`2.1.2+`), Apotheosis (`7.0.0+`)
- **Recommended Companion:** Apothic DMZ (for the 23 Saga bosses)
- **Side:** Server & Client (required on Server / Singleplayer; safe to install on both)

---

## Building from Source

Requires **JDK 17** and **Python 3.10+**, plus a local Forge 1.20.1 libraries folder and the companion mod JARs (`dragonminez`, `Apotheosis`, `Placebo`):

```bash
# Optional overrides (defaults to ~/curseforge/minecraft/Install/libraries and JDK 17 on PATH/JAVA_HOME)
set CF_LIBRARIES=path/to/forge/libraries
set MODS_DIR=path/to/mods_folder
python build_and_test.py
```

Outputs `build/libs/dmz_apothic_vanilla_scaling-1.0.0.jar`, verifies Mixin descriptors against Apotheosis and all 10 bundled entity IDs against `DragonMineZ`, and runs `DmzScalingSelfTest` (35 assertions).

---

## Related Dragon Mine Z Companion Mods

| Mod | CurseForge | GitHub |
|---|---|---|
| **DMZCA Perf Fix** | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dmzca-perf-fix) | [GitHub](https://github.com/FPGAMESBR/dmzca-perf-fix) |
| **DMZ Ultra WorldGen Fix** | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dmz-ultra-worldgen-fix) | [GitHub](https://github.com/FPGAMESBR/dmz-ultra-worldgen-fix) |
| **DMZ Spellbooks Regen Fix** | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dmz-spellbooks-regen-fix) | [GitHub](https://github.com/FPGAMESBR/dmz-spellbooks-regen-fix) |
| **DMZ Apothic & Vanilla Scaling** | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dmz-apothic-vanilla-scaling) | [GitHub](https://github.com/FPGAMESBR/dmz-apothic-vanilla-scaling) |

## License

Licensed under the [MIT License](LICENSE).
