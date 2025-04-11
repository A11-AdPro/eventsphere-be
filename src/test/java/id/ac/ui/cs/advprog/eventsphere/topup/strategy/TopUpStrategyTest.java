package id.ac.ui.cs.advprog.eventsphere.topup.strategy;

import id.ac.ui.cs.advprog.eventsphere.topup.model.CustomTopUp;
import id.ac.ui.cs.advprog.eventsphere.topup.model.FixedTopUp;
import id.ac.ui.cs.advprog.eventsphere.topup.model.TopUp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TopUpStrategyTest {

    private TopUpStrategy fixedTopUpStrategy;
    private TopUpStrategy customTopUpStrategy;

    @BeforeEach
    void setUp() {
        fixedTopUpStrategy = new FixedTopUpStrategy();
        customTopUpStrategy = new CustomTopUpStrategy();
    }

    @Test
    void testFixedTopUpStrategy_ValidAmount() {
        // Should accept 50000 as valid fixed amount
        TopUp result = fixedTopUpStrategy.createTopUp(50000);
        assertNotNull(result);
        assertEquals(50000, result.getAmount());
        assertTrue(result instanceof FixedTopUp);
    }

    @Test
    void testFixedTopUpStrategy_ValidAmounts() {
        // Testing all valid fixed amounts
        int[] validAmounts = {50000, 100000, 150000, 200000};

        for (int amount : validAmounts) {
            TopUp result = fixedTopUpStrategy.createTopUp(amount);
            assertNotNull(result);
            assertEquals(amount, result.getAmount());
            assertTrue(result instanceof FixedTopUp);
        }
    }

    @Test
    void testFixedTopUpStrategy_InvalidAmount() {
        // Should throw exception for non-standard fixed amount
        assertThrows(IllegalArgumentException.class, () -> {
            fixedTopUpStrategy.createTopUp(75000);
        });
    }

    @Test
    void testCustomTopUpStrategy_ValidAmount() {
        // Should accept amount within valid range
        TopUp result = customTopUpStrategy.createTopUp(50000);
        assertNotNull(result);
        assertEquals(50000, result.getAmount());
        assertTrue(result instanceof CustomTopUp);
    }

    @Test
    void testCustomTopUpStrategy_MinimumAmount() {
        // Should accept minimum amount (10000)
        TopUp result = customTopUpStrategy.createTopUp(10000);
        assertNotNull(result);
        assertEquals(10000, result.getAmount());
    }

    @Test
    void testCustomTopUpStrategy_MaximumAmount() {
        // Should accept maximum amount (1000000)
        TopUp result = customTopUpStrategy.createTopUp(1000000);
        assertNotNull(result);
        assertEquals(1000000, result.getAmount());
    }

    @Test
    void testCustomTopUpStrategy_BelowMinimum() {
        // Should throw exception for amount below minimum
        assertThrows(IllegalArgumentException.class, () -> {
            customTopUpStrategy.createTopUp(9999);
        });
    }

    @Test
    void testCustomTopUpStrategy_AboveMaximum() {
        // Should throw exception for amount above maximum
        assertThrows(IllegalArgumentException.class, () -> {
            customTopUpStrategy.createTopUp(1000001);
        });
    }
}