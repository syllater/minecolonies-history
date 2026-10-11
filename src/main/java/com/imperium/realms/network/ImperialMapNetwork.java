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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.DistExecutor;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;
import java.util.List;

/** Registers the read-only map payloads; all membership/data lookup happens on the server. */
@EventBusSubscriber(modid = ImperiumRealms.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ImperialMapNetwork {
    private ImperialMapNetwork() {
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
        context.reply(createSnapshot(player));
    }

    private static void handleClientData(
            final StrategicMapDataPayload payload,
            final IPayloadContext context) {
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> com.imperium.realms.client.ImperialMapPayloadReceiver.accept(payload));
    }

    private static StrategicMapDataPayload createSnapshot(final ServerPlayer player) {
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

        final String mapDimension = realm.capital().dimensionId();
        int centerX = 0;
        int centerZ = 0;
        boolean foundMapCenter = false;

        final IColony capital = MineColoniesIntegration
                .colonyByIdentity(currentLevel.getServer(), realm.capital()).orElse(null);
        if (capital != null) {
            centerX = capital.getCenter().getX();
            centerZ = capital.getCenter().getZ();
            foundMapCenter = true;
        } else {
            for (final ColonyIdentity identity : realm.provinces()) {
                if (!mapDimension.equals(identity.dimensionId())) {
                    continue;
                }
                final IColony province = MineColoniesIntegration
                        .colonyByIdentity(currentLevel.getServer(), identity).orElse(null);
                if (province != null) {
                    centerX = province.getCenter().getX();
                    centerZ = province.getCenter().getZ();
                    foundMapCenter = true;
                    break;
                }
            }
        }
        if (!foundMapCenter && mapDimension.equals(currentLevel.dimension().location().toString())) {
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

    private static StrategicMapDataPayload unavailable(final String messageKey) {
        return new StrategicMapDataPayload(false, messageKey, "", "", 0, 0, List.of());
    }
}
