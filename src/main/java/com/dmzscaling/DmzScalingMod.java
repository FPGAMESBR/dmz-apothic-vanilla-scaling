package com.dmzscaling;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(DmzScalingMod::onConfigLoad);
        modBus.addListener(DmzScalingMod::onConfigReload);

        MinecraftForge.EVENT_BUS.register(VanillaScalingEvents.class);
        MinecraftForge.EVENT_BUS.register(DmzScalingCommands.class);
        LOGGER.info("[DMZ Apothic & Vanilla Scaling v1.1.0] Loaded Apotheosis DMZ boss filter, party scaling, and 2-stage DMZ Defense-aware boss scaling.");
    }

    private static void onConfigLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == DmzScalingConfig.SPEC) {
            DmzScalingConfig.bakeCaches();
        }
    }

    private static void onConfigReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == DmzScalingConfig.SPEC) {
            DmzScalingConfig.bakeCaches();
        }
    }
}
