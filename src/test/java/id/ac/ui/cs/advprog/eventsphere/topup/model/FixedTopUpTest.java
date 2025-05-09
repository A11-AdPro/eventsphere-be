package id.ac.ui.cs.advprog.eventsphere.topup.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class FixedTopUpTest {

    @Test
    @DisplayName("Should create FixedTopUp with correct amount")
    public void testCreateFixedTopUp() {
        int amount = 50000;
        FixedTopUp topUp = new FixedTopUp(amount);
        
        assertEquals(amount, topUp.getAmount());
        assertEquals("FIXED", topUp.getType());
    }
    
    @Test
    @DisplayName("Should create FixedTopUp with zero amount")
    public void testCreateFixedTopUpZeroAmount() {
        int amount = 0;
        FixedTopUp topUp = new FixedTopUp(amount);
        
        assertEquals(amount, topUp.getAmount());
    }
    
    @Test
    @DisplayName("Should create FixedTopUp with negative amount")
    public void testCreateFixedTopUpNegativeAmount() {
        int amount = -5000;
        FixedTopUp topUp = new FixedTopUp(amount);
        
        assertEquals(amount, topUp.getAmount());
    }
}