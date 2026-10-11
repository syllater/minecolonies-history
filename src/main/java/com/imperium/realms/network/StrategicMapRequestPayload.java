package com.imperium.realms.network;

import com.imperium.realms.ImperiumRealms;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Empty request for a server-authoritative snapshot of the player's current imperial realm. */
public record StrategicMapRequestPayload() implements CustomPacketPayload {
    public static final Type<StrategicMapRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ImperiumRealms.MOD_ID, "strategic_map_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StrategicMapRequestPayload> STREAM_CODEC =
            StreamCodec.of(
                    (RegistryFriendlyByteBuf buffer, StrategicMapRequestPayload payload) -> {
                        // Intentionally empty: clients cannot submit realm IDs or member lists.
                    },
                    buffer -> new StrategicMapRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
