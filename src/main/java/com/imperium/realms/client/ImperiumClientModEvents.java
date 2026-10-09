package com.imperium.realms.client;

import com.imperium.realms.ImperiumRealms;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Client mod-bus registrations. Dist gating keeps client types out of dedicated-server setup.
 */
@EventBusSubscriber(
        modid = ImperiumRealms.MOD_ID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD)
public final class ImperiumClientModEvents {
    private ImperiumClientModEvents() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(final RegisterKeyMappingsEvent event) {
        event.register(ImperiumClientKeys.OPEN_LEDGER);
    }
}
