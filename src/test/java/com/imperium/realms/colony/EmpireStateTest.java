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
        assertEquals(EmpireState.TaxProposalResolution.PASSED,
                state.resolveProposal(proposal.id(), true, 5L, "Emperor"));
        assertEquals(6, state.taxRatePercent());
    }

    @Test
    void emperorCanVetoAndProposalsExpire() {
        final EmpireState state = EmpireState.create(
                new ColonyIdentity("minecraft:overworld", 9), "Capital", 0L);
        final ParliamentProposal vetoed = state.createTaxProposal("Emperor", 4, 0L).orElseThrow();
        assertEquals(EmpireState.TaxProposalResolution.REJECTED,
                state.resolveProposal(vetoed.id(), false, 0L, "Emperor"));
        assertEquals(5, state.taxRatePercent());

        final ParliamentProposal expired = state.createTaxProposal("Emperor", 10, 0L).orElseThrow();
        assertTrue(state.expireParliamentProposals(4L));
        assertEquals(ParliamentProposal.Status.EXPIRED, expired.status());
        assertEquals(EmpireState.TaxProposalResolution.ALREADY_RESOLVED,
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

}
