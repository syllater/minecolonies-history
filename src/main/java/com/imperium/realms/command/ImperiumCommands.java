package com.imperium.realms.command;

import com.imperium.realms.ImperiumRealms;
import com.imperium.realms.colony.ColonyIdentity;
import com.imperium.realms.colony.EconomicPolicy;
import com.imperium.realms.colony.ImperialAuditEntry;
import com.imperium.realms.colony.EmpireState;
import com.imperium.realms.colony.EmpireStateSavedData;
import com.imperium.realms.colony.EmpireRealm;
import com.imperium.realms.colony.MineColoniesIntegration;
import com.imperium.realms.colony.MilitaryCampaign;
import com.imperium.realms.colony.ParliamentProposal;
import com.imperium.realms.colony.ProvinceGovernor;
import com.imperium.realms.colony.ProvinceFocus;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.permissions.Action;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
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
                .then(Commands.literal("empire")
                        .then(Commands.literal("status")
                                .executes(context -> showEmpire(context.getSource())))
                        .then(Commands.literal("audit")
                                .executes(context -> showEmpireAudit(context.getSource())))
                        .then(Commands.literal("events")
                                .executes(context -> showRegionalEvents(context.getSource())))
                        .then(Commands.literal("petitions")
                                .executes(context -> showSeparatistPetitions(context.getSource())))
                        .then(Commands.literal("resolve")
                                .then(Commands.argument("colonyId", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("response", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                        List.of("reassure", "crackdown"), builder))
                                                .executes(context -> resolveSeparatistPetition(
                                                        context.getSource(),
                                                        IntegerArgumentType.getInteger(context, "colonyId"),
                                                        StringArgumentType.getString(context, "response"))))))
                        .then(Commands.literal("found")
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(context -> foundEmpire(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "name")))))
                        .then(Commands.literal("invite")
                                .then(Commands.argument("colonyId", IntegerArgumentType.integer(0))
                                        .executes(context -> inviteEmpireProvince(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "colonyId")))))
                        .then(Commands.literal("join")
                                .executes(context -> joinEmpire(context.getSource())))
                        .then(Commands.literal("leave")
                                .executes(context -> leaveEmpire(context.getSource())))
                        .then(Commands.literal("deposit")
                                .then(Commands.argument("crowns",
                                                LongArgumentType.longArg(1L, EmpireState.MAX_TREASURY))
                                        .executes(context -> depositEmpireTreasury(
                                                context.getSource(),
                                                LongArgumentType.getLong(context, "crowns")))))
                        .then(Commands.literal("withdraw")
                                .then(Commands.argument("crowns",
                                                LongArgumentType.longArg(1L, EmpireState.MAX_TREASURY))
                                        .executes(context -> withdrawEmpireTreasury(
                                                context.getSource(),
                                                LongArgumentType.getLong(context, "crowns"))))))
                .then(Commands.literal("governor")
                        .then(Commands.literal("status")
                                .executes(context -> showGovernors(context.getSource())))
                        .then(Commands.literal("appoint")
                                .then(Commands.argument("colonyId", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("governor", EntityArgument.player())
                                                .executes(context -> appointGovernor(
                                                        context.getSource(),
                                                        IntegerArgumentType.getInteger(context, "colonyId"),
                                                        EntityArgument.getPlayer(context, "governor"))))))
                        .then(Commands.literal("dismiss")
                                .then(Commands.argument("colonyId", IntegerArgumentType.integer(0))
                                        .executes(context -> dismissGovernor(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "colonyId"))))))
                .then(Commands.literal("campaign")
                        .then(Commands.literal("status")
                                .executes(context -> showCampaigns(context.getSource())))
                        .then(Commands.literal("launch")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                new String[]{"border_patrol", "relief_expedition", "war_campaign"}, builder))
                                        .then(Commands.argument("colonyId", IntegerArgumentType.integer(0))
                                                .executes(context -> launchCampaign(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "type"),
                                                        IntegerArgumentType.getInteger(context, "colonyId")))))))
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
        final EmpireRealm realm = context.data().realmForProvince(context.state().identity()).orElse(null);
        if (realm != null && realm.capital().equals(context.state().identity())) {
            source.sendSuccess(() -> Component.translatable(
                    "imperium_realms.message.parliament_scope_imperial", realm.name(), realm.provinceCount()), false);
        } else if (realm != null) {
            source.sendSuccess(() -> Component.translatable(
                    "imperium_realms.message.parliament_scope_provincial", realm.name()), false);
        } else {
            source.sendSuccess(() -> Component.translatable(
                    "imperium_realms.message.parliament_scope_local"), false);
        }

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
        final EmpireRealm realm = context.data().realmForProvince(context.state().identity()).orElse(null);
        final boolean imperialCapital = realm != null
                && realm.capital().equals(context.state().identity());
        if (imperialCapital && !mayManageRealm(context, realm)) {
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
        if (result == EmpireState.ProposalResolution.PASSED && imperialCapital && proposal != null) {
            final boolean lawApplied = proposal.type() == ParliamentProposal.Type.TAX_RATE
                    ? context.data().applyImperialTaxLaw(realm.id(), context.state().taxRatePercent())
                    : context.data().applyImperialPolicyLaw(realm.id(), context.state().economicPolicy());
            if (!lawApplied) {
                source.sendFailure(Component.translatable("imperium_realms.message.empire_law_apply_failed"));
                return 0;
            }
            context.data().recordImperialAudit(realm.id(), currentDay(source),
                    context.player().getGameProfile().getName(),
                    proposal.type() == ParliamentProposal.Type.TAX_RATE ? "tax-law" : "policy-law",
                    proposal.valueId(), proposal.type() == ParliamentProposal.Type.TAX_RATE
                            ? context.state().taxRatePercent() : 0L);
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
                if (imperialCapital && proposal != null) {
                    final Component lawType = Component.translatable(
                            "imperium_realms.parliament.type." + proposal.typeId());
                    source.sendSuccess(() -> Component.translatable(
                            "imperium_realms.message.empire_law_applied",
                            lawType, proposal.valueId(), realm.provinceCount(), realm.name()), true);
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

    private static int showEmpire(final CommandSourceStack source) {
        final ColonyContext context = resolveColony(source);
        if (context == null) return 0;

        final EmpireRealm realm = context.data().realmForProvince(context.state().identity()).orElse(null);
        if (realm == null) {
            source.sendSuccess(() -> Component.translatable("imperium_realms.message.empire_none"), false);
            source.sendSuccess(() -> Component.translatable("imperium_realms.message.empire_found_help"), false);
            return 1;
        }

        long treasury = 0L;
        long knowledge = 0L;
        long stabilityTotal = 0L;
        int included = 0;
        for (final ColonyIdentity province : realm.provinces()) {
            final EmpireState provinceState = context.data().get(province).orElse(null);
            if (provinceState == null) continue;
            treasury = safeAdd(treasury, provinceState.treasuryCrowns());
            knowledge = safeAdd(knowledge, provinceState.knowledgePoints());
            stabilityTotal += provinceState.stability();
            included++;
        }
        final int averageStability = included == 0 ? 0 : (int) (stabilityTotal / included);
        final long realmTreasury = treasury;
        final long realmKnowledge = knowledge;
        final int realmAverageStability = averageStability;
        final long imperialTreasury = realm.imperialTreasuryCrowns();
        final EmpireState capitalState = context.data().get(realm.capital()).orElse(null);
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.empire_status",
                realm.name(), realm.id(), realm.emperorName(),
                capitalState == null ? realm.capital().storageKey() : capitalState.colonyName(),
                realm.provinceCount(), imperialTreasury, realmTreasury, realmKnowledge, realmAverageStability), false);
        final Component imperialTaxLaw = realm.hasImperialTaxLaw()
                ? Component.literal(realm.imperialTaxRatePercent() + "%")
                : Component.translatable("imperium_realms.message.empire_law_unset");
        final Component imperialPolicyLaw = realm.hasImperialPolicyLaw()
                ? Component.literal(realm.imperialEconomicPolicyId())
                : Component.translatable("imperium_realms.message.empire_law_unset");
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.empire_law_status", imperialTaxLaw, imperialPolicyLaw), false);
        for (final ColonyIdentity province : realm.provinces()) {
            final EmpireState provinceState = context.data().get(province).orElse(null);
            final String provinceName = provinceState == null ? province.storageKey() : provinceState.colonyName();
            source.sendSuccess(() -> Component.translatable(
                    "imperium_realms.message.empire_province",
                    provinceName, province.dimensionId(), province.colonyId(), province.equals(realm.capital())), false);
            final Component petitionStatus = realm.hasSeparatistPetition(province)
                    ? Component.translatable("imperium_realms.message.empire_petition_pending")
                    : Component.translatable("imperium_realms.message.empire_petition_none");
            final int loyalty = realm.provincialLoyalty(province);
            source.sendSuccess(() -> Component.translatable(
                    "imperium_realms.message.empire_province_loyalty", provinceName, loyalty, petitionStatus), false);
        }
        source.sendSuccess(() -> Component.translatable("imperium_realms.message.empire_help"), false);
        return 1;
    }

    private static int showSeparatistPetitions(final CommandSourceStack source) {
        final ColonyContext context = resolveColony(source);
        if (context == null) return 0;
        final EmpireRealm realm = context.data().realmForProvince(context.state().identity()).orElse(null);
        if (realm == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_none"));
            return 0;
        }
        final List<java.util.Map.Entry<ColonyIdentity, Long>> petitions =
                realm.separatistPetitions().entrySet().stream().toList();
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.empire_petitions_header", realm.name(), petitions.size()), false);
        if (petitions.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("imperium_realms.message.empire_petitions_empty"), false);
            return 1;
        }
        for (final java.util.Map.Entry<ColonyIdentity, Long> petition : petitions) {
            final EmpireState state = context.data().get(petition.getKey()).orElse(null);
            final String provinceName = state == null ? petition.getKey().storageKey() : state.colonyName();
            final int loyalty = realm.provincialLoyalty(petition.getKey());
            source.sendSuccess(() -> Component.translatable(
                    "imperium_realms.message.empire_petition_entry",
                    provinceName, petition.getKey().colonyId(), loyalty, petition.getValue()), false);
        }
        source.sendSuccess(() -> Component.translatable("imperium_realms.message.empire_petition_help"), false);
        return 1;
    }

    private static int resolveSeparatistPetition(
            final CommandSourceStack source, final int targetColonyId, final String requestedResponse) {
        final ColonyContext context = resolveColony(source);
        if (context == null) return 0;
        final EmpireRealm realm = context.data().realmForProvince(context.state().identity()).orElse(null);
        if (realm == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_none"));
            return 0;
        }
        if (!mayManageRealm(context, realm)) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_emperor_only"));
            return 0;
        }
        final String response = requestedResponse.toLowerCase(java.util.Locale.ROOT);
        if (!response.equals("reassure") && !response.equals("crackdown")) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_petition_invalid_response"));
            return 0;
        }
        final IColony target = MineColoniesIntegration
                .colonyById(context.player().serverLevel(), targetColonyId).orElse(null);
        if (target == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_target_not_found", targetColonyId));
            return 0;
        }
        final ColonyIdentity province = ColonyIdentity.from(target);
        final EmpireStateSavedData.PetitionResolutionResult result = context.data().resolveSeparatistPetition(
                realm.id(), province, response.equals("crackdown"),
                context.player().getGameProfile().getName(), currentDay(source));
        switch (result) {
            case RESOLVED -> {
                final int newLoyalty = realm.provincialLoyalty(province);
                final long cost = response.equals("crackdown") ? 25L : 50L;
                final Component policy = Component.translatable(
                        response.equals("crackdown")
                                ? "imperium_realms.message.empire_petition_crackdown"
                                : "imperium_realms.message.empire_petition_reassured",
                        target.getName(), cost, newLoyalty);
                source.sendSuccess(() -> policy, true);
                return 1;
            }
            case INSUFFICIENT_TREASURY -> source.sendFailure(Component.translatable(
                    "imperium_realms.message.empire_petition_funds", response.equals("crackdown") ? 25 : 50));
            case NO_PETITION -> source.sendFailure(Component.translatable("imperium_realms.message.empire_petition_missing"));
            case CAPITAL_PROVINCE -> source.sendFailure(Component.translatable("imperium_realms.message.governor_capital"));
            case NOT_MEMBER -> source.sendFailure(Component.translatable("imperium_realms.message.governor_not_member"));
            case REALM_NOT_FOUND -> source.sendFailure(Component.translatable("imperium_realms.message.empire_none"));
        }
        return 0;
    }

    private static int showRegionalEvents(final CommandSourceStack source) {
        final ColonyContext context = resolveColony(source);
        if (context == null) return 0;
        final EmpireRealm realm = context.data().realmForProvince(context.state().identity()).orElse(null);
        if (realm == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_none"));
            return 0;
        }
        final List<ImperialAuditEntry> events = realm.recentAuditEntries().stream()
                .filter(entry -> "regional-event".equals(entry.actionId()))
                .limit(5)
                .toList();
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.empire_events_header", realm.name(), events.size()), false);
        if (events.isEmpty()) {
            source.sendSuccess(() -> Component.translatable(
                    "imperium_realms.message.empire_events_empty"), false);
            return 1;
        }
        for (final ImperialAuditEntry entry : events) {
            final Component event = Component.translatable(
                    "imperium_realms.regional_event." + entry.subject());
            source.sendSuccess(() -> Component.translatable(
                    "imperium_realms.message.empire_event_entry",
                    entry.dayIndex(), event, entry.amount()), false);
        }
        return 1;
    }

    private static int showEmpireAudit(final CommandSourceStack source) {
        final ColonyContext context = resolveColony(source);
        if (context == null) return 0;
        if (!mayManageEconomy(context)) return denyPermission(source);
        final EmpireRealm realm = context.data().realmForProvince(context.state().identity()).orElse(null);
        if (realm == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_none"));
            return 0;
        }
        final List<ImperialAuditEntry> entries = realm.recentAuditEntries().stream().limit(10).toList();
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.empire_audit_header", realm.name(), entries.size()), false);
        if (entries.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("imperium_realms.message.empire_audit_empty"), false);
            return 1;
        }
        for (final ImperialAuditEntry entry : entries) {
            final Component action = Component.translatable("imperium_realms.audit.action." + entry.actionId());
            source.sendSuccess(() -> Component.translatable(
                    "imperium_realms.message.empire_audit_entry",
                    entry.dayIndex(), action, entry.actor(), entry.subject(), entry.amount()), false);
        }
        return 1;
    }

    private static int foundEmpire(final CommandSourceStack source, final String name) {
        final ColonyContext context = resolveColony(source);
        if (context == null) return 0;
        if (!mayManageEconomy(context)) return denyPermission(source);

        final ColonyIdentity identity = context.state().identity();
        if (context.data().realmForProvince(identity).isPresent()) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_already_member"));
            return 0;
        }
        final EmpireRealm realm = context.data().createRealm(
                name, identity, context.player().getUUID().toString(),
                context.player().getGameProfile().getName(), currentDay(source)).orElse(null);
        if (realm == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_found_failed"));
            return 0;
        }
        context.data().recordImperialAudit(realm.id(), currentDay(source),
                context.player().getGameProfile().getName(), "found", realm.name(), 0L);
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.empire_founded", realm.name(), realm.id()), true);
        return 1;
    }

    private static int inviteEmpireProvince(final CommandSourceStack source, final int targetColonyId) {
        final ColonyContext context = resolveColony(source);
        if (context == null) return 0;
        final EmpireRealm realm = context.data().realmForProvince(context.state().identity()).orElse(null);
        if (realm == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_none"));
            return 0;
        }
        if (!mayManageRealm(context, realm)) return denyPermission(source);

        final IColony target = MineColoniesIntegration
                .colonyById(context.player().serverLevel(), targetColonyId).orElse(null);
        if (target == null) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.empire_target_not_found", targetColonyId));
            return 0;
        }
        final ColonyIdentity targetIdentity = ColonyIdentity.from(target);
        if (context.data().realmForProvince(targetIdentity).isPresent()) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_target_member"));
            return 0;
        }
        MineColoniesIntegration.getOrCreateState(context.player().serverLevel(), target);
        if (!context.data().inviteProvince(realm.id(), targetIdentity, currentDay(source))) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_invite_failed"));
            return 0;
        }
        context.data().recordImperialAudit(realm.id(), currentDay(source),
                context.player().getGameProfile().getName(), "province-invited",
                target.getName() + " (#" + target.getID() + ")", target.getID());
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.empire_invited",
                target.getName(), target.getID(), realm.name(),
                currentDay(source) + EmpireRealm.INVITATION_VALIDITY_DAYS), true);
        return 1;
    }

    private static int joinEmpire(final CommandSourceStack source) {
        final ColonyContext context = resolveColony(source);
        if (context == null) return 0;
        if (!mayManageEconomy(context)) return denyPermission(source);
        if (context.data().realmForProvince(context.state().identity()).isPresent()) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_already_member"));
            return 0;
        }
        final EmpireRealm realm = context.data()
                .acceptRealmInvitation(context.state().identity(), currentDay(source)).orElse(null);
        if (realm == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_invitation_missing"));
            return 0;
        }
        context.data().recordImperialAudit(realm.id(), currentDay(source),
                context.player().getGameProfile().getName(), "province-joined",
                context.state().identity().storageKey(), context.state().identity().colonyId());
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.empire_joined", realm.name(), realm.id()), true);
        return 1;
    }

    private static int leaveEmpire(final CommandSourceStack source) {
        final ColonyContext context = resolveColony(source);
        if (context == null) return 0;
        if (!mayManageEconomy(context)) return denyPermission(source);
        final EmpireRealm realm = context.data().realmForProvince(context.state().identity()).orElse(null);
        if (realm == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_none"));
            return 0;
        }
        if (realm.capital().equals(context.state().identity())) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_capital_cannot_leave"));
            return 0;
        }
        if (!context.data().leaveRealm(context.state().identity())) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_leave_failed"));
            return 0;
        }
        context.data().recordImperialAudit(realm.id(), currentDay(source),
                context.player().getGameProfile().getName(), "province-left",
                context.state().identity().storageKey(), context.state().identity().colonyId());
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.empire_left", realm.name()), true);
        return 1;
    }

    private static boolean mayManageRealm(final ColonyContext context, final EmpireRealm realm) {
        return context.player().hasPermissions(2)
                || realm.isEmperor(context.player().getUUID().toString());
    }

    private static long safeAdd(final long left, final long right) {
        if (right > 0L && left > Long.MAX_VALUE - right) return Long.MAX_VALUE;
        return left + right;
    }

    private static int depositEmpireTreasury(
            final CommandSourceStack source, final long amount) {
        final ColonyContext context = resolveColony(source);
        if (context == null) return 0;
        if (!mayManageEconomy(context)) return denyPermission(source);
        final EmpireRealm realm = context.data().realmForProvince(context.state().identity()).orElse(null);
        if (realm == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_none"));
            return 0;
        }
        if (amount <= 0L || amount > EmpireState.MAX_TREASURY
                || !context.state().debitTreasury(amount)) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.empire_deposit_province_funds", amount));
            return 0;
        }
        if (!realm.depositImperialTreasury(amount)) {
            context.state().creditTreasury(amount);
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.empire_treasury_full",
                    EmpireRealm.MAX_IMPERIAL_TREASURY));
            return 0;
        }
        context.data().markChanged();
        context.data().recordImperialAudit(realm.id(), currentDay(source),
                context.player().getGameProfile().getName(), "deposit",
                context.state().identity().storageKey(), amount);
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.empire_deposited", amount,
                realm.imperialTreasuryCrowns()), true);
        return 1;
    }

    private static int withdrawEmpireTreasury(
            final CommandSourceStack source, final long amount) {
        final ColonyContext context = resolveColony(source);
        if (context == null) return 0;
        final EmpireRealm realm = context.data().realmForProvince(context.state().identity()).orElse(null);
        if (realm == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.empire_none"));
            return 0;
        }
        if (!mayManageRealm(context, realm)) return denyPermission(source);
        if (amount <= 0L || amount > EmpireState.MAX_TREASURY
                || !realm.withdrawImperialTreasury(amount)) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.empire_treasury_insufficient", amount));
            return 0;
        }
        if (!context.state().creditTreasury(amount)) {
            realm.depositImperialTreasury(amount);
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.empire_province_treasury_full",
                    EmpireState.MAX_TREASURY));
            return 0;
        }
        context.data().markChanged();
        context.data().recordImperialAudit(realm.id(), currentDay(source),
                context.player().getGameProfile().getName(), "withdraw",
                context.state().identity().storageKey(), amount);
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.empire_withdrawn", amount,
                realm.imperialTreasuryCrowns()), true);
        return 1;
    }

    private static int showGovernors(final CommandSourceStack source) {
        final ColonyContext context = resolveColony(source);
        if (context == null) return 0;
        final EmpireRealm realm = context.data().realmForProvince(context.state().identity()).orElse(null);
        if (realm == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_empire_required"));
            return 0;
        }
        final List<java.util.Map.Entry<ColonyIdentity, ProvinceGovernor>> governors =
                List.copyOf(realm.governors().entrySet());
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.governor_header", realm.name(), governors.size()), false);
        if (governors.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("imperium_realms.message.governor_empty"), false);
        } else {
            for (final java.util.Map.Entry<ColonyIdentity, ProvinceGovernor> appointment : governors) {
                final EmpireState provinceState = context.data().get(appointment.getKey()).orElse(null);
                final String provinceName = provinceState == null
                        ? appointment.getKey().storageKey() : provinceState.colonyName();
                source.sendSuccess(() -> Component.translatable(
                        "imperium_realms.message.governor_entry",
                        appointment.getValue().playerName(), provinceName,
                        appointment.getValue().appointedDay()), false);
            }
        }
        source.sendSuccess(() -> Component.translatable("imperium_realms.message.governor_help"), false);
        return 1;
    }

    private static int appointGovernor(
            final CommandSourceStack source, final int targetColonyId, final ServerPlayer governorPlayer) {
        final ColonyContext context = resolveColony(source);
        if (context == null) return 0;
        final EmpireRealm realm = context.data().realmForProvince(context.state().identity()).orElse(null);
        if (realm == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_empire_required"));
            return 0;
        }
        if (!mayManageRealm(context, realm)) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_emperor_only"));
            return 0;
        }
        final IColony target = MineColoniesIntegration
                .colonyById(context.player().serverLevel(), targetColonyId).orElse(null);
        if (target == null) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.governor_target_not_found", targetColonyId));
            return 0;
        }
        final ColonyIdentity identity = ColonyIdentity.from(target);
        if (identity.equals(realm.capital())) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_capital"));
            return 0;
        }
        if (!realm.containsProvince(identity)) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_not_member"));
            return 0;
        }
        final EmpireState provinceState = MineColoniesIntegration
                .getOrCreateState(context.player().serverLevel(), target);
        if (!context.data().appointGovernor(realm.id(), identity,
                governorPlayer.getUUID().toString(), governorPlayer.getGameProfile().getName(), currentDay(source))) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_unchanged"));
            return 0;
        }
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.governor_assigned", governorPlayer.getGameProfile().getName(),
                provinceState.colonyName()), true);
        return 1;
    }

    private static int dismissGovernor(final CommandSourceStack source, final int targetColonyId) {
        final ColonyContext context = resolveColony(source);
        if (context == null) return 0;
        final EmpireRealm realm = context.data().realmForProvince(context.state().identity()).orElse(null);
        if (realm == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_empire_required"));
            return 0;
        }
        if (!mayManageRealm(context, realm)) {
            source.sendFailure(Component.translatable("imperium_realms.message.governor_emperor_only"));
            return 0;
        }
        final IColony target = MineColoniesIntegration
                .colonyById(context.player().serverLevel(), targetColonyId).orElse(null);
        if (target == null) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.governor_target_not_found", targetColonyId));
            return 0;
        }
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

    private static int showCampaigns(final CommandSourceStack source) {
        final ColonyContext context = resolveColony(source);
        if (context == null) return 0;

        final EmpireState state = context.state();
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.campaign_header",
                state.colonyName(), state.treasuryCrowns(), state.diplomaticInfluence(),
                state.pendingMilitaryCampaigns().size()), false);

        final List<MilitaryCampaign> campaigns = state.recentMilitaryCampaigns();
        if (campaigns.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("imperium_realms.message.campaign_empty"), false);
        } else {
            for (final MilitaryCampaign campaign : campaigns) {
                final Component type = Component.translatable(
                        "imperium_realms.military_campaign.type." + campaign.type().id());
                final Component outcome = Component.translatable(
                        "imperium_realms.military_campaign.outcome."
                                + campaign.outcome().name().toLowerCase(java.util.Locale.ROOT));
                source.sendSuccess(() -> Component.translatable(
                        "imperium_realms.message.campaign_entry",
                        campaign.id(), type, campaign.targetName(), outcome,
                        campaign.startedDay(), campaign.resolvesDay(),
                        campaign.resolvedDay() < 0L ? "-" : Long.toString(campaign.resolvedDay())), false);
            }
        }
        source.sendSuccess(() -> Component.translatable("imperium_realms.message.campaign_help"), false);
        return 1;
    }

    private static int launchCampaign(
            final CommandSourceStack source, final String requestedType, final int targetColonyId) {
        final ColonyContext context = resolveColony(source);
        if (context == null) return 0;
        if (!mayManageEconomy(context)) return denyPermission(source);

        final MilitaryCampaign.Type type = MilitaryCampaign.Type.fromId(requestedType).orElse(null);
        if (type == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.campaign_type_unknown"));
            return 0;
        }
        final IColony target = MineColoniesIntegration
                .colonyById(context.player().serverLevel(), targetColonyId).orElse(null);
        if (target == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.campaign_target_not_found", targetColonyId));
            return 0;
        }
        final ColonyIdentity targetIdentity = ColonyIdentity.from(target);
        if (targetIdentity.equals(context.state().identity())) {
            source.sendFailure(Component.translatable("imperium_realms.message.campaign_self_target"));
            return 0;
        }
        MineColoniesIntegration.getOrCreateState(context.player().serverLevel(), target);
        final EmpireState state = context.state();
        if (!state.pendingMilitaryCampaigns().isEmpty()) {
            source.sendFailure(Component.translatable("imperium_realms.message.campaign_already_active"));
            return 0;
        }
        if (state.treasuryCrowns() < type.crownCost()) {
            source.sendFailure(Component.translatable("imperium_realms.message.campaign_crowns_required", type.crownCost()));
            return 0;
        }
        if (state.diplomaticInfluence() < type.influenceCost()) {
            source.sendFailure(Component.translatable("imperium_realms.message.campaign_influence_required", type.influenceCost()));
            return 0;
        }
        if (state.totalMilitaryTrainingPoints() < type.minimumTrainingPoints()) {
            source.sendFailure(Component.translatable(
                    "imperium_realms.message.campaign_training_required", type.minimumTrainingPoints()));
            return 0;
        }
        if (type == MilitaryCampaign.Type.WAR_CAMPAIGN && state.relationScore(targetIdentity) >= 75) {
            source.sendFailure(Component.translatable("imperium_realms.message.campaign_target_allied"));
            return 0;
        }

        final MilitaryCampaign campaign = state.launchMilitaryCampaign(
                context.player().getGameProfile().getName(), targetIdentity, target.getName(),
                type, currentDay(source)).orElse(null);
        if (campaign == null) {
            source.sendFailure(Component.translatable("imperium_realms.message.campaign_launch_failed"));
            return 0;
        }
        context.data().markChanged();
        source.sendSuccess(() -> Component.translatable(
                "imperium_realms.message.campaign_launched",
                campaign.id(),
                Component.translatable("imperium_realms.military_campaign.type." + campaign.type().id()),
                campaign.targetName(), campaign.resolvesDay()), true);
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
