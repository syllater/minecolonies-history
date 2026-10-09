package com.imperium.realms.client;

import com.imperium.realms.ImperiumRealms;
import com.imperium.realms.client.gui.ImperialLedgerWindow;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Opens the BlockUI imperial ledger on the client when the configured key is pressed.
 */
@EventBusSubscriber(
        modid = ImperiumRealms.MOD_ID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.GAME)
public final class ImperiumClientEvents {
    private ImperiumClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event) {
        while (ImperiumClientKeys.OPEN_LEDGER.consumeClick()) {
            final Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.level != null) {
                new ImperialLedgerWindow().open();
            }
        }
    }
}
