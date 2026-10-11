package com.imperium.realms.network;

import com.imperium.realms.ImperiumRealms;
import com.imperium.realms.colony.ColonyIdentity;
import com.imperium.realms.colony.EmpireRealm;
import com.imperium.realms.colony.EmpireState;
import com.imperium.realms.colony.EmpireStateSavedData;
import com.imperium.realms.colony.MineColoniesIntegration;
import com.minecolonies.api.colony.IColony;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/** Registers the read-only map payloads; all membership/data lookup happens on the server. */
@EventBusSubscriber(modid = ImperiumRealms.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ImperialMapNetwork {
    private static volatile Consumer<StrategicMapDataPayload> clientReceiver = payload -> { };

    private ImperialMapNetwork() {
    }

    /**
     * Installs a client-only receiver without linking common/network classes
     * to client classes on a dedicated server.
     */
    public static void setClientReceiver(final Consumer<StrategicMapDataPayload> receiver) {
        clientReceiver = Objects.requireNonNull(receiver, "receiver");
    }

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                StrategicMapRequestPayload.TYPE,
                StrategicMapRequestPayload.STREAM_CODEC,
                ImperialMapNetwork::handleRequest);
        registrar.playToClient(
                StrategicMapDataPayload.TYPE,
                StrategicMapDataPayload.STREAM_CODEC,
                ImperialMapNetwork::handleClientData);
    }

    private static void handleRequest(
            final StrategicMapRequestPayload request,
            final IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        // PayloadRegistrar's default thread is the server main thread.
        context.reply(createSnapshot(player, request.dimensionId()));
    }

    private static void handleClientData(
            final StrategicMapDataPayload payload,
            final IPayloadContext context) {
        // Client-only code is registered from a Dist.CLIENT mod-bus subscriber.
        // The dedicated server's default callback is deliberately a no-op.
        clientReceiver.accept(payload);
    }

    private static StrategicMapDataPayload createSnapshot(
            final ServerPlayer player,
            final String requestedDimension) {
        final ServerLevel currentLevel = player.serverLevel();
        final IColony currentColony = MineColoniesIntegration
                .colonyAt(currentLevel, player.blockPosition()).orElse(null);
        if (currentColony == null) {
            return unavailable("imperium_realms.message.enter_colony");
        }

        final ColonyIdentity currentIdentity = ColonyIdentity.from(currentColony);
        final EmpireStateSavedData savedData = EmpireStateSavedData.get(currentLevel);
        final EmpireRealm realm = savedData.realmForProvince(currentIdentity).orElse(null);
        if (realm == null) {
            return unavailable("imperium_realms.message.empire_none");
        }

        // A requested view dimension is accepted only if the server's realm state
        // includes a province in it. Empty/invalid requests default to the capital.
        final String mapDimension = resolveMapDimension(
                requestedDimension, realm.capital(), realm.provinces());
        int centerX = 0;
        int centerZ = 0;
        IColony mapCenter = null;

        if (mapDimension.equals(realm.capital().dimensionId())) {
            mapCenter = MineColoniesIntegration
                    .colonyByIdentity(currentLevel.getServer(), realm.capital()).orElse(null);
        }
        if (mapCenter == null) {
            for (final ColonyIdentity identity : realm.provinces()) {
                if (!mapDimension.equals(identity.dimensionId())) {
                    continue;
                }
                mapCenter = MineColoniesIntegration
                        .colonyByIdentity(currentLevel.getServer(), identity).orElse(null);
                if (mapCenter != null) {
                    break;
                }
            }
        }
        if (mapCenter != null) {
            centerX = mapCenter.getCenter().getX();
            centerZ = mapCenter.getCenter().getZ();
        } else if (mapDimension.equals(currentLevel.dimension().location().toString())) {
            // Useful fallback if the member colony is already the active one but
            // its ID lookup is temporarily unavailable during a lifecycle edge.
            final BlockPos position = currentColony.getCenter();
            centerX = position.getX();
            centerZ = position.getZ();
        }

        final List<StrategicMapDataPayload.ProvinceView> provinces = new ArrayList<>();
        for (final ColonyIdentity identity : realm.provinces()) {
            if (provinces.size() >= StrategicMapDataPayload.MAX_PROVINCES) {
                break;
            }
            final EmpireState state = savedData.get(identity).orElse(null);
            final IColony colony = MineColoniesIntegration
                    .colonyByIdentity(currentLevel.getServer(), identity).orElse(null);

            final boolean hasCoordinates = colony != null;
            final BlockPos center = hasCoordinates ? colony.getCenter() : BlockPos.ZERO;
            final String name = colony != null
                    ? colony.getName()
                    : state == null ? identity.storageKey() : state.colonyName();

            provinces.add(new StrategicMapDataPayload.ProvinceView(
                    name,
                    identity.dimensionId(),
                    identity.colonyId(),
                    center.getX(),
                    center.getZ(),
                    hasCoordinates,
                    identity.equals(realm.capital()),
                    state == null ? 50 : state.stability(),
                    state == null ? 0 : state.unrest(),
                    realm.provincialLoyalty(identity)));
        }

        return new StrategicMapDataPayload(
                true, "", realm.name(), mapDimension, centerX, centerZ, provinces);
    }

    /**
     * Resolve the client's map-view preference against server-owned membership.
     * This method selects only a projection dimension; it never adds provinces
     * or changes realm state.
     */
    static String resolveMapDimension(
            final String requestedDimension,
            final ColonyIdentity capital,
            final List<ColonyIdentity> realmProvinces) {
        Objects.requireNonNull(capital, "capital");
        Objects.requireNonNull(realmProvinces, "realmProvinces");
        if (requestedDimension == null || requestedDimension.isBlank()) {
            return capital.dimensionId();
        }
        final String requested = requestedDimension.strip();
        return realmProvinces.stream()
                .filter(Objects::nonNull)
                .map(ColonyIdentity::dimensionId)
                .filter(requested::equals)
                .findFirst()
                .orElse(capital.dimensionId());
    }

    private static StrategicMapDataPayload unavailable(final String messageKey) {
        return new StrategicMapDataPayload(false, messageKey, "", "", 0, 0, List.of());
    }
}
