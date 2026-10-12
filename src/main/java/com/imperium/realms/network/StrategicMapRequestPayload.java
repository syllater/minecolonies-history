package com.imperium.realms.network;

import com.imperium.realms.ImperiumRealms;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Read-only request for a server-authoritative snapshot of the player's current
 * realm. The optional dimension is only a view preference: the server verifies
 * that the requested dimension actually contains one of the realm's provinces.
 * No realm ID, membership list or state values are accepted from the client.
 */
public record StrategicMapRequestPayload(String dimensionId) implements CustomPacketPayload {
    public static final int MAX_DIMENSION_LENGTH = 128;
    public static final Type<StrategicMapRequestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ImperiumRealms.MOD_ID, "strategic_map_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StrategicMapRequestPayload> STREAM_CODEC =
            StreamCodec.of(StrategicMapRequestPayload::write, StrategicMapRequestPayload::read);

    /** Requests the default (capital) dimension view. */
    public StrategicMapRequestPayload() {
        this("");
    }

    public StrategicMapRequestPayload {
        dimensionId = bounded(dimensionId);
    }

    private static void write(
            final RegistryFriendlyByteBuf buffer,
            final StrategicMapRequestPayload payload) {
        buffer.writeUtf(payload.dimensionId(), MAX_DIMENSION_LENGTH);
    }

    private static StrategicMapRequestPayload read(final RegistryFriendlyByteBuf buffer) {
        return new StrategicMapRequestPayload(buffer.readUtf(MAX_DIMENSION_LENGTH));
    }

    private static String bounded(final String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        final String trimmed = value.strip();
        return trimmed.length() <= MAX_DIMENSION_LENGTH
                ? trimmed : trimmed.substring(0, MAX_DIMENSION_LENGTH);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
