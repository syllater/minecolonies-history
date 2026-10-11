package com.imperium.realms.command;

import com.imperium.realms.ImperiumRealms;
import com.imperium.realms.colony.ColonyIdentity;
import com.imperium.realms.colony.EmpireRealm;
import com.imperium.realms.colony.EmpireState;
import com.imperium.realms.colony.EmpireStateSavedData;
import com.imperium.realms.colony.ImperialDefensiveOrder;
import com.imperium.realms.colony.MineColoniesIntegration;
import com.imperium.realms.colony.EmpireStateSavedData.DefensiveOrderIssueResult;
import com.imperium.realms.colony.EmpireStateSavedData.SupplyRouteBuildResult;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Explicit dimension-aware variants of realm infrastructure and governor
 * commands. The original same-dimension syntax stays available.
 *
 * <p>Dimension and colony identity are resolved by the server. A client cannot
 * supply arbitrary state keys to bypass realm-membership or Emperor checks.</p>
 */
@EventBusSubscriber(modid = ImperiumRealms.MOD_ID)
public final class ImperiumCrossDimensionRealmCommands {
    private ImperiumCrossDimensionRealmCommands() {
    }

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("imperium")
                .then(Commands.literal("empire")
                        .then(Commands.literal("route")
                                .then(Commands.literal("build-at")
                                        .then(Commands.argument("dimension", StringArgumentType.word())
                                                .then(Commands.argument("colonyId", IntegerArgumentType.integer(0))
                                                        .executes(context -> buildSupplyRouteAt(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "dimension"),
                                                                IntegerArgumentType.getInteger(context, "colonyId")))))))
                        .then(Commands.literal("defense")
                                .then(Commands.literal("at")
                                        .then(Commands.argument("dimension", StringArgumentType.word())
                                                .then(Commands.argument("colonyId", IntegerArgumentType.integer(0))
                                                        .executes(context -> issueDefensiveOrderAt(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "dimension"),
                                                                IntegerArgumentType.getInteger(context, "colonyId"))))))))
                .then(Commands.literal("governor")
                        .then(Commands.literal("appoint-at")
                                .then(Commands.argument("dimension", StringArgumentType.word())
                                        .then(Commands.argument("colonyId", IntegerArgumentType.integer(0))
                                                .then(Commands.argument("governor", EntityArgument.player())
                                                        .executes(context -> appointGovernorAt(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "dimension"),
                                                                IntegerArgumentType.getInteger(context, "colonyId"),
                                                                EntityArgument.getPlayer(context, "governor")))))))
                        .then(Commands.literal("dismiss-at")
                                .then(Commands.argument("dimension", StringArgumentType.word())
                                        .then(Commands.argument("colonyId", IntegerArgumentType.integer(0))
                                                .executes(context -> dismissGovernorAt(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "dimension"),
                                                        IntegerArgumentType.getInteger(context, "colonyId")))))));
    }

    private static int buildSupplyRouteAt(
            final CommandSourceStack source, final String dimension, final int colonyId) {
        final EmpireContext context = resolveContext(source);
        if (context == null) return 0;

        final EmpireRealm realm = context.data().realmForProvince(context.identity()).orElse(null);
        if (realm == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_none"));
            return 0;
        }
        if (!mayManageRealm(context, realm)) {
            source.sendFailure(Component.translatable("imperium_realms.message.supply_route_emperor_only"));
            return 0;
        }

        final IColony target = findColony(source, dimension, colonyId);
        if (target == null) return 0;

        final ColonyIdentity destination = ColonyIdentity.from(target);
        MineColoniesIntegration.getOrCreateState(context.level(), target);
        final SupplyRouteBuildResult result = context.data().buildSupplyRoute(
                realm.id(), destination, context.player().getGameProfile().getName(), currentDay(source));
        switch (result) {
            case BUILT -> {
                source.sendSuccess(() -> Component.translatable(
                        "imperium_realms.message.supply_route_built",
                        target.getName(), EmpireRealm.SUPPLY_ROUTE_BUILD_COST,
                        realm.imperialTreasuryCrowns()), true);
                return 1;
            }
            case REALM_NOT_FOUND -> source.sendFailure(Component.translatable("imperium_realms.message.empire_none"));
            case NOT_MEMBER -> source.sendFailure(Component.translatable("imperium_realms.message.supply_route_not_member"));
            case CAPITAL_PROVINCE -> source.sendFailure(Component.translatable("imperium_realms.message.supply_route_capital"));
            case ALREADY_EXISTS -> source.sendFailure(Component.translatable("imperium_realms.message.supply_route_exists"));
            case INSUFFICIENT_TREASURY -> source.sendFailure(Component.translatable(
                    "imperium_realms.message.supply_route_funds", EmpireRealm.SUPPLY_ROUTE_BUILD_COST));
            case INVALID_DAY -> source.sendFailure(Component.translatable("imperium_realms.message.supply_route_build_failed"));
        }
        return 0;
    }

    private static int issueDefensiveOrderAt(
            final CommandSourceStack source, final String dimension, final int colonyId) {
        final EmpireContext context = resolveContext(source);
        if (context == null) return 0;

        final EmpireRealm realm = context.data().realmForProvince(context.identity()).orElse(null);
        if (realm == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_none"));
            return 0;
        }
        if (!mayManageRealm(context, realm)) {
            source.sendFailure(Component.translatable("imperium_realms.message.defensive_orders_emperor_only"));
            return 0;
        }

        final IColony target = findColony(source, dimension, colonyId);
        if (target == null) return 0;

        final ColonyIdentity targetIdentity = ColonyIdentity.from(target);
        MineColoniesIntegration.getOrCreateState(context.level(), target);
        final DefensiveOrderIssueResult result = context.data().issueDefensiveOrder(
                realm.id(), targetIdentity, context.player().getGameProfile().getName(), currentDay(source));
        switch (result) {
            case ISSUED -> {
                source.sendSuccess(() -> Component.translatable(
                        "imperium_realms.message.defensive_order_issued",
                        target.getName(), ImperialDefensiveOrder.COST_CROWNS,
                        ImperialDefensiveOrder.DURATION_DAYS, ImperialDefensiveOrder.READINESS_BONUS,
                        realm.imperialTreasuryCrowns()), true);
                return 1;
            }
            case REALM_NOT_FOUND -> source.sendFailure(Component.translatable("imperium_realms.message.empire_none"));
            case NOT_MEMBER -> source.sendFailure(Component.translatable("imperium_realms.message.supply_route_not_member"));
            case ALREADY_ACTIVE -> source.sendFailure(Component.translatable("imperium_realms.message.defensive_order_active"));
            case INSUFFICIENT_TREASURY -> source.sendFailure(Component.translatable(
                    "imperium_realms.message.defensive_order_funds", ImperialDefensiveOrder.COST_CROWNS));
            case INVALID_DAY -> source.sendFailure(Component.translatable("imperium_realms.message.defensive_order_failed"));
        }
        return 0;
    }

    private static int appointGovernorAt(
            final CommandSourceStack source,
            final String dimension,
            final int colonyId,
            final ServerPlayer governorPlayer) {
        final EmpireContext context = resolveContext(source);
        if (context == null) return 0;

        final EmpireRealm realm = context.data().realmForProvince(context.identity()).orElse(null);
        if (realm == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_empire_required"));
            return 0;
        }
        if (!mayManageRealm(context, realm)) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_emperor_only"));
            return 0;
        }

        final IColony target = findColony(source, dimension, colonyId);
        if (target == null) return 0;
        final ColonyIdentity identity = ColonyIdentity.from(target);
        if (identity.equals(realm.capital())) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_capital"));
            return 0;
        }
        if (!realm.containsProvince(identity)) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_not_member"));
            return 0;
        }

        final EmpireState provinceState = MineColoniesIntegration.getOrCreateState(context.level(), target);
        if (!context.data().appointGovernor(realm.id(), identity,
                governorPlayer.getUUID().toString(), governorPlayer.getGameProfile().getName(), currentDay(source))) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_unchanged"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.governor_assigned",
                governorPlayer.getGameProfile().getName(), provinceState.colonyName()), true);
        return 1;
    }

    private static int dismissGovernorAt(
            final CommandSourceStack source, final String dimension, final int colonyId) {
        final EmpireContext context = resolveContext(source);
        if (context == null) return 0;

        final EmpireRealm realm = context.data().realmForProvince(context.identity()).orElse(null);
        if (realm == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_empire_required"));
            return 0;
        }
        if (!mayManageRealm(context, realm)) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_emperor_only"));
            return 0;
        }

        final IColony target = findColony(source, dimension, colonyId);
        if (target == null) return 0;
        final ColonyIdentity identity = ColonyIdentity.from(target);
        if (!realm.containsProvince(identity) || identity.equals(realm.capital())) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_not_member"));
            return 0;
        }
        if (!context.data().dismissGovernor(realm.id(), identity,
                context.player().getGameProfile().getName(), currentDay(source))) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_none"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.governor_dismissed", target.getName()), true);
        return 1;
    }

    private static IColony findColony(
            final CommandSourceStack source, final String dimension, final int colonyId) {
        final ColonyIdentity identity;
        try {
            identity = new ColonyIdentity(dimension, colonyId);
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.empire_target_not_found", colonyId));
            return null;
        }
        return MineColoniesIntegration.colonyByIdentity(source.getServer(), identity).orElseGet(() -> {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.empire_target_not_found", colonyId));
            return null;
        });
    }

    private static EmpireContext resolveContext(final CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("imperium_realms.message.player_required"));
            return null;
        }
        final var level = player.serverLevel();
        final IColony colony = MineColoniesIntegration.colonyAt(level, player.blockPosition()).orElse(null);
        if (colony == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.enter_colony"));
            return null;
        }
        final EmpireStateSavedData data = EmpireStateSavedData.get(level);
        final ColonyIdentity identity = ColonyIdentity.from(colony);
        data.observeColony(identity, colony.getName(), level.getGameTime());
        final EmpireState state = data.get(identity).orElseGet(() -> {
            MineColoniesIntegration.getOrCreateState(level, colony);
            return data.get(identity).orElse(null);
        });
        if (state == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.ledger_missing"));
            return null;
        }
        return new EmpireContext(player, level, colony, data, identity, state);
    }

    private static long currentDay(final CommandSourceStack source) {
        return Math.max(0L, Math.floorDiv(source.getServer().overworld().getGameTime(), 24_000L));
    }

    private static boolean mayManageRealm(final EmpireContext context, final EmpireRealm realm) {
        return context.player().hasPermissions(2) || realm.isEmperor(context.player().getUUID().toString());
    }

    @SuppressWarnings("unused")
    private static boolean mayManageEconomy(final EmpireContext context) {
        return context.player().hasPermissions(2)
                || context.colony().getPermissions().hasPermission(context.player(), Action.MANAGE_HUTS);
    }

    private static int denyPermission(final CommandSourceStack source) {
        source.sendFailure(Component.translatable("imperium_realms.message.permission_denied"));
        return 0;
    }

    private record EmpireContext(
            ServerPlayer player,
            net.minecraft.server.level.ServerLevel level,
            IColony colony,
            EmpireStateSavedData data,
            ColonyIdentity identity,
            EmpireState state) {
    }
}
