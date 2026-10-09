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

/**
 * First playable command interface for the empire ledger. All financial and
 * policy operations resolve the player's MineColonies colony on the server.
 */
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
        source.sendSuccess(() -> Component.literal(
                "§6Imperial Ledger — " + state.colonyName()
                        + " §7| §eTreasury: §f" + state.treasuryCrowns() + " crowns"
                        + " §7| §ePopulation: §f" + population
                        + " §7| §eTax: §f" + state.taxRatePercent() + "%"
                        + " §7| §ePolicy: §f" + state.economicPolicy().id()
                        + " §7| §eKnowledge: §f" + state.knowledgePoints()
                        + " §7| §eStability: §f" + state.stability() + "/100"),
                false);
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
            source.sendFailure(Component.literal("Tax rate unchanged. Choose a rate from 0 to 25 percent."));
            return 0;
        }
        context.data().markChanged();
        source.sendSuccess(() -> Component.literal("Tax rate set to " + rate + "% for "
                + context.state().colonyName() + "."), true);
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
            source.sendFailure(Component.literal(
                    "Unknown policy. Choose balanced, mercantile, welfare, or austerity."));
            return 0;
        }
        if (!context.state().setEconomicPolicy(policy)) {
            source.sendFailure(Component.literal("That policy is already active."));
            return 0;
        }
        context.data().markChanged();
        source.sendSuccess(() -> Component.literal("Economic policy changed to "
                + policy.id() + ". Its tax and stability effects apply on the next daily turn."), true);
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
            source.sendFailure(Component.literal("Investments must be a multiple of 10 crowns."));
            return 0;
        }
        if (!context.state().investInKnowledge(crowns)) {
            source.sendFailure(Component.literal("The treasury does not contain enough crowns."));
            return 0;
        }
        context.data().markChanged();
        final long points = crowns / 10L;
        source.sendSuccess(() -> Component.literal("Invested " + crowns + " crowns into "
                + points + " knowledge point(s). Treasury balance: "
                + context.state().treasuryCrowns() + " crowns."), true);
        return 1;
    }

    private static boolean mayManageEconomy(final ColonyContext context) {
        final ServerPlayer player = context.player();
        return player.hasPermissions(2)
                || context.colony().getPermissions().hasPermission(player, Action.MANAGE_HUTS);
    }

    private static int denyPermission(final CommandSourceStack source) {
        source.sendFailure(Component.literal(
                "You need colony hut-management permission or operator permission to change the imperial economy."));
        return 0;
    }

    private static ColonyContext resolveColony(final CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be used by a player in a MineColonies colony."));
            return null;
        }

        final ServerLevel level = player.serverLevel();
        final IColony colony = MineColoniesIntegration.colonyAt(level, player.blockPosition()).orElse(null);
        if (colony == null) {
            source.sendFailure(Component.literal(
                    "Stand inside a MineColonies colony to access its imperial ledger."));
            return null;
        }

        final EmpireStateSavedData data = EmpireStateSavedData.get(level);
        final ColonyIdentity identity = ColonyIdentity.from(colony);
        data.observeColony(identity, colony.getName(), level.getGameTime());
        final EmpireState state = data.get(identity).orElse(null);
        if (state == null) {
            source.sendFailure(Component.literal("The imperial ledger could not initialize for this colony."));
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
