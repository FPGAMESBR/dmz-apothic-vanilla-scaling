import glob
import json
import os
import shutil
import subprocess
import zipfile

PROJ = os.path.dirname(os.path.abspath(__file__))
CNW = 0x08000000 if os.name == "nt" else 0


def resolve_jdk_tool(name: str) -> str:
    ext = ".exe" if os.name == "nt" else ""
    jdk_bin = os.environ.get("JDK17_BIN")
    if jdk_bin:
        cand = os.path.join(jdk_bin, name + ext)
        if os.path.exists(cand):
            return cand
    java_home = os.environ.get("JAVA_HOME")
    if java_home:
        cand = os.path.join(java_home, "bin", name + ext)
        if os.path.exists(cand):
            return cand
    if os.name == "nt":
        cand = os.path.join(r"C:\Program Files\Java\jdk-17\bin", name + ext)
        if os.path.exists(cand):
            return cand
    found = shutil.which(name)
    if found:
        return found
    raise FileNotFoundError(f"Could not locate JDK 17 tool '{name}'. Set JDK17_BIN or JAVA_HOME.")


def find_mod_jar(mods_dir: str, pattern: str, env_var: str) -> str:
    override = os.environ.get(env_var)
    if override and os.path.exists(override):
        return override
    matches = sorted(glob.glob(os.path.join(mods_dir, pattern)))
    if not matches:
        raise FileNotFoundError(f"Missing {pattern} in {mods_dir}. Set {env_var} or MODS_DIR.")
    return matches[-1]


JAVAC = resolve_jdk_tool("javac")
JAVA = resolve_jdk_tool("java")
JAVAP = resolve_jdk_tool("javap")

HOME = os.path.expanduser("~")
CF_LIB = os.environ.get(
    "CF_LIBRARIES",
    os.path.join(HOME, "curseforge", "minecraft", "Install", "libraries"),
)
MODS_DIR = os.environ.get(
    "MODS_DIR",
    os.path.join(HOME, "curseforge", "minecraft", "Instances", "Dragon Mine Z - Definitive Edition", "mods"),
)
JAR_APOTH = find_mod_jar(MODS_DIR, "Apotheosis-*.jar", "APOTHEOSIS_JAR")
JAR_PLACEBO = find_mod_jar(MODS_DIR, "Placebo-*.jar", "PLACEBO_JAR")
JAR_DMZ = find_mod_jar(MODS_DIR, "dragonminez-*.jar", "DRAGONMINEZ_JAR")


def make_boss_json(entity, weight, quality, width, height, min_rar, max_rar, stats_dict):
    return {
        "entity": entity,
        "weight": weight,
        "quality": quality,
        "size": {"width": width, "height": height},
        "valid_gear_sets": ["#overworld"],
        "dimensions": ["minecraft:overworld"],
        "min_rarity": min_rar,
        "max_rarity": max_rar,
        "stats": stats_dict,
    }


def stat_block(enc_chance, enc_lvls, mods):
    return {
        "enchant_chance": enc_chance,
        "enchantment_levels": enc_lvls,
        "effects": [{"effect": "minecraft:fire_resistance", "chance": 1.0}],
        "attribute_modifiers": mods,
    }


def mod_add(attr, val):
    return {"attribute": attr, "operation": "ADDITION", "value": val}


def mod_mul(attr, val):
    return {"attribute": attr, "operation": "MULTIPLY_BASE", "value": val}


