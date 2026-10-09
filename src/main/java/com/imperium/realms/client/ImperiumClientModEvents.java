package com.imperium.realms.client;

import com.imperium.realms.ImperiumRealms;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/** Physical-client-only key mapping registration. */
@EventBusSubscriber(
        modid = ImperiumRealms.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class ImperiumClientModEvents {
    public static final KeyMapping OPEN_LEDGER = new KeyMapping(
            "key.imperium_realms.open_ledger",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            "key.categories.imperium_realms");

    private ImperiumClientModEvents() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(final RegisterKeyMappingsEvent event) {
        event.register(OPEN_LEDGER);
    }
}
