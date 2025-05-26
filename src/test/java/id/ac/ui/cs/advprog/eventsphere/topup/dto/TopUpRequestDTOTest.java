package id.ac.ui.cs.advprog.eventsphere.topup.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class TopUpRequestDTOTest {
    
    @Test
    @DisplayName("Should create TopUpRequestDTO with all fields")
    public void testCreateTopUpRequestDTO() {
        TopUpRequestDTO dto = TopUpRequestDTO.builder()
                .amount(50000)
                .topUpType("FIXED")
                .build();
        
        assertEquals(50000, dto.getAmount());
        assertEquals("FIXED", dto.getTopUpType());
    }
    
    @Test
    @DisplayName("Should create TopUpRequestDTO with no-args constructor and setters")
    public void testCreateTopUpRequestDTONoArgsConstructor() {
        TopUpRequestDTO dto = new TopUpRequestDTO();
        dto.setAmount(50000);
        dto.setTopUpType("CUSTOM");
        
        assertEquals(50000, dto.getAmount());
        assertEquals("CUSTOM", dto.getTopUpType());
    }
    
    @Test
    @DisplayName("Should test equals, hashCode and toString methods")
    public void testEqualsHashCodeToString() {
        TopUpRequestDTO dto1 = TopUpRequestDTO.builder()
                .amount(50000)
                .topUpType("FIXED")
                .build();
        
        TopUpRequestDTO dto2 = TopUpRequestDTO.builder()
                .amount(50000)
                .topUpType("FIXED")
                .build();
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        String toStringResult = dto1.toString();
        assertTrue(toStringResult.contains("50000"));
        assertTrue(toStringResult.contains("FIXED"));
    }
}