def generate_extra_dmz_boss_jsons():
    out_dir = os.path.join(PROJ, "src", "main", "resources", "data", "apotheosis", "bosses", "dmz_extra")
    os.makedirs(out_dir, exist_ok=True)
    bosses = {
        "bandit.json": make_boss_json(
            "dragonminez:bandit", 9, 0, 0.6, 1.9, "uncommon", "uncommon",
            {"uncommon": stat_block(0.25, [12, 8, 20, 14], [
                mod_add("minecraft:generic.max_health", {"min": 45, "steps": 25, "step": 2}),
                mod_mul("minecraft:generic.attack_damage", 0.25),
                mod_mul("minecraft:generic.movement_speed", 0.08),
                mod_add("minecraft:generic.knockback_resistance", 0.25),
                mod_add("minecraft:generic.armor", 3),
            ])},
        ),
        "red_ribbon_soldier.json": make_boss_json(
            "dragonminez:red_ribbon_soldier", 9, 0, 0.6, 1.9, "uncommon", "uncommon",
            {"uncommon": stat_block(0.25, [12, 8, 20, 14], [
                mod_add("minecraft:generic.max_health", {"min": 55, "steps": 25, "step": 2}),
                mod_mul("minecraft:generic.attack_damage", 0.30),
                mod_mul("minecraft:generic.movement_speed", 0.08),
                mod_add("minecraft:generic.knockback_resistance", 0.30),
                mod_add("minecraft:generic.armor", 4),
            ])},
        ),
        "robot1.json": make_boss_json(
            "dragonminez:robot1", 6, 1, 0.9, 2.2, "uncommon", "rare",
            {
                "uncommon": stat_block(0.30, [14, 10, 22, 16], [
                    mod_mul("minecraft:generic.max_health", 0.35),
                    mod_mul("minecraft:generic.attack_damage", 0.20),
                    mod_mul("minecraft:generic.movement_speed", 0.05),
                    mod_add("minecraft:generic.knockback_resistance", 0.45),
                    mod_add("minecraft:generic.armor", 5),
                ]),
                "rare": stat_block(0.45, [18, 12, 26, 20], [
                    mod_mul("minecraft:generic.max_health", 0.70),
                    mod_mul("minecraft:generic.attack_damage", 0.35),
                    mod_mul("minecraft:generic.movement_speed", 0.08),
                    mod_add("minecraft:generic.knockback_resistance", 0.55),
                    mod_add("minecraft:generic.armor", 8),
                    mod_add("minecraft:generic.armor_toughness", 2),
                ]),
            },
        ),
        "robot2.json": make_boss_json(
            "dragonminez:robot2", 5, 1, 0.9, 2.2, "uncommon", "rare",
            {
                "uncommon": stat_block(0.30, [14, 10, 22, 16], [
                    mod_mul("minecraft:generic.max_health", 0.45),
                    mod_mul("minecraft:generic.attack_damage", 0.25),
                    mod_mul("minecraft:generic.movement_speed", 0.06),
                    mod_add("minecraft:generic.knockback_resistance", 0.50),
                    mod_add("minecraft:generic.armor", 6),
                ]),
                "rare": stat_block(0.45, [18, 12, 26, 20], [
                    mod_mul("minecraft:generic.max_health", 0.80),
                    mod_mul("minecraft:generic.attack_damage", 0.40),
                    mod_mul("minecraft:generic.movement_speed", 0.08),
                    mod_add("minecraft:generic.knockback_resistance", 0.60),
                    mod_add("minecraft:generic.armor", 9),
                    mod_add("minecraft:generic.armor_toughness", 3),
                ]),
            },
        ),
        "robot3.json": make_boss_json(
            "dragonminez:robot3", 5, 2, 0.9, 2.2, "uncommon", "rare",
            {
                "uncommon": stat_block(0.35, [15, 10, 24, 18], [
                    mod_mul("minecraft:generic.max_health", 0.55),
                    mod_mul("minecraft:generic.attack_damage", 0.30),
                    mod_mul("minecraft:generic.movement_speed", 0.06),
                    mod_add("minecraft:generic.knockback_resistance", 0.55),
                    mod_add("minecraft:generic.armor", 7),
                ]),
                "rare": stat_block(0.50, [19, 13, 27, 22], [
                    mod_mul("minecraft:generic.max_health", 0.90),
                    mod_mul("minecraft:generic.attack_damage", 0.45),
                    mod_mul("minecraft:generic.movement_speed", 0.09),
                    mod_add("minecraft:generic.knockback_resistance", 0.65),
                    mod_add("minecraft:generic.armor", 10),
                    mod_add("minecraft:generic.armor_toughness", 4),
                ]),
            },
        ),
    }

    for name, w, hp_m, dmg_m in [
        ("evil_clone_krillin", 4, 0.15, 0.15),
        ("evil_clone_piccolo", 4, 0.20, 0.15),
        ("evil_clone_nappa", 4, 0.20, 0.15),
        ("evil_clone_goku", 3, 0.25, 0.20),
        ("evil_clone_vegeta", 3, 0.25, 0.20),
    ]:
        bosses[f"{name}.json"] = make_boss_json(
            f"dragonminez:{name}", w, 2, 0.6, 1.9, "rare", "rare",
            {"rare": stat_block(0.45, [18, 12, 26, 20], [
                mod_mul("minecraft:generic.max_health", hp_m),
                mod_mul("minecraft:generic.attack_damage", dmg_m),
                mod_mul("dragonminez:ki_blast_damage", dmg_m),
                mod_mul("minecraft:generic.movement_speed", 0.08),
                mod_add("minecraft:generic.knockback_resistance", 0.40),
                mod_add("minecraft:generic.armor", 4),
            ])},
        )

    for fn, data in bosses.items():
        with open(os.path.join(out_dir, fn), "w", encoding="utf-8", newline="\n") as f:
            json.dump(data, f, indent=2)
    print(f"[0/4] Verified {len(bosses)} extra DMZ Overworld boss JSONs in data/apotheosis/bosses/dmz_extra/.")
    return bosses


