package com.imperium.realms.network;

import com.imperium.realms.ImperiumRealms;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * A bounded read-only strategic map snapshot assembled by the logical server.
 * Pan/zoom/selection are presentation-only client state.
 */
public record StrategicMapDataPayload(
        boolean available,
        String messageKey,
        String realmName,
        String dimensionId,
        int centerX,
        int centerZ,
        List<ProvinceView> provinces) implements CustomPacketPayload {

    public static final int MAX_PROVINCES = 64;
    public static final Type<StrategicMapDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ImperiumRealms.MOD_ID, "strategic_map_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StrategicMapDataPayload> STREAM_CODEC =
            StreamCodec.of(StrategicMapDataPayload::write, StrategicMapDataPayload::read);

    public StrategicMapDataPayload {
        messageKey = bounded(messageKey, 128);
        realmName = bounded(realmName, 64);
        dimensionId = bounded(dimensionId, 128);
        if (provinces == null || provinces.isEmpty()) {
            provinces = List.of();
        } else {
            provinces = List.copyOf(provinces.subList(0, Math.min(provinces.size(), MAX_PROVINCES)));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void write(
            final RegistryFriendlyByteBuf buffer,
            final StrategicMapDataPayload payload) {
        buffer.writeBoolean(payload.available());
        buffer.writeUtf(payload.messageKey(), 128);
        buffer.writeUtf(payload.realmName(), 64);
        buffer.writeUtf(payload.dimensionId(), 128);
        buffer.writeInt(payload.centerX());
        buffer.writeInt(payload.centerZ());
        buffer.writeVarInt(payload.provinces().size());
        for (final ProvinceView province : payload.provinces()) {
            buffer.writeUtf(province.name(), 96);
            buffer.writeUtf(province.dimensionId(), 128);
            buffer.writeVarInt(province.colonyId());
            buffer.writeInt(province.x());
            buffer.writeInt(province.z());
            buffer.writeBoolean(province.hasCoordinates());
            buffer.writeBoolean(province.capital());
            buffer.writeVarInt(province.stability());
            buffer.writeVarInt(province.unrest());
            buffer.writeVarInt(province.loyalty());
        }
    }

    private static StrategicMapDataPayload read(final RegistryFriendlyByteBuf buffer) {
        final boolean available = buffer.readBoolean();
        final String messageKey = buffer.readUtf(128);
        final String realmName = buffer.readUtf(64);
        final String dimensionId = buffer.readUtf(128);
        final int centerX = buffer.readInt();
        final int centerZ = buffer.readInt();
        final int count = buffer.readVarInt();
        if (count < 0 || count > MAX_PROVINCES) {
            throw new IllegalArgumentException("Invalid strategic map province count: " + count);
        }

        final List<ProvinceView> provinces = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            provinces.add(new ProvinceView(
                    buffer.readUtf(96),
                    buffer.readUtf(128),
                    buffer.readVarInt(),
                    buffer.readInt(),
                    buffer.readInt(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readVarInt(),
                    buffer.readVarInt(),
                    buffer.readVarInt()));
        }
        return new StrategicMapDataPayload(
                available, messageKey, realmName, dimensionId, centerX, centerZ, provinces);
    }

    private static String bounded(final String input, final int maxLength) {
        if (input == null) {
            return "";
        }
        final String trimmed = input.strip();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    public record ProvinceView(
            String name,
            String dimensionId,
            int colonyId,
            int x,
            int z,
            boolean hasCoordinates,
            boolean capital,
            int stability,
            int unrest,
            int loyalty) {
        public ProvinceView {
            name = bounded(name, 96);
            dimensionId = bounded(dimensionId, 128);
            colonyId = Math.max(0, colonyId);
            stability = clampMetric(stability);
            unrest = clampMetric(unrest);
            loyalty = clampMetric(loyalty);
        }

        private static int clampMetric(final int value) {
            return Math.max(0, Math.min(100, value));
        }
    }
}
