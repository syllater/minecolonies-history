package com.imperium.realms.colony;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EmpireRealmTest {
    private final ColonyIdentity capital = new ColonyIdentity("minecraft:overworld", 1);
    private final ColonyIdentity province = new ColonyIdentity("minecraft:overworld", 2);

    @Test
    void foundRealmIncludesCapitalAndCanAcceptProvinceInvitation() {
        final EmpireRealm realm = EmpireRealm.found(
                1L, "North Sea Union", capital, "uuid-emperor", "Ada", 10L);

        assertEquals(1, realm.provinceCount());
        assertTrue(realm.containsProvince(capital));
        assertTrue(realm.isEmperor("uuid-emperor"));
        assertFalse(realm.containsProvince(province));

        assertTrue(realm.inviteProvince(province, 10L));
        assertEquals(17L, realm.invitationExpiry(province));
        assertTrue(realm.hasValidInvitation(province, 17L));
        assertTrue(realm.acceptInvitation(province, 17L));
        assertEquals(2, realm.provinceCount());
        assertTrue(realm.containsProvince(province));
        assertEquals(-1L, realm.invitationExpiry(province));
    }

    @Test
    void invitationExpiresAndCapitalCannotLeave() {
        final EmpireRealm realm = EmpireRealm.found(
                1L, "North Sea Union", capital, "uuid-emperor", "Ada", 0L);
        assertFalse(realm.removeProvince(capital));

        assertTrue(realm.inviteProvince(province, 5L));
        assertFalse(realm.acceptInvitation(province, 13L));
        assertFalse(realm.containsProvince(province));
        assertFalse(realm.invitations().containsKey(province));
    }

    @Test
    void realmRestoreKeepsCapitalAndMembership() {
        final EmpireRealm realm = EmpireRealm.restore(
                2L, "Realm", capital, "uuid", "Emperor", 4L,
                0L, java.util.List.of(province), java.util.Map.of(), 15, "welfare", java.util.List.of(), java.util.Map.of(), 4L);
        assertEquals(2, realm.provinceCount());
        assertTrue(realm.containsProvince(capital));
        assertTrue(realm.containsProvince(province));
        assertEquals(15, realm.imperialTaxRatePercent());
        assertEquals("welfare", realm.imperialEconomicPolicyId());
    }
    @Test
    void imperialLawsAreBoundedAndHaveExplicitUnsetDefaults() {
        final EmpireRealm realm = EmpireRealm.found(
                1L, "North Sea Union", capital, "uuid-emperor", "Ada", 0L);

        assertFalse(realm.hasImperialTaxLaw());
        assertFalse(realm.hasImperialPolicyLaw());
        assertFalse(realm.setImperialTaxRatePercent(-1));
        assertFalse(realm.setImperialTaxRatePercent(26));
        assertTrue(realm.setImperialTaxRatePercent(10));
        assertEquals(10, realm.imperialTaxRatePercent());
        assertFalse(realm.setImperialTaxRatePercent(10));

        assertTrue(realm.setImperialEconomicPolicy(EconomicPolicy.WELFARE));
        assertEquals("welfare", realm.imperialEconomicPolicyId());
        assertTrue(realm.hasImperialPolicyLaw());
        assertFalse(realm.setImperialEconomicPolicy(EconomicPolicy.WELFARE));
    }

    @Test
    void imperialTaxRemittanceUsesOnlyExistingReceiptsAndRequiresAnEnactedLaw() {
        final EmpireRealm realm = EmpireRealm.found(
                1L, "North Sea Union", capital, "uuid-emperor", "Ada", 0L);
        assertEquals(0L, realm.calculateImperialTaxRemittance(1_000L));

        assertTrue(realm.setImperialTaxRatePercent(10));
        assertEquals(100L, realm.calculateImperialTaxRemittance(1_000L));
        assertEquals(2L, realm.calculateImperialTaxRemittance(29L));
        assertEquals(0L, realm.calculateImperialTaxRemittance(9L));
        assertEquals(0L, realm.calculateImperialTaxRemittance(0L));
        assertEquals(0L, realm.calculateImperialTaxRemittance(-100L));
    }

    @Test
    void realmAuditIsBoundedAndReturnsNewestEntriesFirst() {
        final EmpireRealm realm = EmpireRealm.found(
                1L, "North Sea Union", capital, "uuid-emperor", "Ada", 0L);
        for (int index = 0; index < EmpireRealm.MAX_AUDIT_ENTRIES + 5; index++) {
            realm.recordAudit(index, "Inspector", "deposit", "province-" + index, index);
        }
        final java.util.List<ImperialAuditEntry> recent = realm.recentAuditEntries();
        assertEquals(EmpireRealm.MAX_AUDIT_ENTRIES, recent.size());
        assertEquals("province-104", recent.get(0).subject());
        assertEquals("province-5", recent.get(recent.size() - 1).subject());
        assertEquals(104L, recent.get(0).dayIndex());
    }

    @Test
    void governorsCanOnlyBeAssignedToExistingNonCapitalProvinces() {
        final EmpireRealm realm = EmpireRealm.found(
                1L, "North Sea Union", capital, "uuid-emperor", "Ada", 0L);
        assertFalse(realm.appointGovernor(capital, "uuid", "Governor", 1L));
        assertTrue(realm.inviteProvince(province, 1L));
        assertTrue(realm.acceptInvitation(province, 1L));
        assertTrue(realm.appointGovernor(province, "uuid-governor", "Bram", 2L));
        assertEquals("Bram", realm.governorFor(province).orElseThrow().playerName());
        assertTrue(realm.hasGovernor(province));
        assertFalse(realm.appointGovernor(province, "uuid-governor", "Bram", 2L));
        assertTrue(realm.dismissGovernor(province));
        assertFalse(realm.hasGovernor(province));
    }

    @Test
    void regionalEventsRunAtSevenDayIntervals() {
        final EmpireRealm realm = EmpireRealm.found(
                7L, "North Sea Union", capital, "uuid-emperor", "Ada", 3L);
        assertFalse(realm.isRegionalEventDue(9L));
        assertTrue(realm.isRegionalEventDue(10L));
        assertTrue(realm.markRegionalEvent(10L));
        assertFalse(realm.isRegionalEventDue(16L));
        assertTrue(realm.isRegionalEventDue(17L));
    }

    @Test
    void weeklyRegionalEventSelectionIsDeterministicAndBounded() {
        for (long realmId = 1L; realmId < 20L; realmId++) {
            final ImperialRegionalEvent first = ImperialRegionalEvent.forTurn(realmId, 35L);
            assertEquals(first, ImperialRegionalEvent.forTurn(realmId, 35L));
            assertTrue(ImperialRegionalEvent.fromId(first.id()).isPresent());
        }
        assertEquals(7, ImperialRegionalEvent.values().length);
    }

    @Test
    void lowLoyaltyRaisesPetitionAfterFiveConsecutiveDaysAndResponseResetsIt() {
        final EmpireRealm realm = EmpireRealm.found(1L, "North Sea Union", capital, "uuid-emperor", "Ada", 0L);
        assertTrue(realm.inviteProvince(province, 0L));
        assertTrue(realm.acceptInvitation(province, 0L));
        for (long day = 1L; day <= 8L; day++) {
            assertTrue(realm.beginCohesionTurn(day));
            assertEquals(day == 8L, realm.updateProvinceLoyalty(province, day <= 4L ? -15 : 0, day));
        }
        assertTrue(realm.hasSeparatistPetition(province));
        assertEquals(8L, realm.separatistPetitionDay(province));
        assertTrue(realm.resolveSeparatistPetition(province, 25));
        assertFalse(realm.hasSeparatistPetition(province));
        assertEquals(40, realm.provincialLoyalty(province));
    }

    @Test
    void imperialTreasurySupportsBoundedDepositsAndWithdrawals() {
        final EmpireRealm realm = EmpireRealm.found(
                1L, "North Sea Union", capital, "uuid-emperor", "Ada", 0L);

        assertEquals(0L, realm.imperialTreasuryCrowns());
        assertFalse(realm.withdrawImperialTreasury(1L));
        assertFalse(realm.depositImperialTreasury(0L));
        assertTrue(realm.depositImperialTreasury(500L));
        assertEquals(500L, realm.imperialTreasuryCrowns());
        assertFalse(realm.depositImperialTreasury(EmpireRealm.MAX_IMPERIAL_TREASURY));
        assertTrue(realm.withdrawImperialTreasury(200L));
        assertEquals(300L, realm.imperialTreasuryCrowns());
        assertFalse(realm.withdrawImperialTreasury(301L));
    }


    @Test
    void supplyRoutesRequireMemberProvinceAndCentralFunds() {
        final EmpireRealm realm = EmpireRealm.found(
                1L, "North Sea Union", capital, "uuid-emperor", "Ada", 0L);
        assertFalse(realm.buildSupplyRoute(capital, 1L, "Ada"));

        assertTrue(realm.inviteProvince(province, 0L));
        assertTrue(realm.acceptInvitation(province, 0L));
        assertFalse(realm.buildSupplyRoute(province, 1L, "Ada"),
                "Building a route requires enough central treasury funds");
        assertTrue(realm.depositImperialTreasury(EmpireRealm.SUPPLY_ROUTE_BUILD_COST));
        assertTrue(realm.buildSupplyRoute(province, 1L, "Ada"));
        assertFalse(realm.buildSupplyRoute(province, 1L, "Ada"),
                "A route to a province must not be duplicated");
        assertEquals(0L, realm.imperialTreasuryCrowns());
        assertTrue(realm.supplyRouteTo(province).orElseThrow().isActive());
    }

    @Test
    void unpaidSupplyRoutesWearDownAndPaidRoutesRecover() {
        final EmpireRealm realm = EmpireRealm.found(
                1L, "North Sea Union", capital, "uuid-emperor", "Ada", 0L);
        assertTrue(realm.inviteProvince(province, 0L));
        assertTrue(realm.acceptInvitation(province, 0L));
        assertTrue(realm.depositImperialTreasury(EmpireRealm.SUPPLY_ROUTE_BUILD_COST));
        assertTrue(realm.buildSupplyRoute(province, 1L, "Ada"));
        final ImperialSupplyRoute route = realm.supplyRouteTo(province).orElseThrow();

        for (long day = 2L; day <= 8L; day++) {
            assertEquals(1, realm.processSupplyRoutes(day));
        }
        assertEquals(30, route.condition());
        assertFalse(route.isActive());
        assertTrue(realm.depositImperialTreasury(EmpireRealm.SUPPLY_ROUTE_DAILY_UPKEEP_CROWNS * 8L));

        for (long day = 9L; day <= 11L; day++) {
            assertEquals(1, realm.processSupplyRoutes(day));
        }
        assertEquals(45, route.condition());
        assertTrue(route.isActive());
        assertEquals(11L, route.lastUpkeepDay());
    }

    @Test
    void routeRemovalFollowsProvinceSeparation() {
        final EmpireRealm realm = EmpireRealm.found(
                1L, "North Sea Union", capital, "uuid-emperor", "Ada", 0L);
        assertTrue(realm.inviteProvince(province, 0L));
        assertTrue(realm.acceptInvitation(province, 0L));
        assertTrue(realm.depositImperialTreasury(EmpireRealm.SUPPLY_ROUTE_BUILD_COST));
        assertTrue(realm.buildSupplyRoute(province, 1L, "Ada"));
        assertTrue(realm.removeProvince(province));
        assertTrue(realm.supplyRoutes().isEmpty());
    }

}
