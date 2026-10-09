package com.imperium.realms;

import com.imperium.realms.colony.ColonyIdentity;
import com.imperium.realms.colony.EmpireState;
import com.imperium.realms.colony.EmpireStateSavedData;
import com.imperium.realms.colony.MineColoniesIntegration;
import com.imperium.realms.economy.ImperialPolicy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = ImperiumRealms.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class ImperialCommands {
    private static final SimpleCommandExceptionType NO_COLONY =
            new SimpleCommandExceptionType(Component.translatable("imperium.command.no_colony"));
    private static final SimpleCommandExceptionType NO_PERMISSION =
            new SimpleCommandExceptionType(Component.translatable("imperium.command.no_permission"));
    private static final SimpleCommandExceptionType INVALID_POLICY =
            new SimpleCommandExceptionType(Component.translatable("imperium.command.invalid_policy"));
    private static final SimpleCommandExceptionType NOT_ENOUGH_EMERALDS =
            new SimpleCommandExceptionType(Component.translatable("imperium.command.not_enough_emeralds"));
    private static final SimpleCommandExceptionType TREASURY_FULL =
            new SimpleCommandExceptionType(Component.translatable("imperium.command.treasury_full"));

    private ImperialCommands() { }

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event) {
        final CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("imperium")
                .then(Commands.literal("status").executes(context -> showStatus(context.getSource())))
                .then(Commands.literal("policy")
                        .then(Commands.literal("set")
                                .then(Commands.argument("policy", StringArgumentType.word())
                                        .executes(context -> setPolicy(context.getSource(),
                                                StringArgumentType.getString(context, "policy"))))))
                .then(Commands.literal("treasury")
                        .then(Commands.literal("status").executes(context -> showTreasury(context.getSource())))
                        .then(Commands.literal("deposit")
                                .then(Commands.argument("emeralds", IntegerArgumentType.integer(1, 1000))
                                        .executes(context -> depositEmeralds(context.getSource(),
                                                IntegerArgumentType.getInteger(context, "emeralds")))))));
    }

    private static int showStatus(final CommandSourceStack source) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final IColony colony = requireManageableColony(player);
        final EmpireState state = stateFor(player.serverLevel(), colony);
        source.sendSuccess(() -> Component.translatable("imperium.command.status",
                state.colonyName(), Component.translatable(state.policy().translationKey()),
                state.treasuryCrowns(), state.diplomaticInfluence(),
                colony.getCitizenManager().getCurrentCitizenCount()), false);
        return 1;
    }

    private static int showTreasury(final CommandSourceStack source) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final IColony colony = requireManageableColony(player);
        final EmpireState state = stateFor(player.serverLevel(), colony);
        source.sendSuccess(() -> Component.translatable(
                "imperium.command.treasury_status", state.treasuryCrowns()), false);
        return 1;
    }

    private static int setPolicy(final CommandSourceStack source, final String policyId)
            throws CommandSyntaxException {
        final ImperialPolicy policy = ImperialPolicy.fromId(policyId).orElseThrow(INVALID_POLICY::create);
        final ServerPlayer player = source.getPlayerOrException();
        final IColony colony = requireManageableColony(player);
        final EmpireState state = stateFor(player.serverLevel(), colony);
        final boolean changed = EmpireStateSavedData.get(player.serverLevel())
                .setPolicy(ColonyIdentity.from(colony), policy);
        if (!changed && state.policy() != policy) throw INVALID_POLICY.create();

        source.sendSuccess(() -> Component.translatable("imperium.command.policy_set",
                Component.translatable(policy.translationKey()), policy.crownsPerCitizenPerDay()), false);
        return 1;
    }

    private static int depositEmeralds(final CommandSourceStack source, final int emeraldCount)
            throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final IColony colony = requireManageableColony(player);
        final ServerLevel level = player.serverLevel();
        final EmpireState state = stateFor(level, colony);
        final long crowns = (long) emeraldCount * 10L;
        if (!state.canCreditCrowns(crowns)) throw TREASURY_FULL.create();

        final Inventory inventory = player.getInventory();
        int available = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            final ItemStack stack = inventory.getItem(slot);
            if (stack.is(Items.EMERALD)) available += stack.getCount();
        }
        if (available < emeraldCount) throw NOT_ENOUGH_EMERALDS.create();

        // Permission, colony and balance checks occur before inventory changes.
        int remaining = emeraldCount;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            final ItemStack stack = inventory.getItem(slot);
            if (!stack.is(Items.EMERALD)) continue;
            final int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
            if (stack.isEmpty()) inventory.setItem(slot, ItemStack.EMPTY);
        }
        inventory.setChanged();

        final EmpireStateSavedData savedData = EmpireStateSavedData.get(level);
        if (!savedData.creditCrowns(ColonyIdentity.from(colony), crowns)) {
            // Defensive refund; this should not be reachable after the preflight.
            final ItemStack refund = new ItemStack(Items.EMERALD, emeraldCount);
            if (!inventory.add(refund)) player.drop(refund, false);
            throw TREASURY_FULL.create();
        }

        final long balance = stateFor(level, colony).treasuryCrowns();
        source.sendSuccess(() -> Component.translatable(
                "imperium.command.deposit_complete", emeraldCount, crowns, balance), false);
        return emeraldCount;
    }

    private static EmpireState stateFor(final ServerLevel level, final IColony colony) {
        return MineColoniesIntegration.getOrCreateState(level, colony);
    }

    private static IColony requireManageableColony(final ServerPlayer player) throws CommandSyntaxException {
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(
                player.serverLevel(), player.blockPosition());
        if (colony == null) throw NO_COLONY.create();
        if (!colony.getPermissions().hasPermission(player, Action.MANAGE_HUTS)) throw NO_PERMISSION.create();
        return colony;
    }
}
