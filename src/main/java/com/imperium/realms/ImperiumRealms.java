package com.imperium.realms;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(ImperiumRealms.MOD_ID)
public final class ImperiumRealms {
    public static final String MOD_ID = "imperium_realms";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ImperiumRealms(final IEventBus modEventBus) {
        ImperialJobRegistry.register(modEventBus);
        ImperialBlockRegistry.register(modEventBus);
        ImperialBuildingRegistry.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Imperium: European Realms common setup initialized.");
    }
}
