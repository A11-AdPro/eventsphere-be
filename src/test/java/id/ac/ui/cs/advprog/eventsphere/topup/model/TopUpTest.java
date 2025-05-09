package id.ac.ui.cs.advprog.eventsphere.topup.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TopUpTest {

    @Test
    public void testFixedTopUpAmountIsValid() {
        TopUp topUp = new FixedTopUp(50000);
        assertEquals(50000, topUp.getAmount());
    }

    @Test
    public void testCustomTopUpWithinValidRange() {
        TopUp topUp = new CustomTopUp(50000);
        assertEquals(50000, topUp.getAmount());
    }

    @Test
    public void testCustomTopUpBelowMinimumThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CustomTopUp(5000);
        });
    }

    @Test
    public void testCustomTopUpAboveMaximumThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CustomTopUp(2000000);
        });
    }
}
