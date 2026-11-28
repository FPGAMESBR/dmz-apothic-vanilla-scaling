package com.dmzscaling.mixin;

import com.dmzscaling.DmzScalingConfig;
import com.dmzscaling.DmzScalingHelpers;
import com.dmzscaling.DmzScalingMod;
import com.google.gson.JsonElement;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.boss.BossEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Filters Apotheosis's BossRegistry during datapack prepare() so Overworld/Nether/End/DMZ
 * invaders only spawn Dragon Mine Z enemies (preserving configured dimensions and empty-dimension fallbacks)
 * and registers default BOSS_SPAWN_RULES for Namek, Demon Realm, and Otherworld if absent.
 */
@Mixin(targets = "dev.shadowsoffire.apotheosis.adventure.boss.BossRegistry", remap = false)
public abstract class BossRegistryMixin {

    @Inject(
            method = "m_5944_(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/Map;",
            at = @At("RETURN"),
            remap = false
    )
    private void dmzscaling$filterBossEntries(
            ResourceManager resourceManager,
            ProfilerFiller profiler,
            CallbackInfoReturnable<Map<ResourceLocation, JsonElement>> cir
    ) {
        DmzScalingHelpers.filterBossRegistryMap(cir.getReturnValue());
        dmzscaling$ensureDmzDimensionSpawnRules();
    }

    @Unique
    private static void dmzscaling$ensureDmzDimensionSpawnRules() {
        try {
            Map<ResourceLocation, Pair<Float, BossEvents.BossSpawnRules>> rules = AdventureConfig.BOSS_SPAWN_RULES;
            if (rules == null) {
                return;
            }
            synchronized (rules) {
                if (DmzScalingConfig.isEnableExtraDmzNamekBosses()) {
                    rules.putIfAbsent(
                            new ResourceLocation("dragonminez", "namek"),
                            Pair.of(0.020f, BossEvents.BossSpawnRules.NEEDS_SKY)
                    );
                }
                if (DmzScalingConfig.isEnableExtraDmzNetherAndDemonBosses()) {
                    rules.putIfAbsent(
                            new ResourceLocation("dragonminez", "demon_realm"),
                            Pair.of(0.025f, BossEvents.BossSpawnRules.ANY)
                    );
                }
                if (DmzScalingConfig.isEnableExtraDmzEndAndOtherworldBosses()) {
                    rules.putIfAbsent(
                            new ResourceLocation("dragonminez", "otherworld"),
                            Pair.of(0.020f, BossEvents.BossSpawnRules.ANY)
                    );
                }
            }
        } catch (Throwable t) {
            DmzScalingMod.LOGGER.debug("[DMZ Scaling] Could not register default DMZ dimension BossSpawnRules: {}", t.toString());
        }
    }
}
