# DMZ Apothic & Vanilla Scaling (`dmzscaling`) — v1.1.0

**CurseForge:** https://www.curseforge.com/minecraft/mc-mods/dmz-apothic-vanilla-scaling

A lightweight, configurable compatibility and progression bridge for **Dragon Mine Z** (`1.20.1` Forge) that works **with or without Apotheosis**:

1. **Configurable Apotheosis Boss & Miniboss Filter (`[ApotheosisBossFilter]`)**:
   - Filters non-DragonMineZ Apotheosis world invaders (`minecraft:zombie`, `skeleton`, `blaze`, `enderman`, etc.) out of `BossRegistry` and `MinibossRegistry`, keeping `allowedBossNamespaces` (`dragonminez`, `dmzultra`, `dmzrevamp`) and `preservedBossDimensions` (`twilightforest:twilight_forest`, etc.).
   - **26 Bundled Dragon Mine Z Apotheosis Invaders (`data/apotheosis/bosses/dmz_extra/`)**:
     - **Overworld (10):** Bandit, Red Ribbon Soldier, Red Ribbon Robots 1–3, Evil Clones (Krillin, Piccolo, Nappa, Goku, Vegeta).
     - **Namek (`dragonminez:namek`, 8):** Frieza Soldier, Cui, Dodoria, Zarbon, Transformed Zarbon, Recoome, Burter, Jeice.
     - **Nether & Demon Realm (`minecraft:the_nether`, `dragonminez:demon_realm`, 4):** Dr. Gero, Android 19, Cell Jr., Dabura.
     - **The End & Otherworld (`minecraft:the_end`, `dragonminez:otherworld`, 4):** Cooler, Fifth Form Cooler, Full Power Bojack, Hirudegarn.
   - **Empty-Dimension Safety Fallback (`preserveDimensionIfEmpty = true`)**: Never leaves a dimension with 0 bosses if custom packs disable extra bosses without `apothicdmz`.
   - **Optional Apotheosis Dependency**: Uses `DmzScalingMixinPlugin` (`IMixinConfigPlugin`) so the mod runs standalone with just DragonMineZ when Apotheosis is not installed.

2. **Two-Stage DMZ Defense-Aware Hybrid Scaling (`[VanillaMobScaling]`, `[VanillaMinibossScaling]`, `[VanillaBossScaling]`)**:
   - **Stage 1 (`LivingHurtEvent` @ `HIGHEST`)**: Scales raw outgoing mob/miniboss/boss damage *before* DragonMineZ's `CombatEvent.onLivingHurt` (`HIGH`), so DMZ **Defense (`DEF`), blocking, parry, speed dodge, and evasion** work naturally against scaled bosses.
   - **Stage 2 (`LivingDamageEvent` @ `HIGH`)**: Enforces the anti-oneshot soft cap (`maxHitPercentOfPlayerHp`) and a separate continuous DoT cap (`maxDotHitPercentOfPlayerHp` for Dragon Breath `AreaEffectCloud`, Wither effect, fire/magic clouds) *after* DMZ Defense reduction.
   - **Entity Blacklist & Modded Boss Presets**: Blacklists `minecraft:silverfish` and `minecraft:endermite` by default, supports `extraMobNamespaces`, and includes zero-dependency presets for **Iron's Spellbooks**, **Alex's Caves**, **Twilight Forest**, and **Ice & Fire** (plus an opt-in toggle for **L_Ender's Cataclysm**).

3. **Party Co-op Scaling, Lazy Combat Rescaling & Scaled Boss Mechanics (`[PartyAndCombatScaling]`)**:
   - Combines nearby party members' untransformed DMZ BASE power (`STRONGEST`, `AVERAGE`, or `NEAREST`) and adds `+35%` bonus HP per extra co-op player (`maxPartyHpBonusMultiplier = 2.5`).
   - **Lazy Combat Rescaling**: Bosses/minibosses loaded in distant chunks before player stats finish loading automatically re-scale when players enter combat range.
   - **Scaled End Crystal Healing & Wither Regeneration**: End Crystals and Wither passive regeneration scale proportionally with the boss's HP multiplier so boss mechanics stay meaningful at high DMZ levels.
   - **In-Game Commands**:
     - `/dmzscaling status` — displays your live DMZ BASE power and calculated multipliers for Mobs, Warden, Ender Dragon, and Wither.
     - `/dmzscaling bosses` — displays surviving Apotheosis `BossRegistry` counts per dimension.
     - `/dmzscaling rescale` *(OP level 2)* — forces immediate re-scaling of all bosses/mini-bosses within 128 blocks.

## Build & Self-Test

```bash
python build_and_test.py
```
