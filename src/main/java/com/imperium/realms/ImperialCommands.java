package com.imperium.realms;

import com.imperium.realms.colony.ColonyIdentity;
import com.imperium.realms.colony.EmpireState;
import com.imperium.realms.colony.EmpireStateSavedData;
import com.imperium.realms.colony.MineColoniesIntegration;
import com.imperium.realms.economy.ImperialPolicy;
import com.imperium.realms.government.GovernmentType;
import com.imperium.realms.government.ImperialFaction;
import com.imperium.realms.government.PolicyBill;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.colony.permissions.ColonyPlayer;
import com.minecolonies.api.colony.permissions.Rank;
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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
    private static final SimpleCommandExceptionType OPEN_BILL_EXISTS =
            new SimpleCommandExceptionType(Component.translatable("imperium.command.open_bill_exists"));
    private static final SimpleCommandExceptionType NO_OPEN_BILL =
            new SimpleCommandExceptionType(Component.translatable("imperium.command.no_open_bill"));
    private static final SimpleCommandExceptionType INVALID_VOTE =
            new SimpleCommandExceptionType(Component.translatable("imperium.command.invalid_vote"));
    private static final SimpleCommandExceptionType NOT_ELIGIBLE_VOTER =
            new SimpleCommandExceptionType(Component.translatable("imperium.command.not_eligible_voter"));
    private static final SimpleCommandExceptionType ALREADY_VOTED =
            new SimpleCommandExceptionType(Component.translatable("imperium.command.already_voted"));

    private ImperialCommands() { }

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event) {
        final CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("imperium")
                .then(Commands.literal("status").executes(context -> showStatus(context.getSource())))
                .then(Commands.literal("government")
                        .then(Commands.literal("status").executes(context -> showGovernment(context.getSource()))))
                .then(Commands.literal("policy")
                        .then(Commands.literal("set")
                                .then(Commands.argument("policy", StringArgumentType.word())
                                        .executes(context -> proposePolicy(context.getSource(),
                                                StringArgumentType.getString(context, "policy"))))))
                .then(Commands.literal("parliament")
                        .then(Commands.literal("status").executes(context -> showParliament(context.getSource())))
                        .then(Commands.literal("propose")
                                .then(Commands.argument("policy", StringArgumentType.word())
                                        .executes(context -> proposePolicy(context.getSource(),
                                                StringArgumentType.getString(context, "policy")))))
                        .then(Commands.literal("vote")
                                .then(Commands.argument("billId", IntegerArgumentType.integer(1))
                                        .then(Commands.argument("choice", StringArgumentType.word())
                                                .executes(context -> voteOnBill(
                                                        context.getSource(),
                                                        IntegerArgumentType.getInteger(context, "billId"),
                                                        StringArgumentType.getString(context, "choice")))))))
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

    private static int showGovernment(final CommandSourceStack source) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final IColony colony = requireManageableColony(player);
        final EmpireState state = stateFor(player.serverLevel(), colony);
        source.sendSuccess(() -> Component.translatable("imperium.command.government_status",
                colony.getPermissions().getOwnerName(),
                Component.translatable(state.governmentType().translationKey()),
                state.governmentStability(), state.unrestLevel()), false);
        return 1;
    }

    private static int showParliament(final CommandSourceStack source) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final IColony colony = requireManageableColony(player);
        final EmpireState state = stateFor(player.serverLevel(), colony);
        final PolicyBill bill = state.currentBill().orElse(null);
        if (bill == null) {
            source.sendSuccess(() -> Component.translatable("imperium.command.parliament_empty",
                    state.governmentStability(),
                    state.factionSupport(ImperialFaction.CROWN),
                    state.factionSupport(ImperialFaction.GUILDS),
                    state.factionSupport(ImperialFaction.COMMONS)), false);
            return 1;
        }
        source.sendSuccess(() -> Component.translatable("imperium.command.parliament_status",
                bill.id(), Component.translatable(bill.proposedPolicy().translationKey()),
                Component.translatable(bill.status().translationKey()),
                bill.yesVotes(), bill.noVotes(),
                state.governmentStability(),
                state.factionSupport(ImperialFaction.CROWN),
                state.factionSupport(ImperialFaction.GUILDS),
                state.factionSupport(ImperialFaction.COMMONS)), false);
        return 1;
    }

    private static int proposePolicy(final CommandSourceStack source, final String policyId)
            throws CommandSyntaxException {
        final ImperialPolicy policy = ImperialPolicy.fromId(policyId).orElseThrow(INVALID_POLICY::create);
        final ServerPlayer player = source.getPlayerOrException();
        final IColony colony = requireManageableColony(player);
        final ServerLevel level = player.serverLevel();
        stateFor(level, colony);

        final var bill = EmpireStateSavedData.get(level).proposePolicy(
                ColonyIdentity.from(colony), policy, player.getUUID(),
                level.getServer().overworld().getGameTime());
        if (bill.isEmpty()) throw OPEN_BILL_EXISTS.create();

        source.sendSuccess(() -> Component.translatable("imperium.command.bill_proposed",
                bill.get().id(), Component.translatable(policy.translationKey())), false);
        return bill.get().id();
    }

    private static int voteOnBill(
            final CommandSourceStack source, final int billId, final String choice)
            throws CommandSyntaxException {
        final boolean approve;
        if ("yes".equalsIgnoreCase(choice)) {
            approve = true;
        } else if ("no".equalsIgnoreCase(choice)) {
            approve = false;
        } else {
            throw INVALID_VOTE.create();
        }

        final ServerPlayer player = source.getPlayerOrException();
        final IColony colony = requireManageableColony(player);
        final ServerLevel level = player.serverLevel();
        final EmpireState state = stateFor(level, colony);
        final PolicyBill bill = state.currentBill().orElseThrow(NO_OPEN_BILL::create);
        if (bill.id() != billId || bill.status() != PolicyBill.Status.OPEN) {
            throw NO_OPEN_BILL.create();
        }

        final Set<UUID> electorate = parliamentElectorate(colony);
        final PolicyBill.VoteResult result = EmpireStateSavedData.get(level).castPolicyVote(
                ColonyIdentity.from(colony), player.getUUID(), approve, electorate);
        switch (result) {
            case RECORDED -> source.sendSuccess(() -> Component.translatable(
                    "imperium.command.vote_recorded", billId, bill.yesVotes(), bill.noVotes()), false);
            case PASSED -> source.sendSuccess(() -> Component.translatable(
                    "imperium.command.bill_passed", billId,
                    Component.translatable(bill.proposedPolicy().translationKey())), false);
            case REJECTED -> source.sendSuccess(() -> Component.translatable(
                    "imperium.command.bill_rejected", billId), false);
            case ALREADY_VOTED -> throw ALREADY_VOTED.create();
            case NOT_ELIGIBLE -> throw NOT_ELIGIBLE_VOTER.create();
            case NOT_OPEN -> throw NO_OPEN_BILL.create();
        }
        return 1;
    }

    private static Set<UUID> parliamentElectorate(final IColony colony) {
        final Set<UUID> eligible = new HashSet<>();
        final var permissions = colony.getPermissions();
        eligible.add(permissions.getOwner());

        final Set<Rank> parliamentaryRanks = new HashSet<>();
        parliamentaryRanks.add(permissions.getRankOwner());
        parliamentaryRanks.add(permissions.getRankOfficer());
        for (final ColonyPlayer member : permissions.getPlayersByRank(parliamentaryRanks)) {
            eligible.add(member.getID());
        }
        return Set.copyOf(eligible);
    }

    private static int showTreasury(final CommandSourceStack source) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final IColony colony = requireManageableColony(player);
        final EmpireState state = stateFor(player.serverLevel(), colony);
        source.sendSuccess(() -> Component.translatable(
                "imperium.command.treasury_status", state.treasuryCrowns()), false);
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
