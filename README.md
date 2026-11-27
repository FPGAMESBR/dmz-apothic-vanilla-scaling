# DMZ Apothic & Vanilla Scaling

A Minecraft **Forge 1.20.1** companion mod (`modId: dmzscaling`) for **Dragon Mine Z** and **Apotheosis** that aligns Apotheosis boss invasions and vanilla combat progression with Dragon Mine Z power scaling.

## Features

1. **Dragon Mine Z Apotheosis Invaders (`BossRegistryMixin` & `BossEventsMixin`):**
   - Filters Apotheosis Overworld, Nether, and End invaders to Dragon Mine Z enemies, preserving **Apothic DMZ's 23 Saga bosses** and optionally keeping **Twilight Forest** dimension bosses intact.
   - Bundles **10 additional Dragon Mine Z Overworld boss definitions** (`data/apotheosis/bosses/dmz_extra/`):
     - *Uncommon:* `bandit`, `red_ribbon_soldier`
     - *Uncommon–Rare:* `robot1`, `robot2`, `robot3`
     - *Rare:* `evil_clone_krillin`, `evil_clone_piccolo`, `evil_clone_nappa`, `evil_clone_goku`, `evil_clone_vegeta`
2. **Hybrid Dragon Mine Z BASE-Power Scaling for Vanilla Mobs (`VanillaScalingEvents`):**
   - Scales vanilla hostile mobs, mini-bosses (**Warden**, **Elder Guardian**), and main bosses (**Ender Dragon**, **Wither**) based on nearby players' Dragon Mine Z **BASE power**, preventing vanilla encounters from becoming trivial as players progress.
   - Fully configurable via `config/dmz_apothic_vanilla_scaling-common.toml`.

## Requirements

- **Minecraft:** `1.20.1`
- **Forge:** `47+`
- **Dragon Mine Z:** `2.1.2+`
- **Apotheosis:** `7.0.0+`

## Building from Source

Requires **JDK 17** and **Python 3.10+**, plus a local Forge 1.20.1 libraries folder and the companion mod JARs (`dragonminez`, `Apotheosis`, `Placebo`):

```bash
# Optional overrides (defaults to ~/curseforge/minecraft/Install/libraries and JDK 17 on PATH/JAVA_HOME)
set CF_LIBRARIES=path/to/forge/libraries
set MODS_DIR=path/to/mods_folder
python build_and_test.py
```

The compiled JAR is output to `build/libs/dmz_apothic_vanilla_scaling-1.0.0.jar`, followed by Mixin descriptor verification, JSON schema validation, and self-tests (`DmzScalingSelfTest`).

## License

Licensed under the [MIT License](LICENSE).
