package com.imperium.realms.commands;

import com.imperium.realms.ImperiumRealms;
import com.imperium.realms.colony.ColonyIdentity;
import com.imperium.realms.colony.EmpireState;
import com.imperium.realms.colony.EmpireStateSavedData;
import com.imperium.realms.colony.MineColoniesIntegration;
import com.imperium.realms.economy.EmpirePolicy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Optional;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;

/**
 * Initial playable economy interface. All writes go through server-side
 * MineColonies checks and Imperium's SavedData.
 */
@EventBusSubscriber(modid = ImperiumRealms.MOD_ID)
public final class ImperiumCommands {
    private ImperiumCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(final RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("imperium")
                        .then(Commands.literal("status")
                                .executes(context -> showStatus(context.getSource())))
                        .then(Commands.literal("taxes")
                                .then(Commands.literal("rate")
                                        .then(Commands.argument("percent", integer(0, 50))
                                                .executes(context -> setTaxRate(
                                                        context.getSource(),
                                                        getInteger(context, "percent")))))
                                .then(Commands.literal("collect")
                                        .executes(context -> collectTaxes(context.getSource()))))
                        .then(Commands.literal("policy")
                                .then(Commands.literal("balanced")
                                        .executes(context -> setPolicy(context.getSource(), EmpirePolicy.BALANCED)))
                                .then(Commands.literal("public_works")
                                        .executes(context -> setPolicy(context.getSource(), EmpirePolicy.PUBLIC_WORKS)))
                                .then(Commands.literal("scholarship")
                                        .executes(context -> setPolicy(context.getSource(), EmpirePolicy.SCHOLARSHIP))));
    }

    private static int showStatus(final CommandSourceStack source) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final Optional<IColony> colony = findColony(source, player);
        if (colony.isEmpty()) {
            return 0;
        }

        final EmpireState state = stateFor(player.serverLevel(), colony.get());
        source.sendSuccess(() -> Component.translatable(
                "commands.imperium.status",
                state.colonyName(),
                state.treasury(),
                state.taxRatePercent(),
                Component.translatable("policy.imperium." + state.policy().id())), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int setTaxRate(final CommandSourceStack source, final int percent) throws Exception {
        final ServerPlayer player = source.getPlayerOrException();
        final Optional<IColony> colony = findManageableColony(source, player);
        if (colony.isEmpty()) {
            return 0;
        }

        final ServerLevel level = player.serverLevel();
        final ColonyIdentity identity = ColonyIdentity.from(colony.get());
        final EmpireStateSavedData data = EmpireStateSavedData.get(level);
        final EmpireState state = stateFor(level, colony.get());
        if (state.taxRatePercent() == percent) {
            source.sendSuccess(() -> Component.translatable("commands.imperium.tax_rate_unchanged", percent), false);
            return Command.SINGLE_SUCCESS;
        }
        data.setTaxRate(identity, percent);
        source.sendSuccess(() -> Component.translatable("commands.imperium.tax_rate_set", percent), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setPolicy(
            final CommandSourceStack source,
            final EmpirePolicy policy) throws Exception {
        final ServerPlayer player = source.getPlayerOrException();
        final Optional<IColony> colony = findManageableColony(source, player);
        if (colony.isEmpty()) {
            return 0;
        }

        final ServerLevel level = player.serverLevel();
        final ColonyIdentity identity = ColonyIdentity.from(colony.get());
        final EmpireStateSavedData data = EmpireStateSavedData.get(level);
        final EmpireState state = stateFor(level, colony.get());
        if (state.policy() == policy) {
            source.sendSuccess(() -> Component.translatable(
                    "commands.imperium.policy_unchanged",
                    Component.translatable("policy.imperium." + policy.id())), false);
            return Command.SINGLE_SUCCESS;
        }
        data.setPolicy(identity, policy);
        source.sendSuccess(() -> Component.translatable(
                "commands.imperium.policy_set",
                Component.translatable("policy.imperium." + policy.id())), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int collectTaxes(final CommandSourceStack source) throws Exception {
        final ServerPlayer player = source.getPlayerOrException();
        final Optional<IColony> colony = findManageableColony(source, player);
        if (colony.isEmpty()) {
            return 0;
        }

        final ServerLevel level = player.serverLevel();
        final IColony mineColony = colony.get();
        final ColonyIdentity identity = ColonyIdentity.from(mineColony);
        final EmpireStateSavedData data = EmpireStateSavedData.get(level);
        stateFor(level, mineColony);

        final Optional<EmpireState.TaxCollectionResult> result = data.collectTaxes(
                identity,
                mineColony.getCitizenManager().getCurrentCitizenCount(),
                level.getServer().overworld().getGameTime());
        if (result.isEmpty()) {
            source.sendFailure(Component.translatable("commands.imperium.taxes_already_collected"));
            return 0;
        }

        final EmpireState.TaxCollectionResult taxes = result.get();
        source.sendSuccess(() -> Component.translatable(
                "commands.imperium.taxes_collected",
                taxes.grossRevenue(),
                taxes.upkeepPaid(),
                taxes.treasuryBalance()), true);

        if (taxes.unpaidUpkeep() > 0) {
            source.sendFailure(Component.translatable("commands.imperium.upkeep_unpaid", taxes.unpaidUpkeep()));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static Optional<IColony> findColony(
            final CommandSourceStack source,
            final ServerPlayer player) {
        final Optional<IColony> colony = MineColoniesIntegration.colonyAt(
                player.serverLevel(), player.blockPosition());
        if (colony.isEmpty()) {
            source.sendFailure(Component.translatable("commands.imperium.no_colony"));
        }
        return colony;
    }

    private static Optional<IColony> findManageableColony(
            final CommandSourceStack source,
            final ServerPlayer player) {
        final Optional<IColony> colony = findColony(source, player);
        if (colony.isEmpty()) {
            return Optional.empty();
        }
        if (!colony.get().getPermissions().hasPermission(player, Action.MANAGE_HUTS)) {
            source.sendFailure(Component.translatable("commands.imperium.no_permission"));
            return Optional.empty();
        }
        return colony;
    }

    private static EmpireState stateFor(final ServerLevel level, final IColony colony) {
        return MineColoniesIntegration.getOrCreateState(level, colony);
    }
}
