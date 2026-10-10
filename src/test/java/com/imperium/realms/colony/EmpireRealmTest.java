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
                0L, java.util.List.of(province), java.util.Map.of(), 15, "welfare");
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

}
