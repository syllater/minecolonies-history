package com.imperium.realms.command;

import com.imperium.realms.ImperiumRealms;
import com.imperium.realms.colony.ColonyIdentity;
import com.imperium.realms.colony.EconomicPolicy;
import com.imperium.realms.colony.EmpireState;
import com.imperium.realms.colony.EmpireStateSavedData;
import com.imperium.realms.colony.MineColoniesIntegration;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Server-authoritative command interface for the imperial ledger. */
@EventBusSubscriber(modid = ImperiumRealms.MOD_ID)
public final class ImperiumCommands {
    private ImperiumCommands() {
    }

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("imperium")
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource())))
                .then(Commands.literal("tax")
                        .then(Commands.argument("percent", IntegerArgumentType.integer(0, 25))
                                .executes(context -> setTaxRate(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "percent")))))
                .then(Commands.literal("policy")
                        .then(Commands.argument("policy", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        new String[]{"balanced", "mercantile", "welfare", "austerity"}, builder))
                                .executes(context -> setPolicy(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "policy")))))
                .then(Commands.literal("invest")
                        .then(Commands.argument("crowns", IntegerArgumentType.integer(10, 100_000))
                                .executes(context -> invest(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "crowns"))))));
    }

    private static int showStatus(final CommandSourceStack source) {
        final ColonyContext context = resolveColony(source);
        if (context == null) {
            return 0;
        }

        final EmpireState state = context.state();
        final int population = context.colony().getCitizenManager().getCitizens().size();
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.status",
                state.colonyName(),
                state.treasuryCrowns(),
                population,
                state.taxRatePercent(),
                state.economicPolicy().id(),
                state.knowledgePoints(),
                state.stability()), false);
        return 1;
    }

    private static int setTaxRate(final CommandSourceStack source, final int rate) {
        final ColonyContext context = resolveColony(source);
        if (context == null) {
            return 0;
        }
        if (!mayManageEconomy(context)) {
            return denyPermission(source);
        }
        if (!context.state().setTaxRatePercent(rate)) {
            source.sendFailure(Component.translatable("imperium_realms.message.tax_unchanged"));
            return 0;
        }
        context.data().markChanged();
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.tax_rate", rate, context.state().colonyName()), true);
        return 1;
    }

    private static int setPolicy(final CommandSourceStack source, final String requestedPolicy) {
        final ColonyContext context = resolveColony(source);
        if (context == null) {
            return 0;
        }
        if (!mayManageEconomy(context)) {
            return denyPermission(source);
        }

        final EconomicPolicy policy = EconomicPolicy.fromId(requestedPolicy).orElse(null);
        if (policy == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.policy_unknown"));
            return 0;
        }
        if (!context.state().setEconomicPolicy(policy)) {
            source.sendFailure(Component.translatable("imperium_realms.message.policy_same"));
            return 0;
        }
        context.data().markChanged();
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.policy_changed", policy.id()), true);
        return 1;
    }

    private static int invest(final CommandSourceStack source, final int crowns) {
        final ColonyContext context = resolveColony(source);
        if (context == null) {
            return 0;
        }
        if (!mayManageEconomy(context)) {
            return denyPermission(source);
        }
        if (crowns % 10 != 0) {
            source.sendFailure(Component.translatable("imperium_realms.message.invest_multiple"));
            return 0;
        }
        if (!context.state().investInKnowledge(crowns)) {
            source.sendFailure(Component.translatable("imperium_realms.message.treasury_insufficient"));
            return 0;
        }
        context.data().markChanged();
        final long points = crowns / 10L;
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.invest_success",
                crowns,
                points,
                context.state().treasuryCrowns()), true);
        return 1;
    }

    private static boolean mayManageEconomy(final ColonyContext context) {
        final ServerPlayer player = context.player();
        return player.hasPermissions(2)
                || context.colony().getPermissions().hasPermission(player, Action.MANAGE_HUTS);
    }

    private static int denyPermission(final CommandSourceStack source) {
        source.sendFailure(Component.translatable("imperium_realms.message.permission_denied"));
        return 0;
    }

    private static ColonyContext resolveColony(final CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("imperium_realms.message.player_required"));
            return null;
        }

        final ServerLevel level = player.serverLevel();
        final IColony colony = MineColoniesIntegration.colonyAt(level, player.blockPosition()).orElse(null);
        if (colony == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.enter_colony"));
            return null;
        }

        final EmpireStateSavedData data = EmpireStateSavedData.get(level);
        final ColonyIdentity identity = ColonyIdentity.from(colony);
        data.observeColony(identity, colony.getName(), level.getGameTime());
        final EmpireState state = data.get(identity).orElse(null);
        if (state == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.ledger_missing"));
            return null;
        }
        return new ColonyContext(player, colony, data, state);
    }

    private record ColonyContext(
            ServerPlayer player,
            IColony colony,
            EmpireStateSavedData data,
            EmpireState state) {
    }
}
