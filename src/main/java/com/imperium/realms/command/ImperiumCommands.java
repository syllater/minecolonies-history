package com.imperium.realms.command;

import com.imperium.realms.ImperiumRealms;
import com.imperium.realms.colony.ColonyIdentity;
import com.imperium.realms.colony.EconomicPolicy;
import com.imperium.realms.colony.EmpireState;
import com.imperium.realms.colony.EmpireStateSavedData;
import com.imperium.realms.colony.MineColoniesIntegration;
import com.imperium.realms.colony.ParliamentProposal;
import com.imperium.realms.colony.ProvinceFocus;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
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

import java.util.List;

/** Server-authoritative commands for the ledger and first parliament. */
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
                                .executes(context -> proposeTaxRate(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "percent")))))
                .then(Commands.literal("policy")
                        .then(Commands.argument("policy", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        new String[]{"balanced", "mercantile", "welfare", "austerity"}, builder))
                                 .executes(context -> proposePolicy(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "policy")))))
                .then(Commands.literal("invest")
                        .then(Commands.argument("crowns", IntegerArgumentType.integer(10, 100_000))
                                .executes(context -> invest(
                                        context.getSource(),
                                        IntegerArgumentType.getInteger(context, "crowns")))))
                .then(Commands.literal("army")
                        .executes(context -> showArmy(context.getSource())))
                .then(Commands.literal("politics")
                        .executes(context -> showPolitics(context.getSource())))
                .then(Commands.literal("diplomacy")
                        .then(Commands.literal("status")
                                .executes(context -> showDiplomacy(context.getSource())))
                        .then(Commands.literal("improve")
                                .then(Commands.argument("colonyId", IntegerArgumentType.integer(0))
                                        .executes(context -> improveDiplomacy(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "colonyId"))))))
                .then(Commands.literal("province")
                        .then(Commands.literal("status")
                                .executes(context -> showProvince(context.getSource())))
                        .then(Commands.literal("focus")
                                .then(Commands.argument("focus", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                new String[]{"agriculture", "trade", "scholarship", "military", "civic"}, builder))
                                        .executes(context -> setProvinceFocus(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "focus")))))
                        .then(Commands.literal("develop")
                                .executes(context -> developProvince(context.getSource()))))
                .then(Commands.literal("parliament")
                        .then(Commands.literal("status")
                                .executes(context -> showParliament(context.getSource())))
                        .then(Commands.literal("propose-tax")
                                .then(Commands.argument("percent", IntegerArgumentType.integer(0, 25))
                                        .executes(context -> proposeTaxRate(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "percent")))))
                        .then(Commands.literal("propose-policy")
                                .then(Commands.argument("policy", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                new String[]{"balanced", "mercantile", "welfare", "austerity"}, builder))
                                        .executes(context -> proposePolicy(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "policy")))))
                        .then(Commands.literal("assent")
                                .then(Commands.argument("id", LongArgumentType.longArg(1L))
                                        .executes(context -> resolveProposal(
                                                context.getSource(),
                                                LongArgumentType.getLong(context, "id"),
                                                true))))
                        .then(Commands.literal("veto")
                                .then(Commands.argument("id", LongArgumentType.longArg(1L))
                                        .executes(context -> resolveProposal(
                                                context.getSource(),
                                                LongArgumentType.getLong(context, "id"),
                                                false))))));
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
                state.stability(),
                state.legitimacy(),
                state.taxCollectionEfficiencyPercent(),
                state.diplomaticInfluence(),
                state.unrest(),
                state.civicDisorder().name().toLowerCase(java.util.Locale.ROOT)), false);
        return 1;
    }

    /**
     * Historical alias retained for the GUI: a tax button now proposes a bill
     * rather than bypassing the parliamentary process.
     */
    private static int proposeTaxRate(final CommandSourceStack source, final int rate) {
        final ColonyContext context = resolveColony(source);
        if (context == null) {
            return 0;
        }
        if (!mayManageEconomy(context)) {
            return denyPermission(source);
        }

        final long day = currentDay(source);
        final ParliamentProposal proposal = context.state()
                .createTaxProposal(context.player().getGameProfile().getName(), rate, day)
                .orElse(null);
        if (proposal == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.tax_proposal_invalid",
                    rate, context.state().taxRatePercent()));
            return 0;
        }

        context.data().markChanged();
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.tax_proposed",
                proposal.id(),
                proposal.proposedTaxRate(),
                proposal.councilYesVotes(),
                proposal.councilNoVotes(),
                proposal.expiresDay()), true);
        return 1;
    }

    private static int showParliament(final CommandSourceStack source) {
        final ColonyContext context = resolveColony(source);
        if (context == null) {
            return 0;
        }

        final long day = currentDay(source);
        if (context.state().expireParliamentProposals(day)) {
            context.data().markChanged();
        }
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.parliament_header",
                context.state().colonyName(),
                context.state().taxRatePercent()), false);

        final List<ParliamentProposal> proposals = context.state().recentParliamentProposals();
        if (proposals.isEmpty()) {
            source.sendSuccess(() -> Component.translatable(
                    "imperium_realms.message.parliament_empty"), false);
            return 1;
        }

        for (final ParliamentProposal proposal : proposals) {
            final Component proposalType = Component.translatable(
                    "imperium_realms.parliament.type." + proposal.typeId());
            source.sendSuccess(() -> Component.translatable(
                    "imperium_realms.message.parliament_proposal",
                    proposal.id(),
                    proposalType,
                    proposal.valueId(),
                    proposal.proposer(),
                    proposal.councilYesVotes(),
                    proposal.councilNoVotes(),
                    proposal.statusId(),
                    proposal.expiresDay(),
                    proposal.resolvedBy().isBlank() ? "-" : proposal.resolvedBy(),
                    proposal.resolvedDay() < 0L ? "-" : Long.toString(proposal.resolvedDay())), false);
        }
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.parliament_instructions"), false);
        return 1;
    }

    private static int resolveProposal(
            final CommandSourceStack source,
            final long proposalId,
            final boolean emperorAssents) {
        final ColonyContext context = resolveColony(source);
        if (context == null) {
            return 0;
        }
        if (!mayManageEconomy(context)) {
            return denyPermission(source);
        }

        final ParliamentProposal proposal = context.state().findProposal(proposalId).orElse(null);
        final EmpireState.ProposalResolution result = context.state().resolveProposal(
                proposalId, emperorAssents, currentDay(source), context.player().getGameProfile().getName());
        if (result == EmpireState.ProposalResolution.NOT_FOUND) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.parliament_proposal_not_found", proposalId));
            return 0;
        }
        if (result == EmpireState.ProposalResolution.ALREADY_RESOLVED) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.parliament_proposal_closed", proposalId));
            return 0;
        }

        context.data().markChanged();
        switch (result) {
            case PASSED -> {
                if (proposal != null && proposal.type() == ParliamentProposal.Type.ECONOMIC_POLICY) {
                    source.sendSuccess(() -> Component.translatable(
                            "imperium_realms.message.parliament_policy_passed",
                            proposalId,
                            context.state().economicPolicy().id()), true);
                } else {
                    source.sendSuccess(() -> Component.translatable(
                            "imperium_realms.message.parliament_proposal_passed",
                            proposalId,
                            context.state().taxRatePercent()), true);
                }
            }
            case EXPIRED -> source.sendFailure(Component.translatable(
                    "imperium_realms.message.parliament_proposal_expired", proposalId));
            case REJECTED -> {
                if (emperorAssents) {
                    source.sendFailure(Component.translatable(
                            "imperium_realms.message.parliament_no_majority", proposalId));
                } else {
                    source.sendSuccess(() -> Component.translatable(
                            "imperium_realms.message.parliament_vetoed", proposalId), true);
                }
            }
            default -> source.sendFailure(Component.translatable(
                    "imperium_realms.message.parliament_proposal_closed", proposalId));
        }
        return result == EmpireState.ProposalResolution.PASSED ? 1 : 0;
    }

    private static int proposePolicy(final CommandSourceStack source, final String requestedPolicy) {
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
        final ParliamentProposal proposal = context.state()
                .createPolicyProposal(context.player().getGameProfile().getName(), policy, currentDay(source))
                .orElse(null);
        if (proposal == null) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.policy_proposal_invalid",
                    policy.id(),
                    context.state().economicPolicy().id()));
            return 0;
        }

        context.data().markChanged();
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.policy_proposed",
                proposal.id(),
                policy.id(),
                proposal.councilYesVotes(),
                proposal.councilNoVotes(),
                proposal.expiresDay()), true);
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

    private static int showProvince(final CommandSourceStack source) {
        final ColonyContext context = resolveColony(source);
        if (context == null) {
            return 0;
        }

        final EmpireState state = context.state();
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.province_status",
                state.colonyName(),
                Component.translatable("imperium_realms.province_tier." + state.provinceTierId()),
                state.provinceDevelopmentPoints(),
                Component.translatable("imperium_realms.province_focus." + state.provinceFocus().id()),
                state.knowledgePoints()), false);
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.province_help"), false);
        return 1;
    }

    private static int setProvinceFocus(
            final CommandSourceStack source,
            final String requestedFocus) {
        final ColonyContext context = resolveColony(source);
        if (context == null) {
            return 0;
        }
        if (!mayManageEconomy(context)) {
            return denyPermission(source);
        }

        final ProvinceFocus focus = ProvinceFocus.fromId(requestedFocus).orElse(null);
        if (focus == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.province_focus_unknown"));
            return 0;
        }
        if (!context.state().setProvinceFocus(focus)) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.province_focus_unchanged",
                    Component.translatable("imperium_realms.province_focus." + focus.id())));
            return 0;
        }

        context.data().markChanged();
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.province_focus_set",
                Component.translatable("imperium_realms.province_focus." + focus.id())), true);
        return 1;
    }

    private static int developProvince(final CommandSourceStack source) {
        final ColonyContext context = resolveColony(source);
        if (context == null) {
            return 0;
        }
        if (!mayManageEconomy(context)) {
            return denyPermission(source);
        }

        if (context.state().provinceDevelopmentPoints() >= 1_000) {
            source.sendFailure(Component.translatable("imperium_realms.message.province_fully_developed"));
            return 0;
        }
        if (context.state().knowledgePoints() < 10L) {
            source.sendFailure(Component.translatable("imperium_realms.message.province_knowledge_required"));
            return 0;
        }
        if (!context.state().developProvince()) {
            source.sendFailure(Component.translatable("imperium_realms.message.province_development_failed"));
            return 0;
        }

        context.data().markChanged();
        final EmpireState state = context.state();
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.province_developed",
                Component.translatable("imperium_realms.province_tier." + state.provinceTierId()),
                state.provinceDevelopmentPoints(),
                state.knowledgePoints()), true);
        return 1;
    }

    private static int showArmy(final CommandSourceStack source) {
        final ColonyContext context = resolveColony(source);
        if (context == null) {
            return 0;
        }

        final EmpireState state = context.state();
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.army_status",
                state.colonyName(),
                state.siegeEngineeringPoints(),
                state.fieldMedicinePoints(),
                state.cavalryDrillPoints()), false);
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.army_help"), false);
        return 1;
    }

    private static int showPolitics(final CommandSourceStack source) {
        final ColonyContext context = resolveColony(source);
        if (context == null) {
            return 0;
        }

        final EmpireState state = context.state();
        final Component disorder = Component.translatable(
                "imperium_realms.civic_disorder."
                        + state.civicDisorder().name().toLowerCase(java.util.Locale.ROOT));
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.politics_header",
                state.colonyName(),
                state.stability(),
                state.legitimacy(),
                state.unrest(),
                disorder), false);

        for (final var faction : state.factionApproval().entrySet()) {
            source.sendSuccess(() -> Component.translatable(
                    "imperium_realms.message.faction_approval",
                    Component.translatable("imperium_realms.faction." + faction.getKey()),
                    faction.getValue()), false);
        }
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.politics_help"), false);
        return 1;
    }

    private static int showDiplomacy(final CommandSourceStack source) {
        final ColonyContext context = resolveColony(source);
        if (context == null) {
            return 0;
        }

        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.diplomacy_header",
                context.state().colonyName(),
                context.state().diplomaticInfluence()), false);

        final var relations = context.state().diplomaticRelations();
        if (relations.isEmpty()) {
            source.sendSuccess(() -> Component.translatable(
                    "imperium_realms.message.diplomacy_empty"), false);
        } else {
            relations.forEach((target, score) -> {
                final Component status = Component.translatable(
                        "imperium_realms.diplomacy.relation."
                                + EmpireState.relationStatusId(score));
                source.sendSuccess(() -> Component.translatable(
                        "imperium_realms.message.diplomacy_relation",
                        target.dimensionId(),
                        target.colonyId(),
                        score,
                        status), false);
            });
        }
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.diplomacy_instructions"), false);
        return 1;
    }

    private static int improveDiplomacy(final CommandSourceStack source, final int targetColonyId) {
        final ColonyContext context = resolveColony(source);
        if (context == null) {
            return 0;
        }
        if (!mayManageEconomy(context)) {
            return denyPermission(source);
        }

        final IColony target = MineColoniesIntegration
                .colonyById(context.player().serverLevel(), targetColonyId).orElse(null);
        if (target == null) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.diplomacy_target_missing", targetColonyId));
            return 0;
        }

        final ColonyIdentity targetIdentity = ColonyIdentity.from(target);
        if (context.state().identity().equals(targetIdentity)) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.diplomacy_self_target"));
            return 0;
        }
        if (context.state().diplomaticInfluence() < 10L) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.diplomacy_influence_insufficient"));
            return 0;
        }
        if (context.state().relationScore(targetIdentity) >= 100) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.diplomacy_relation_max", target.getName()));
            return 0;
        }
        if (!context.state().improveDiplomaticRelations(targetIdentity)) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.diplomacy_improve_failed"));
            return 0;
        }

        context.data().markChanged();
        final int score = context.state().relationScore(targetIdentity);
        final Component status = Component.translatable(
                "imperium_realms.diplomacy.relation." + EmpireState.relationStatusId(score));
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.diplomacy_improved",
                target.getName(),
                status,
                score,
                context.state().diplomaticInfluence()), true);
        return 1;
    }

    private static long currentDay(final CommandSourceStack source) {
        return Math.max(0L, Math.floorDiv(source.getServer().overworld().getGameTime(), 24_000L));
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
