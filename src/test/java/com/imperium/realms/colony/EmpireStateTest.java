package com.imperium.realms.colony;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EmpireStateTest {
    @Test
    void firstObservationUsesSafeDefaults() {
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 12);
        final EmpireState state = EmpireState.create(identity, "  Capital  ", -9);

        assertEquals(identity, state.identity());
        assertEquals("Capital", state.colonyName());
        assertEquals(0L, state.firstSeenGameTime());
        assertEquals(0L, state.lastSeenGameTime());
        assertEquals(0L, state.treasuryCrowns());
        assertEquals(5, state.taxRatePercent());
        assertEquals(EconomicPolicy.BALANCED, state.economicPolicy());
        assertEquals(50, state.stability());
    }

    @Test
    void observationUpdatesNameWithoutResettingFirstSeen() {
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 12);
        final EmpireState state = EmpireState.create(identity, "Capital", 100L);

        assertTrue(state.observe("New Capital", 200L));
        assertEquals("New Capital", state.colonyName());
        assertEquals(100L, state.firstSeenGameTime());
        assertEquals(100L, state.lastSeenGameTime());
    }

    @Test
    void heartbeatOnlyAdvancesOnAnInGameDayBoundary() {
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 12);
        final EmpireState state = EmpireState.create(identity, "Capital", 100L);

        assertFalse(state.observe("Capital", 23_999L));
        assertEquals(100L, state.lastSeenGameTime());

        assertTrue(state.observe("Capital", 24_100L));
        assertEquals(24_100L, state.lastSeenGameTime());
        assertEquals(100L, state.firstSeenGameTime());
    }

    @Test
    void emptyNamesAreNormalized() {
        final ColonyIdentity identity = new ColonyIdentity("minecraft:overworld", 12);
        final EmpireState state = EmpireState.create(identity, "", 10L);

        assertEquals("Unnamed colony", state.colonyName());
    }

    @Test
    void dailyTaxesAreCollectedOnlyOncePerDay() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 1), "Capital", 0L);

        assertEquals(10L, state.collectDailyTaxes(1L, 10L));
        assertEquals(10L, state.treasuryCrowns());
        assertEquals(0L, state.collectDailyTaxes(1L, 10L));
        assertEquals(10L, state.collectDailyTaxes(2L, 10L));
        assertEquals(20L, state.treasuryCrowns());
    }

    @Test
    void policyChangesTaxAndStabilityEffects() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 2), "Port", 0L);
        assertTrue(state.setEconomicPolicy(EconomicPolicy.WELFARE));
        assertEquals(7L, state.collectDailyTaxes(1L, 10L));
        assertEquals(52, state.stability());

        assertTrue(state.setEconomicPolicy(EconomicPolicy.AUSTERITY));
        assertEquals(15L, state.collectDailyTaxes(2L, 10L));
        assertEquals(50, state.stability());
    }

    @Test
    void taxRateRejectsValuesOutsideThePermittedRange() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 3), "Port", 0L);

        assertFalse(state.setTaxRatePercent(-1));
        assertFalse(state.setTaxRatePercent(26));
        assertEquals(5, state.taxRatePercent());
        assertTrue(state.setTaxRatePercent(12));
        assertEquals(12, state.taxRatePercent());
    }

    @Test
    void investmentDebitsTreasuryAndAddsKnowledge() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 4), "Capital", 0L);
        assertTrue(state.creditTreasury(100L));
        assertTrue(state.investInKnowledge(20L));
        assertEquals(80L, state.treasuryCrowns());
        assertEquals(2L, state.knowledgePoints());
        assertFalse(state.investInKnowledge(25L));
        assertFalse(state.investInKnowledge(90L));
        assertEquals(80L, state.treasuryCrowns());
    }

    @Test
    void philosopherCanGenerateKnowledgeOnlyOncePerMinute() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 5), "Archive", 0L);

        assertTrue(state.recordScholarWork(100L));
        assertFalse(state.recordScholarWork(1_299L));
        assertTrue(state.recordScholarWork(1_300L));
        assertEquals(2L, state.knowledgePoints());
    }

    @Test
    void treasuryCannotBeOverdrawnOrOverflowItsCap() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 6), "Capital", 0L);

        assertFalse(state.debitTreasury(1L));
        assertTrue(state.creditTreasury(EmpireState.MAX_TREASURY));
        assertFalse(state.creditTreasury(1L));
        assertEquals(EmpireState.MAX_TREASURY, state.treasuryCrowns());
    }

    @Test
    void taxChangesNeedAnEmperorDecisionAndCouncilMajority() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 7), "Capital", 0L);
        final ParliamentProposal proposal = state.createTaxProposal("Emperor", 10, 0L).orElseThrow();

        assertEquals(0, proposal.councilYesVotes());
        assertEquals(EmpireState.ProposalResolution.REJECTED,
                state.resolveProposal(proposal.id(), true, 0L, "Emperor"));
        assertEquals(5, state.taxRatePercent());
        assertEquals(ParliamentProposal.Status.REJECTED, proposal.status());
    }

    @Test
    void welfareCanBuildCoalitionForAModestTaxIncrease() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 8), "Capital", 0L);
        state.setEconomicPolicy(EconomicPolicy.WELFARE);
        for (long day = 1L; day <= 5L; day++) {
            state.collectDailyTaxes(day, 0L);
        }
        assertEquals(60, state.stability());

        final ParliamentProposal proposal = state.createTaxProposal("Emperor", 6, 5L).orElseThrow();
        assertEquals(3, proposal.councilYesVotes());
        assertEquals(EmpireState.ProposalResolution.PASSED,
                state.resolveProposal(proposal.id(), true, 5L, "Emperor"));
        assertEquals(6, state.taxRatePercent());
    }

    @Test
    void emperorCanVetoAndProposalsExpire() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 9), "Capital", 0L);
        final ParliamentProposal vetoed = state.createTaxProposal("Emperor", 4, 0L).orElseThrow();
        assertEquals(EmpireState.ProposalResolution.REJECTED,
                state.resolveProposal(vetoed.id(), false, 0L, "Emperor"));
        assertEquals(5, state.taxRatePercent());

        final ParliamentProposal expired = state.createTaxProposal("Emperor", 10, 0L).orElseThrow();
        assertTrue(state.expireParliamentProposals(4L));
        assertEquals(ParliamentProposal.Status.EXPIRED, expired.status());
        assertEquals(EmpireState.ProposalResolution.ALREADY_RESOLVED,
                state.resolveProposal(expired.id(), true, 4L, "Emperor"));
    }

    @Test
    void taxProposalsCannotDuplicateActiveBillsOrChangeTaxImmediately() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 10), "Capital", 0L);
        assertTrue(state.createTaxProposal("Emperor", 4, 0L).isPresent());
        assertFalse(state.createTaxProposal("Emperor", 4, 0L).isPresent());
        assertFalse(state.createTaxProposal("Emperor", 5, 0L).isPresent());
        assertEquals(5, state.taxRatePercent());
    }
    @Test
    void policyChangesNeedParliamentaryApprovalAndRecordAnAuditEntry() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 11), "Capital", 0L);
        final ParliamentProposal proposal = state.createPolicyProposal(
                "Emperor", EconomicPolicy.MERCANTILE, 2L).orElseThrow();

        assertEquals(ParliamentProposal.Type.ECONOMIC_POLICY, proposal.type());
        assertEquals(EconomicPolicy.BALANCED, state.economicPolicy());
        assertEquals(3, proposal.councilYesVotes());

        assertEquals(EmpireState.ProposalResolution.PASSED,
                state.resolveProposal(proposal.id(), true, 2L, "Sovereign"));
        assertEquals(EconomicPolicy.MERCANTILE, state.economicPolicy());
        assertEquals(ParliamentProposal.Status.PASSED, proposal.status());
        assertEquals("Sovereign", proposal.resolvedBy());
        assertEquals(2L, proposal.resolvedDay());
    }

    @Test
    void policyProposalWithoutCouncilMajorityIsRejectedAndAudited() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 13), "Capital", 0L);
        final ParliamentProposal proposal = state.createPolicyProposal(
                "Emperor", EconomicPolicy.AUSTERITY, 3L).orElseThrow();

        assertEquals(1, proposal.councilYesVotes());
        assertEquals(EmpireState.ProposalResolution.REJECTED,
                state.resolveProposal(proposal.id(), true, 3L, "Sovereign"));
        assertEquals(EconomicPolicy.BALANCED, state.economicPolicy());
        assertEquals("Parliament", proposal.resolvedBy());
        assertEquals(3L, proposal.resolvedDay());
    }

    @Test
    void policyBillsCannotDuplicateAndDoNotApplyBeforeAssent() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 14), "Capital", 0L);
        assertTrue(state.createPolicyProposal(
                "Emperor", EconomicPolicy.MERCANTILE, 0L).isPresent());
        assertFalse(state.createPolicyProposal(
                "Emperor", EconomicPolicy.MERCANTILE, 0L).isPresent());
        assertFalse(state.createPolicyProposal(
                "Emperor", EconomicPolicy.BALANCED, 0L).isPresent());
        assertEquals(EconomicPolicy.BALANCED, state.economicPolicy());
    }

    @Test
    void vetoRecordsActorAndDoesNotApplyTaxBill() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 15), "Capital", 0L);
        final ParliamentProposal proposal = state.createTaxProposal("Minister", 4, 1L).orElseThrow();

        assertEquals(EmpireState.ProposalResolution.REJECTED,
                state.resolveProposal(proposal.id(), false, 1L, "Emperor Adelaide"));
        assertEquals("Emperor Adelaide", proposal.resolvedBy());
        assertEquals(1L, proposal.resolvedDay());
        assertEquals(5, state.taxRatePercent());
    }

    @Test
    void liveColonyHappinessChangesStabilityAndLegitimacy() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 16), "Capital", 0L);

        state.collectDailyTaxes(1L, 10L, 5.0);
        assertEquals(53, state.stability());
        assertEquals(53, state.legitimacy());

        assertTrue(state.setTaxRatePercent(25));
        state.collectDailyTaxes(2L, 10L, 0.5);
        assertEquals(50, state.stability());
        assertEquals(50, state.legitimacy());
    }

    @Test
    void emptyColonyDoesNotGainLegitimacyFromAnEmptyCitizensHappinessFallback() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 17), "Abandoned", 0L);

        state.collectDailyTaxes(1L, 0L, 5.5);
        assertEquals(48, state.stability());
        assertEquals(48, state.legitimacy());
    }

    @Test
    void taxCollectorTrainingImprovesRevenueAndIsCapped() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 18), "Capital", 0L);

        assertEquals(0, state.taxCollectionEfficiencyPercent());
        assertTrue(state.recordTaxCollectorWork(0L));
        assertFalse(state.recordTaxCollectorWork(1_199L));
        assertTrue(state.recordTaxCollectorWork(1_200L));
        assertEquals(2, state.taxCollectionEfficiencyPercent());

        // A 100-citizen colony at 5% tax has a base yield of 100 crowns.
        assertEquals(102L, state.collectDailyTaxes(1L, 100L));
        assertEquals(102L, state.treasuryCrowns());

        long tick = 2_400L;
        while (state.taxCollectionEfficiencyPercent() < 25) {
            assertTrue(state.recordTaxCollectorWork(tick));
            tick += 1_200L;
        }
        assertEquals(25, state.taxCollectionEfficiencyPercent());
        assertFalse(state.recordTaxCollectorWork(tick));
        assertEquals(125L, state.collectDailyTaxes(2L, 100L));
        assertEquals(227L, state.treasuryCrowns());
    }

    @Test
    void diplomatGeneratesInfluenceAtMostOncePerWorkInterval() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 19), "Capital", 0L);

        assertTrue(state.recordDiplomatWork(0L));
        assertFalse(state.recordDiplomatWork(2_399L));
        assertEquals(1L, state.diplomaticInfluence());
        assertTrue(state.recordDiplomatWork(2_400L));
        assertEquals(2L, state.diplomaticInfluence());
    }

    @Test
    void diplomacyCostsInfluenceAndCannotTargetTheSameColony() {
        final ColonyIdentity ownIdentity = new ColonyIdentity("minecraft:overworld", 20);
        final ColonyIdentity target = new ColonyIdentity("minecraft:overworld", 21);
        final EmpireState state = EmpireState.create(ownIdentity, "Capital", 0L);

        assertFalse(state.improveDiplomaticRelations(ownIdentity));
        assertEquals(0L, state.diplomaticInfluence());
        for (long tick = 0L; tick < 10L * 2_400L; tick += 2_400L) {
            assertTrue(state.recordDiplomatWork(tick));
        }

        assertEquals(10L, state.diplomaticInfluence());
        assertTrue(state.improveDiplomaticRelations(target));
        assertEquals(5, state.relationScore(target));
        assertEquals(0L, state.diplomaticInfluence());
        assertFalse(state.improveDiplomaticRelations(target));
        assertEquals("neutral", EmpireState.relationStatusId(state.relationScore(target)));
    }

    @Test
    void diplomaticRelationStatusUsesDeterministicThresholds() {
        assertEquals("hostile", EmpireState.relationStatusId(-50));
        assertEquals("unfriendly", EmpireState.relationStatusId(-15));
        assertEquals("neutral", EmpireState.relationStatusId(0));
        assertEquals("cordial", EmpireState.relationStatusId(15));
        assertEquals("friendly", EmpireState.relationStatusId(40));
        assertEquals("allied", EmpireState.relationStatusId(75));
        assertEquals("allied", EmpireState.relationStatusId(150));
    }

    @Test
    void diplomaticInfluenceAndRelationsAreBounded() {
        final ColonyIdentity ownIdentity = new ColonyIdentity("minecraft:overworld", 22);
        final ColonyIdentity target = new ColonyIdentity("minecraft:overworld", 23);
        final EmpireState state = EmpireState.create(ownIdentity, "Capital", 0L);

        for (long tick = 0L; tick < 1_100L * 2_400L; tick += 2_400L) {
            state.recordDiplomatWork(tick);
        }
        assertEquals(EmpireState.MAX_DIPLOMATIC_INFLUENCE, state.diplomaticInfluence());

        int steps = 0;
        while (state.relationScore(target) < 100) {
            assertTrue(state.improveDiplomaticRelations(target));
            steps++;
        }
        assertEquals(20, steps);
        assertEquals(100, state.relationScore(target));
        assertFalse(state.improveDiplomaticRelations(target));
    }

}
