package id.ac.ui.cs.advprog.eventsphere.topup.strategy;

import id.ac.ui.cs.advprog.eventsphere.topup.model.TopUp;
import id.ac.ui.cs.advprog.eventsphere.topup.model.CustomTopUp;
import id.ac.ui.cs.advprog.eventsphere.topup.model.FixedTopUp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TopUpFactoryTest {

    private final TopUpFactory factory = new TopUpFactory();

    @Test
    void testCreateCustomTopUp() {
        TopUp topUp = factory.createTopUp("CUSTOM", 50000);
        assertTrue(topUp instanceof CustomTopUp);
        assertEquals("CUSTOM", topUp.getType());
        assertEquals(50000, topUp.getAmount());
    }

    @Test
    void testCreateFixedTopUp() {
        TopUp topUp = factory.createTopUp("FIXED", 100000);
        assertTrue(topUp instanceof FixedTopUp);
        assertEquals("FIXED", topUp.getType());
        assertEquals(100000, topUp.getAmount());
    }

    @Test
    void testCreateInvalidTopUpThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> factory.createTopUp("INVALID", 100000));
    }

    @Test
    void testCreateSmallTopUp() {
        TopUp topUp = factory.createSmallTopUp();
        assertTrue(topUp instanceof FixedTopUp);
        assertEquals(TopUpFactory.SMALL_FIXED, topUp.getAmount());
    }

    @Test
    void testCreateMediumTopUp() {
        TopUp topUp = factory.createMediumTopUp();
        assertTrue(topUp instanceof FixedTopUp);
        assertEquals(TopUpFactory.MEDIUM_FIXED, topUp.getAmount());
    }

    @Test
    void testCreateLargeTopUp() {
        TopUp topUp = factory.createLargeTopUp();
        assertTrue(topUp instanceof FixedTopUp);
        assertEquals(TopUpFactory.LARGE_FIXED, topUp.getAmount());
    }
}
