package com.imperium.realms.client;

import com.imperium.realms.network.StrategicMapDataPayload;
import net.minecraft.client.Minecraft;

/** Physical-client receiver isolated from dedicated-server class loading. */
public final class ImperialMapPayloadReceiver {
    private ImperialMapPayloadReceiver() {
    }

    public static void accept(final StrategicMapDataPayload payload) {
        if (Minecraft.getInstance().screen instanceof ImperialMapScreen screen) {
            screen.acceptServerSnapshot(payload);
        }
    }
}
