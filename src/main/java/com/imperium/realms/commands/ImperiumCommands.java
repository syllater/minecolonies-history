package com.imperium.realms.commands;

import com.imperium.realms.ImperiumRealms;
import com.imperium.realms.colony.ColonyIdentity;
import com.imperium.realms.colony.EmpireState;
import com.imperium.realms.colony.EmpireStateSavedData;
import com.imperium.realms.colony.MineColoniesIntegration;
import com.imperium.realms.economy.EmpirePolicy;
import com.imperium.realms.politics.ImperialOfficeSavedData;
import com.imperium.realms.politics.DiplomaticRelation;
import com.imperium.realms.politics.DiplomacyOffer;
import com.imperium.realms.politics.DiplomacySavedData;
import com.imperium.realms.politics.TreatyType;
import com.imperium.realms.politics.FactionType;
import com.imperium.realms.politics.GovernmentType;
import com.imperium.realms.politics.ImperialLaw;
import com.imperium.realms.politics.ParliamentSavedData;
import com.imperium.realms.politics.ParliamentSession;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
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
 * Server-authoritative command interface for the initial economic and parliament systems.
 */
@EventBusSubscriber(modid = ImperiumRealms.MOD_ID)
public final class ImperiumCommands {
    private ImperiumCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(final RegisterCommandsEvent event) {
        final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("imperium")
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

        root.then(Commands.literal("politics")
                .executes(context -> showPoliticsStatus(context.getSource())));

        root.then(Commands.literal("parliament")
                .then(Commands.literal("status")
                        .executes(context -> showParliamentStatus(context.getSource())))
                .then(Commands.literal("propose")
                        .then(Commands.literal("public_works_act")
                                .executes(context -> proposeLaw(context.getSource(), ImperialLaw.PUBLIC_WORKS_ACT)))
                        .then(Commands.literal("scholarship_charter")
                                .executes(context -> proposeLaw(context.getSource(), ImperialLaw.SCHOLARSHIP_CHARTER)))
                        .then(Commands.literal("tax_relief_charter")
                                .executes(context -> proposeLaw(context.getSource(), ImperialLaw.TAX_RELIEF_CHARTER))))
                .then(Commands.literal("vote")
                        .then(Commands.literal("yes")
                                .executes(context -> castParliamentVote(context.getSource(), true)))
                        .then(Commands.literal("no")
                                .executes(context -> castParliamentVote(context.getSource(), false))))
                .then(Commands.literal("resolve")
                        .executes(context -> resolveLaw(context.getSource()))));

        root.then(Commands.literal("emperor")
                .then(Commands.literal("status")
                        .executes(context -> showEmperorStatus(context.getSource())))
                .then(Commands.literal("claim")
                        .executes(context -> claimEmperor(context.getSource())))
                .then(Commands.literal("appoint")
                        .then(Commands.argument("successor", EntityArgument.player())
                                .executes(context -> appointEmperor(
                                        context.getSource(), EntityArgument.getPlayer(context, "successor")))))
                .then(Commands.literal("abdicate")
                        .executes(context -> abdicateEmperor(context.getSource()))));

        root.then(Commands.literal("diplomacy")
                .then(Commands.literal("status")
                        .executes(context -> showDiplomacyStatus(context.getSource())))
                .then(Commands.literal("offer")
                        .then(Commands.argument("colonyId", integer(0))
                                .then(Commands.literal("friendship")
                                        .executes(context -> offerTreaty(context.getSource(),
                                                getInteger(context, "colonyId"), TreatyType.FRIENDSHIP)))
                                .then(Commands.literal("trade_pact")
                                        .executes(context -> offerTreaty(context.getSource(),
                                                getInteger(context, "colonyId"), TreatyType.TRADE_PACT)))
                                .then(Commands.literal("non_aggression")
                                        .executes(context -> offerTreaty(context.getSource(),
                                                getInteger(context, "colonyId"), TreatyType.NON_AGGRESSION)))
                                .then(Commands.literal("alliance")
                                        .executes(context -> offerTreaty(context.getSource(),
                                                getInteger(context, "colonyId"), TreatyType.ALLIANCE)))))
                .then(Commands.literal("accept")
                        .executes(context -> acceptTreaty(context.getSource())))
                .then(Commands.literal("decline")
                        .executes(context -> declineTreaty(context.getSource()))));

        event.getDispatcher().register(root);
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

    private static int setTaxRate(final CommandSourceStack source, final int percent) throws CommandSyntaxException {
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
            final EmpirePolicy policy) throws CommandSyntaxException {
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

    private static int collectTaxes(final CommandSourceStack source) throws CommandSyntaxException {
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




    private static int showDiplomacyStatus(final CommandSourceStack source) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final Optional<IColony> colony = findColony(source, player);
        if (colony.isEmpty()) {
            return 0;
        }

        final ServerLevel level = player.serverLevel();
        final ColonyIdentity identity = ColonyIdentity.from(colony.get());
        final DiplomacySavedData data = DiplomacySavedData.get(level);
        final Optional<DiplomacyOffer> pending = data.pendingOfferFor(identity);
        if (pending.isPresent()) {
            final DiplomacyOffer offer = pending.get();
            source.sendSuccess(() -> Component.translatable(
                    "commands.imperium.diplomacy_pending_offer",
                    colonyNameFor(offer.source(), level),
                    Component.translatable(offer.treaty().translationKey()),
                    offer.proposerName()), false);
        } else {
            source.sendSuccess(() -> Component.translatable(
                    "commands.imperium.diplomacy_no_pending_offer"), false);
        }

        final var relations = data.relationsFor(identity);
        if (relations.isEmpty()) {
            source.sendSuccess(() -> Component.translatable(
                    "commands.imperium.diplomacy_no_relations"), false);
        } else {
            for (final DiplomaticRelation.Snapshot relation : relations) {
                final String name = colonyNameFor(relation.otherColony(), level);
                final Component treaty = Component.translatable(relation.treaty().translationKey());
                final Component stance = Component.translatable("diplomacy.imperium.stance." + relation.stanceId());
                source.sendSuccess(() -> Component.translatable(
                        "commands.imperium.diplomacy_relation",
                        name, treaty, relation.standing(), stance), false);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int offerTreaty(
            final CommandSourceStack source,
            final int targetColonyId,
            final TreatyType treaty) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final Optional<IColony> origin = findManageableColony(source, player);
        if (origin.isEmpty()) {
            return 0;
        }

        final ServerLevel level = player.serverLevel();
        final IColony target = IMinecoloniesAPI.getInstance().getColonyManager()
                .getColonyByWorld(targetColonyId, level);
        if (target == null) {
            source.sendFailure(Component.translatable("commands.imperium.diplomacy_target_missing", targetColonyId));
            return 0;
        }

        final ColonyIdentity fromIdentity = ColonyIdentity.from(origin.get());
        final ColonyIdentity toIdentity = ColonyIdentity.from(target);
        if (fromIdentity.equals(toIdentity)) {
            source.sendFailure(Component.translatable("commands.imperium.diplomacy_self_offer"));
            return 0;
        }

        final boolean offered = DiplomacySavedData.get(level).offer(
                fromIdentity,
                toIdentity,
                treaty,
                player.getUUID(),
                player.getGameProfile().getName(),
                currentGameDay(level));
        if (!offered) {
            source.sendFailure(Component.translatable("commands.imperium.diplomacy_offer_rejected"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable(
                "commands.imperium.diplomacy_offer_sent",
                target.getName(),
                Component.translatable(treaty.translationKey())), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int acceptTreaty(final CommandSourceStack source) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final Optional<IColony> colony = findManageableColony(source, player);
        if (colony.isEmpty()) {
            return 0;
        }

        final ServerLevel level = player.serverLevel();
        final ColonyIdentity identity = ColonyIdentity.from(colony.get());
        final Optional<DiplomaticRelation.Snapshot> accepted = DiplomacySavedData.get(level).accept(
                identity,
                player.getUUID(),
                player.getGameProfile().getName(),
                currentGameDay(level));
        if (accepted.isEmpty()) {
            source.sendFailure(Component.translatable("commands.imperium.diplomacy_accept_rejected"));
            return 0;
        }

        final DiplomaticRelation.Snapshot relation = accepted.get();
        source.sendSuccess(() -> Component.translatable(
                "commands.imperium.diplomacy_offer_accepted",
                colonyNameFor(relation.otherColony(), level),
                Component.translatable(relation.treaty().translationKey())), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int declineTreaty(final CommandSourceStack source) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final Optional<IColony> colony = findManageableColony(source, player);
        if (colony.isEmpty()) {
            return 0;
        }

        final boolean declined = DiplomacySavedData.get(player.serverLevel())
                .decline(ColonyIdentity.from(colony.get()));
        if (!declined) {
            source.sendFailure(Component.translatable("commands.imperium.diplomacy_decline_rejected"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable(
                "commands.imperium.diplomacy_offer_declined"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static String colonyNameFor(final ColonyIdentity identity, final ServerLevel level) {
        if (!identity.dimensionId().equals(level.dimension().location().toString())) {
            return identity.storageKey();
        }
        final IColony colony = IMinecoloniesAPI.getInstance().getColonyManager()
                .getColonyByWorld(identity.colonyId(), level);
        return colony == null ? identity.storageKey() : colony.getName();
    }

    private static int showEmperorStatus(final CommandSourceStack source) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final Optional<IColony> colony = findColony(source, player);
        if (colony.isEmpty()) {
            return 0;
        }

        final var office = ImperialOfficeSavedData.get(player.serverLevel())
                .office(ColonyIdentity.from(colony.get()));
        final Component emperor = office.emperorName()
                .<Component>map(Component::literal)
                .orElseGet(() -> Component.translatable("commands.imperium.emperor_vacant"));
        source.sendSuccess(() -> Component.translatable(
                "commands.imperium.emperor_status",
                colony.get().getName(),
                emperor,
                office.reignCount(),
                office.abdications()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int claimEmperor(final CommandSourceStack source) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final Optional<IColony> colony = findManageableColony(source, player);
        if (colony.isEmpty()) {
            return 0;
        }

        final ColonyIdentity identity = ColonyIdentity.from(colony.get());
        final boolean claimed = ImperialOfficeSavedData.get(player.serverLevel()).claim(
                identity, player.getUUID(), player.getGameProfile().getName(), currentGameDay(player.serverLevel()));
        if (!claimed) {
            source.sendFailure(Component.translatable("commands.imperium.emperor_claim_rejected"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable(
                "commands.imperium.emperor_claimed", player.getGameProfile().getName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int appointEmperor(
            final CommandSourceStack source,
            final ServerPlayer successor) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final Optional<IColony> colony = findColony(source, player);
        if (colony.isEmpty()) {
            return 0;
        }
        if (!colony.get().getPermissions().isColonyMember(player)) {
            source.sendFailure(Component.translatable("commands.imperium.parliament_not_member"));
            return 0;
        }
        if (!colony.get().getPermissions().isColonyMember(successor)) {
            source.sendFailure(Component.translatable("commands.imperium.emperor_successor_not_member"));
            return 0;
        }

        final boolean appointed = ImperialOfficeSavedData.get(player.serverLevel()).appoint(
                ColonyIdentity.from(colony.get()),
                player.getUUID(),
                successor.getUUID(),
                successor.getGameProfile().getName(),
                currentGameDay(player.serverLevel()));
        if (!appointed) {
            source.sendFailure(Component.translatable("commands.imperium.emperor_appoint_rejected"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable(
                "commands.imperium.emperor_appointed",
                successor.getGameProfile().getName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int abdicateEmperor(final CommandSourceStack source) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final Optional<IColony> colony = findColony(source, player);
        if (colony.isEmpty()) {
            return 0;
        }

        final boolean abdicated = ImperialOfficeSavedData.get(player.serverLevel()).abdicate(
                ColonyIdentity.from(colony.get()), player.getUUID());
        if (!abdicated) {
            source.sendFailure(Component.translatable("commands.imperium.emperor_abdicate_rejected"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable(
                "commands.imperium.emperor_abdicated", player.getGameProfile().getName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int showPoliticsStatus(final CommandSourceStack source) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final Optional<IColony> colony = findColony(source, player);
        if (colony.isEmpty()) {
            return 0;
        }

        final EmpireState state = stateFor(player.serverLevel(), colony.get());
        source.sendSuccess(() -> Component.translatable(
                "commands.imperium.politics_status",
                state.colonyName(),
                state.citizenApproval(),
                state.unrest()), false);
        for (final FactionType faction : FactionType.values()) {
            final int support = state.factionSupport(faction);
            source.sendSuccess(() -> Component.translatable(
                    "commands.imperium.faction_support",
                    Component.translatable(faction.translationKey()),
                    support), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int showParliamentStatus(final CommandSourceStack source) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final Optional<IColony> colony = findColony(source, player);
        if (colony.isEmpty()) {
            return 0;
        }

        final ColonyIdentity identity = ColonyIdentity.from(colony.get());
        final ParliamentSavedData.ParliamentSnapshot parliament =
                ParliamentSavedData.get(player.serverLevel()).snapshot(identity);
        final Component proposal = parliament.hasActiveProposal()
                ? Component.translatable("law.imperium." + parliament.proposalLawId())
                : Component.translatable("commands.imperium.parliament_no_active_proposal");
        source.sendSuccess(() -> Component.translatable(
                "commands.imperium.parliament_status",
                Component.translatable(GovernmentType.CONSTITUTIONAL_EMPIRE.translationKey()),
                proposal,
                parliament.yesVotes(),
                parliament.noVotes(),
                parliament.votesCast()), false);
        if (!parliament.enactedLawIds().isEmpty()) {
            source.sendSuccess(() -> Component.translatable(
                    "commands.imperium.parliament_enacted",
                    String.join(", ", parliament.enactedLawIds().stream().sorted().toList())), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int proposeLaw(
            final CommandSourceStack source,
            final ImperialLaw law) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final Optional<IColony> colony = findManageableColony(source, player);
        if (colony.isEmpty()) {
            return 0;
        }

        final ServerLevel level = player.serverLevel();
        final ColonyIdentity identity = ColonyIdentity.from(colony.get());
        final long gameDay = currentGameDay(level);
        final boolean proposed = ParliamentSavedData.get(level).propose(
                identity, law, player.getUUID(), gameDay);
        if (!proposed) {
            source.sendFailure(Component.translatable("commands.imperium.parliament_proposal_rejected"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable(
                "commands.imperium.parliament_proposal_opened",
                Component.translatable(law.translationKey())), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int castParliamentVote(
            final CommandSourceStack source,
            final boolean inFavor) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final Optional<IColony> colony = findColony(source, player);
        if (colony.isEmpty()) {
            return 0;
        }
        if (!colony.get().getPermissions().isColonyMember(player)) {
            source.sendFailure(Component.translatable("commands.imperium.parliament_not_member"));
            return 0;
        }

        final ColonyIdentity identity = ColonyIdentity.from(colony.get());
        final boolean accepted = ParliamentSavedData.get(player.serverLevel()).castVote(
                identity, player.getUUID(), inFavor);
        if (!accepted) {
            source.sendFailure(Component.translatable("commands.imperium.parliament_vote_rejected"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable(
                inFavor ? "commands.imperium.parliament_vote_yes" : "commands.imperium.parliament_vote_no"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int resolveLaw(final CommandSourceStack source) throws CommandSyntaxException {
        final ServerPlayer player = source.getPlayerOrException();
        final Optional<IColony> colony = findManageableColony(source, player);
        if (colony.isEmpty()) {
            return 0;
        }

        final ServerLevel level = player.serverLevel();
        final ColonyIdentity identity = ColonyIdentity.from(colony.get());
        final ParliamentSavedData parliament = ParliamentSavedData.get(level);
        final ParliamentSavedData.ParliamentSnapshot before = parliament.snapshot(identity);
        if (!before.hasActiveProposal()) {
            source.sendFailure(Component.translatable("commands.imperium.parliament_no_active_proposal"));
            return 0;
        }

        final Optional<ParliamentSession.Resolution> resolution = parliament.resolve(identity, currentGameDay(level));
        if (resolution.isEmpty()) {
            source.sendFailure(Component.translatable("commands.imperium.parliament_too_early"));
            return 0;
        }

        final ParliamentSession.Resolution result = resolution.get();
        if (result.passed()) {
            final ImperialLaw law = ImperialLaw.fromId(result.lawId()).orElseThrow();
            law.apply(EmpireStateSavedData.get(level), identity);
            source.sendSuccess(() -> Component.translatable(
                    "commands.imperium.parliament_law_passed",
                    Component.translatable(law.translationKey()),
                    result.yesVotes(),
                    result.noVotes()), true);
        } else {
            source.sendSuccess(() -> Component.translatable(
                    "commands.imperium.parliament_law_failed",
                    Component.translatable("law.imperium." + result.lawId()),
                    result.yesVotes(),
                    result.noVotes()), true);
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

    private static long currentGameDay(final ServerLevel level) {
        return Math.floorDiv(level.getServer().overworld().getGameTime(), 24_000L);
    }
}
