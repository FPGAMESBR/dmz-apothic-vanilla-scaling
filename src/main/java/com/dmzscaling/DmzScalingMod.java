package com.dmzscaling;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(DmzScalingMod.MOD_ID)
public final class DmzScalingMod {
    public static final String MOD_ID = "dmzscaling";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public DmzScalingMod() {
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.COMMON,
                DmzScalingConfig.SPEC,
                "dmz_apothic_vanilla_scaling-common.toml"
        );
        MinecraftForge.EVENT_BUS.register(VanillaScalingEvents.class);
        LOGGER.info("[DMZ Apothic & Vanilla Scaling] Loaded Apotheosis DMZ boss filter and hybrid DMZ vanilla mob/boss scaling.");
    }
}