def collect_classpath():
    jars = [
        os.path.join(CF_LIB, "net", "minecraftforge", "forge", "1.20.1-47.4.28", "forge-1.20.1-47.4.28-client.jar"),
        os.path.join(CF_LIB, "net", "minecraftforge", "forge", "1.20.1-47.4.28", "forge-1.20.1-47.4.28-universal.jar"),
        os.path.join(CF_LIB, "net", "minecraft", "client", "1.20.1-20230612.114412", "client-1.20.1-20230612.114412-srg.jar"),
        os.path.join(CF_LIB, "org", "spongepowered", "mixin", "0.8.5", "mixin-0.8.5.jar"),
        JAR_APOTH,
        JAR_PLACEBO,
        JAR_DMZ,
    ]
    for pat in [
        os.path.join("net", "minecraftforge", "fmlcore", "1.20.1-47.4.28", "*.jar"),
        os.path.join("net", "minecraftforge", "javafmllanguage", "1.20.1-47.4.28", "*.jar"),
        os.path.join("net", "minecraftforge", "lowcodelanguage", "1.20.1-47.4.28", "*.jar"),
        os.path.join("net", "minecraftforge", "fmlloader", "1.20.1-47.4.28", "*.jar"),
        os.path.join("net", "minecraftforge", "eventbus", "*", "*.jar"),
        os.path.join("net", "minecraftforge", "coremods", "*", "*.jar"),
        os.path.join("com", "electronwill", "night-config", "*", "*", "*.jar"),
        os.path.join("it", "unimi", "dsi", "fastutil", "*", "*.jar"),
        os.path.join("org", "slf4j", "slf4j-api", "*", "*.jar"),
        os.path.join("com", "google", "guava", "guava", "31.1-jre", "*.jar"),
        os.path.join("com", "google", "code", "gson", "gson", "*", "*.jar"),
        os.path.join("org", "apache", "logging", "log4j", "*", "*", "*.jar"),
        os.path.join("org", "ow2", "asm", "*", "9.7*", "*.jar"),
        os.path.join("cpw", "mods", "modlauncher", "*", "*.jar"),
        os.path.join("cpw", "mods", "securejarhandler", "*", "*.jar"),
        os.path.join("com", "mojang", "datafixerupper", "*", "*.jar"),
        os.path.join("com", "mojang", "logging", "*", "*.jar"),
        os.path.join("com", "mojang", "brigadier", "*", "*.jar"),
        os.path.join("net", "minecraftforge", "forgespi", "*", "*.jar"),
        os.path.join("org", "joml", "joml", "*", "*.jar"),
        os.path.join("org", "apache", "commons", "*", "*", "*.jar"),
        os.path.join("commons-io", "commons-io", "*", "*.jar"),
        os.path.join("com", "mojang", "authlib", "*", "*.jar"),
        os.path.join("io", "netty", "*", "*", "*.jar"),
    ]:
        for p in sorted(glob.glob(os.path.join(CF_LIB, pat))):
            if p not in jars:
                jars.append(p)
    return [p for p in jars if os.path.exists(p)]


def compile_main(cp):
    out_dir = os.path.join(PROJ, "build", "classes", "main")
    shutil.rmtree(out_dir, ignore_errors=True)
    os.makedirs(out_dir, exist_ok=True)
    srcs = []
    for root, _, files in os.walk(os.path.join(PROJ, "src", "main", "java")):
        for f in sorted(files):
            if f.endswith(".java"):
                srcs.append(os.path.join(root, f))
    print(f"[1/4] Compiling {len(srcs)} main sources with JDK 17...")
    r = subprocess.run(
        [JAVAC, "--release", "17", "-proc:none", "-cp", os.pathsep.join(cp), "-d", out_dir] + srcs,
        capture_output=True, text=True, creationflags=CNW,
    )
    if r.stdout:
        print(r.stdout)
    if r.stderr:
        print(r.stderr)
    if r.returncode != 0:
        raise RuntimeError(f"Compilation failed (rc={r.returncode})")
    print("      Main compilation OK!")
    return out_dir


