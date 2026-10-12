package com.imperium.realms.client;

import com.imperium.realms.ImperiumRealms;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Opens the ledger without loading client classes on a dedicated server. */
@EventBusSubscriber(
        modid = ImperiumRealms.MOD_ID,
        bus = EventBusSubscriber.Bus.GAME,
        value = Dist.CLIENT)
public final class ImperiumClientEvents {
    private ImperiumClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(final ClientTickEvent.Post event) {
        final Minecraft minecraft = Minecraft.getInstance();
        while (ImperiumClientModEvents.OPEN_LEDGER.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                new ImperiumLedgerWindow().open();
            }
        }
    }
}
