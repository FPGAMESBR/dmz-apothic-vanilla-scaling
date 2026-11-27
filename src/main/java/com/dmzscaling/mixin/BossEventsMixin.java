package com.dmzscaling.mixin;

import com.dmzscaling.DmzScalingHelpers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optionally suppresses vanilla Apotheosis Minibosses (Craig, Undead Knight, Honeyed Archer,
 * Fast Enderman, Withering Archer) outside Twilight Forest.
 */
@Mixin(targets = "dev.shadowsoffire.apotheosis.adventure.boss.BossEvents", remap = false)
public abstract class BossEventsMixin {

    @Inject(
            method = "minibosses(Lnet/minecraftforge/event/entity/living/MobSpawnEvent$FinalizeSpawn;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void dmzscaling$filterVanillaMinibosses(MobSpawnEvent.FinalizeSpawn event, CallbackInfo ci) {
        Mob mob = event.getEntity();
        if (mob == null) {
            return;
        }
        ResourceLocation entityKey = EntityType.m_20613_(mob.m_6095_());
        String entityId = (entityKey != null) ? entityKey.toString() : "";
        String dimId = "";
        try {
            dimId = event.getLevel().m_6018_().m_46472_().m_135782_().toString();
        } catch (Throwable ignored) {
        }
        if (DmzScalingHelpers.shouldBlockApotheosisMiniboss(entityId, dimId)) {
            ci.cancel();
        }
    }
}
