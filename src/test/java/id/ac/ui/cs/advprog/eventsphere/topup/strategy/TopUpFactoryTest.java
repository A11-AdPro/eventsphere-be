package id.ac.ui.cs.advprog.eventsphere.topup.strategy;

import id.ac.ui.cs.advprog.eventsphere.topup.model.CustomTopUp;
import id.ac.ui.cs.advprog.eventsphere.topup.model.FixedTopUp;
import id.ac.ui.cs.advprog.eventsphere.topup.model.TopUp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class TopUpFactoryTest {
    
    private TopUpFactory topUpFactory;
    
    @BeforeEach
    public void setUp() {
        topUpFactory = new TopUpFactory();
    }
    
    @Test
    @DisplayName("Should create CustomTopUp")
    public void testCreateCustomTopUp() {
        TopUp topUp = topUpFactory.createTopUp("CUSTOM", 50000);
        
        assertTrue(topUp instanceof CustomTopUp);
        assertEquals(50000, topUp.getAmount());
        assertEquals("CUSTOM", topUp.getType());
    }
    
    @Test
    @DisplayName("Should create FixedTopUp")
    public void testCreateFixedTopUp() {
        TopUp topUp = topUpFactory.createTopUp("FIXED", 50000);
        
        assertTrue(topUp instanceof FixedTopUp);
        assertEquals(50000, topUp.getAmount());
        assertEquals("FIXED", topUp.getType());
    }
    
    @Test
    @DisplayName("Should throw exception for invalid topUp type")
    public void testCreateInvalidTopUpType() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            topUpFactory.createTopUp("INVALID", 50000);
        });
        
        assertEquals("Invalid top-up type", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should create small FixedTopUp")
    public void testCreateSmallTopUp() {
        TopUp topUp = topUpFactory.createSmallTopUp();
        
        assertTrue(topUp instanceof FixedTopUp);
        assertEquals(TopUpFactory.SMALL_FIXED, topUp.getAmount());
        assertEquals("FIXED", topUp.getType());
    }
    
    @Test
    @DisplayName("Should create medium FixedTopUp")
    public void testCreateMediumTopUp() {
        TopUp topUp = topUpFactory.createMediumTopUp();
        
        assertTrue(topUp instanceof FixedTopUp);
        assertEquals(TopUpFactory.MEDIUM_FIXED, topUp.getAmount());
        assertEquals("FIXED", topUp.getType());
    }
    
    @Test
    @DisplayName("Should create large FixedTopUp")
    public void testCreateLargeTopUp() {
        TopUp topUp = topUpFactory.createLargeTopUp();
        
        assertTrue(topUp instanceof FixedTopUp);
        assertEquals(TopUpFactory.LARGE_FIXED, topUp.getAmount());
        assertEquals("FIXED", topUp.getType());
    }
}