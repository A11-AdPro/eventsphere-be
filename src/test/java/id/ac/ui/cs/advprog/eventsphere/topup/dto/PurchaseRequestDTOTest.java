package id.ac.ui.cs.advprog.eventsphere.topup.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class PurchaseRequestDTOTest {
    
    @Test
    @DisplayName("Should create PurchaseRequestDTO with all fields")
    public void testCreatePurchaseRequestDTO() {
        PurchaseRequestDTO dto = PurchaseRequestDTO.builder()
                .eventId("event-123")
                .amount(50000)
                .description("Purchase ticket")
                .build();
        
        assertEquals("event-123", dto.getEventId());
        assertEquals(50000, dto.getAmount());
        assertEquals("Purchase ticket", dto.getDescription());
    }
    
    @Test
    @DisplayName("Should create PurchaseRequestDTO with no-args constructor and setters")
    public void testCreatePurchaseRequestDTONoArgsConstructor() {
        PurchaseRequestDTO dto = new PurchaseRequestDTO();
        dto.setEventId("event-123");
        dto.setAmount(50000);
        dto.setDescription("Purchase ticket");
        
        assertEquals("event-123", dto.getEventId());
        assertEquals(50000, dto.getAmount());
        assertEquals("Purchase ticket", dto.getDescription());
    }
    
    @Test
    @DisplayName("Should test equals, hashCode and toString methods")
    public void testEqualsHashCodeToString() {
        PurchaseRequestDTO dto1 = PurchaseRequestDTO.builder()
                .eventId("event-123")
                .amount(50000)
                .description("Purchase ticket")
                .build();
        
        PurchaseRequestDTO dto2 = PurchaseRequestDTO.builder()
                .eventId("event-123")
                .amount(50000)
                .description("Purchase ticket")
                .build();
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        String toStringResult = dto1.toString();
        assertTrue(toStringResult.contains("event-123"));
        assertTrue(toStringResult.contains("50000"));
        assertTrue(toStringResult.contains("Purchase ticket"));
    }
}