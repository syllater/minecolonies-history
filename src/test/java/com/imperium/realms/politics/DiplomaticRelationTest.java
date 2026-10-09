package com.imperium.realms.politics;

import com.imperium.realms.colony.ColonyIdentity;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class DiplomaticRelationTest {
    @Test
    void relationIdentityIsBilateralAndScoresAreBounded() {
        final ColonyIdentity a = new ColonyIdentity("minecraft:overworld", 1);
        final ColonyIdentity b = new ColonyIdentity("minecraft:overworld", 2);
        final DiplomaticRelation ab = new DiplomaticRelation(a, b, 130, TreatyType.FRIENDSHIP, 0);
        final DiplomaticRelation ba = new DiplomaticRelation(b, a, -15, TreatyType.FRIENDSHIP, 0);

        assertEquals(ab.first(), ba.first());
        assertEquals(ab.second(), ba.second());
        assertEquals(100, ab.standing());
        assertEquals(-15, ba.standing());
        assertEquals(b, ab.otherColony(a));
        assertEquals(a, ba.otherColony(b));
    }

    @Test
    void treatyImprovesStandingWithoutBreakingHigherExistingTrust() {
        final ColonyIdentity a = new ColonyIdentity("minecraft:overworld", 1);
        final ColonyIdentity b = new ColonyIdentity("minecraft:overworld", 2);
        final DiplomaticRelation relation = new DiplomaticRelation(a, b, 10, TreatyType.FRIENDSHIP, 0);

        relation.applyTreaty(TreatyType.ALLIANCE, 4L);
        assertEquals(75, relation.standing());
        assertEquals(TreatyType.ALLIANCE, relation.treaty());
        assertEquals(4L, relation.lastChangedGameDay());

        relation.applyTreaty(TreatyType.FRIENDSHIP, 5L);
        assertEquals(75, relation.standing());
        assertEquals(TreatyType.FRIENDSHIP, relation.treaty());
    }

    @Test
    void selfRelationsAreRejected() {
        final ColonyIdentity a = new ColonyIdentity("minecraft:overworld", 1);
        assertThrows(IllegalArgumentException.class,
                () -> new DiplomaticRelation(a, a, 0, TreatyType.FRIENDSHIP, 0));
    }
}
