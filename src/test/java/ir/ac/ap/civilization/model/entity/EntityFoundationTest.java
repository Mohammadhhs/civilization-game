package ir.ac.ap.civilization.model.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityFoundationTest {

    @Test
    void typedKeysShouldPreventCrossTypeIdCollisions() {
        EntityKey unit = new EntityKey(EntityKind.UNIT, 1);
        EntityKey building = new EntityKey(EntityKind.BUILDING, 1);

        assertNotEquals(unit, building);
        assertEquals("UNIT:1", unit.toExternalForm());
        assertEquals(
                unit,
                EntityKey.fromExternalForm(unit.toExternalForm())
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> EntityKey.fromExternalForm("UNIT")
        );
    }

    @Test
    void ownersShouldBeStableValueObjects() {
        assertEquals(Owner.tribe(3), Owner.tribe(3));
        assertNotEquals(Owner.tribe(3), Owner.tribe(4));
        assertTrue(Owner.PLAYER.isPlayer());
        assertFalse(Owner.NEUTRAL.isPlayer());
    }

    @Test
    void hitPointsShouldClampDamageAndHealing() {
        HitPoints hitPoints = new HitPoints(60, 100);

        hitPoints.damage(80);
        assertEquals(0, hitPoints.getCurrent());
        assertTrue(hitPoints.isDepleted());

        hitPoints.heal(500);
        assertEquals(100, hitPoints.getCurrent());
    }

    @Test
    void invalidHealthMutationsShouldBeRejected() {
        HitPoints hitPoints = new HitPoints(100);

        assertThrows(
                IllegalArgumentException.class,
                () -> hitPoints.damage(-1)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> hitPoints.heal(-1)
        );
    }
}
