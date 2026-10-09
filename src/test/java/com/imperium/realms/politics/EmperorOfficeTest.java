package com.imperium.realms.politics;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EmperorOfficeTest {
    @Test
    void onlyOneInitialClaimCanSucceed() {
        final EmperorOffice office = new EmperorOffice();
        final UUID first = UUID.randomUUID();
        final UUID second = UUID.randomUUID();

        assertTrue(office.claim(first, "Ada", 4L));
        assertFalse(office.claim(second, "Bram", 4L));
        assertTrue(office.isEmperor(first));
        assertEquals("Ada", office.emperorName().orElseThrow());
        assertEquals(1L, office.reignCount());
    }

    @Test
    void currentEmperorCanAppointMemberIdentityPassedByServerGuard() {
        final EmperorOffice office = new EmperorOffice();
        final UUID first = UUID.randomUUID();
        final UUID successor = UUID.randomUUID();

        assertTrue(office.claim(first, "Ada", 4L));
        assertFalse(office.appoint(successor, first, "Ada", 5L));
        assertFalse(office.appoint(first, first, "Ada", 5L));
        assertTrue(office.appoint(first, successor, "Bram", 5L));
        assertFalse(office.isEmperor(first));
        assertTrue(office.isEmperor(successor));
        assertEquals(2L, office.reignCount());
        assertEquals(5L, office.appointmentGameDay());
    }

    @Test
    void abdicationOpensTheOfficeAndIncrementsHistory() {
        final EmperorOffice office = new EmperorOffice();
        final UUID emperor = UUID.randomUUID();

        assertTrue(office.claim(emperor, "Ada", 4L));
        assertFalse(office.abdicate(UUID.randomUUID()));
        assertTrue(office.abdicate(emperor));
        assertTrue(office.emperorId().isEmpty());
        assertEquals(1, office.abdications());
        assertTrue(office.claim(UUID.randomUUID(), "Bram", 8L));
        assertEquals(2L, office.reignCount());
    }
}
