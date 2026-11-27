package com.dmzscaling.mixin;

import com.dmzscaling.DmzScalingHelpers;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Filters Apotheosis's BossRegistry during datapack prepare() so Overworld/Nether/End
 * invaders only spawn Dragon Mine Z enemies (optionally keeping Twilight Forest bosses
 * inside Twilight Forest).
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
    }
}