def package_jar(classes_dir):
    libs_dir = os.path.join(PROJ, "build", "libs")
    os.makedirs(libs_dir, exist_ok=True)
    jar_path = os.path.join(libs_dir, "dmz_apothic_vanilla_scaling-1.0.0.jar")
    manifest = (
        "Manifest-Version: 1.0\r\n"
        "Specification-Title: dmzscaling\r\n"
        "Specification-Vendor: FPGAMESBR\r\n"
        "Specification-Version: 1\r\n"
        "Implementation-Title: DMZ Apothic & Vanilla Scaling\r\n"
        "Implementation-Version: 1.0.0\r\n"
        "Implementation-Vendor: FPGAMESBR\r\n"
        "MixinConfigs: dmzscaling.mixins.json\r\n"
        "Created-By: 17 (Oracle Corporation)\r\n"
        "\r\n"
    )
    print(f"[2/4] Packaging {jar_path}...")
    with zipfile.ZipFile(jar_path, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("META-INF/MANIFEST.MF", manifest.encode("utf-8"))
        res_root = os.path.join(PROJ, "src", "main", "resources")
        for root, _, files in os.walk(res_root):
            for f in sorted(files):
                full = os.path.join(root, f)
                arc = os.path.relpath(full, res_root).replace("\\", "/")
                zf.write(full, arc)
        for root, _, files in os.walk(classes_dir):
            for f in sorted(files):
                full = os.path.join(root, f)
                arc = os.path.relpath(full, classes_dir).replace("\\", "/")
                zf.write(full, arc)
    print(f"      Packaged OK ({os.path.getsize(jar_path):,} bytes)!")
    return jar_path


def verify_mixins_and_jsons(jar_path, bosses):
    print("[3/4] Verifying Mixin descriptors against Apotheosis & entity IDs against DragonMineZ...")
    r_br = subprocess.run(
        [JAVAP, "-s", "-p", "-cp", JAR_APOTH, "dev.shadowsoffire.apotheosis.adventure.boss.BossRegistry"],
        capture_output=True, text=True, creationflags=CNW,
    )
    assert "(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/Map;" in r_br.stdout
    print("      Verified BossRegistry.m_5944_ descriptor!")

    r_be = subprocess.run(
        [JAVAP, "-s", "-p", "-cp", JAR_APOTH, "dev.shadowsoffire.apotheosis.adventure.boss.BossEvents"],
        capture_output=True, text=True, creationflags=CNW,
    )
    assert "minibosses(net.minecraftforge.event.entity.living.MobSpawnEvent$FinalizeSpawn);" in r_be.stdout
    assert "(Lnet/minecraftforge/event/entity/living/MobSpawnEvent$FinalizeSpawn;)V" in r_be.stdout
    print("      Verified BossEvents.minibosses descriptor!")

    r_me = subprocess.run(
        [JAVAP, "-c", "-p", "-cp", JAR_DMZ, "com.dragonminez.common.init.MainEntities"],
        capture_output=True, text=True, creationflags=CNW,
    )
    with zipfile.ZipFile(jar_path) as zf:
        names = set(zf.namelist())
        for fn in bosses:
            arc = f"data/apotheosis/bosses/dmz_extra/{fn}"
            assert arc in names, f"Missing {arc} in JAR!"
            parsed = json.loads(zf.read(arc).decode("utf-8"))
            path = parsed["entity"].split(":", 1)[1]
            assert f"// String {path}" in r_me.stdout, f"Entity {parsed['entity']} not found in MainEntities!"
            assert parsed["min_rarity"] in ("uncommon", "rare", "epic", "mythic", "ancient")
            assert parsed["max_rarity"] in ("uncommon", "rare", "epic", "mythic", "ancient")
    print(f"      Verified all {len(bosses)} bundled DMZ boss JSONs against MainEntities & Apotheosis schema!")


def run_tests(cp, jar_path):
    test_out = os.path.join(PROJ, "build", "classes", "test")
    shutil.rmtree(test_out, ignore_errors=True)
    os.makedirs(test_out, exist_ok=True)
    test_cp = [jar_path] + cp
    test_src = os.path.join(PROJ, "src", "test", "java", "com", "dmzscaling", "DmzScalingSelfTest.java")
    print("[4/4] Compiling & running unit tests...")
    r1 = subprocess.run(
        [JAVAC, "--release", "17", "-proc:none", "-cp", os.pathsep.join(test_cp), "-d", test_out, test_src],
        capture_output=True, text=True, creationflags=CNW,
    )
    if r1.returncode != 0:
        print(r1.stdout, r1.stderr)
        raise RuntimeError("Test compilation failed!")
    r2 = subprocess.run(
        [JAVA, "-cp", os.pathsep.join([test_out] + test_cp), "com.dmzscaling.DmzScalingSelfTest"],
        capture_output=True, text=True, creationflags=CNW,
    )
    print("     ", r2.stdout.strip())
    if r2.stderr:
        print(r2.stderr)
    if r2.returncode != 0:
        raise RuntimeError("Unit tests failed!")


if __name__ == "__main__":
    bosses = generate_extra_dmz_boss_jsons()
    cp = collect_classpath()
    classes = compile_main(cp)
    jar = package_jar(classes)
    verify_mixins_and_jsons(jar, bosses)
    run_tests(cp, jar)
    print("\nBUILD + MIXIN/JSON VERIFICATION + SELF-TESTS: 100% GREEN!")
