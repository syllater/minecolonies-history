package com.imperium.realms.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

/**
 * Client-only key bindings. Server gameplay mutations still run through commands.
 */
@OnlyIn(Dist.CLIENT)
public final class ImperiumClientKeys {
    public static final KeyMapping OPEN_LEDGER = new KeyMapping(
            "key.imperium_realms.open_ledger",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.imperium_realms");

    private ImperiumClientKeys() {
    }
}
