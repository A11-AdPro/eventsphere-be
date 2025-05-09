package id.ac.ui.cs.advprog.eventsphere.topup.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class CustomTopUpTest {

    @Test
    @DisplayName("Should create CustomTopUp with valid amount")
    public void testCreateValidCustomTopUp() {
        int amount = 50000;
        CustomTopUp topUp = new CustomTopUp(amount);
        
        assertEquals(amount, topUp.getAmount());
        assertEquals("CUSTOM", topUp.getType());
    }
    
    @Test
    @DisplayName("Should throw exception when amount below minimum")
    public void testCreateCustomTopUpBelowMinimum() {
        int belowMinAmount = 5000;
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new CustomTopUp(belowMinAmount);
        });
        
        assertTrue(exception.getMessage().contains("must be between 10,000 and 1,000,000"));
    }
    
    @Test
    @DisplayName("Should throw exception when amount above maximum")
    public void testCreateCustomTopUpAboveMaximum() {
        int aboveMaxAmount = 1500000;
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new CustomTopUp(aboveMaxAmount);
        });
        
        assertTrue(exception.getMessage().contains("must be between 10,000 and 1,000,000"));
    }
    
    @Test
    @DisplayName("Should create CustomTopUp with boundary values")
    public void testCreateCustomTopUpBoundaryValues() {
        int minAmount = 10000;
        int maxAmount = 1000000;
        
        CustomTopUp minTopUp = new CustomTopUp(minAmount);
        CustomTopUp maxTopUp = new CustomTopUp(maxAmount);
        
        assertEquals(minAmount, minTopUp.getAmount());
        assertEquals(maxAmount, maxTopUp.getAmount());
    }
}