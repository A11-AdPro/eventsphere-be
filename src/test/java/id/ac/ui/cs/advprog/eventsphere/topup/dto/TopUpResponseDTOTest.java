package id.ac.ui.cs.advprog.eventsphere.topup.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class TopUpResponseDTOTest {
    
    @Test
    @DisplayName("Should create TopUpResponseDTO with all fields")
    public void testCreateTopUpResponseDTO() {
        LocalDateTime now = LocalDateTime.now();
        TopUpResponseDTO dto = TopUpResponseDTO.builder()
                .transactionId("transaction-123")
                .userId(1L)
                .amount(50000)
                .newBalance(100000)
                .timestamp(now)
                .status("SUCCESS")
                .build();
        
        assertEquals("transaction-123", dto.getTransactionId());
        assertEquals(1L, dto.getUserId());
        assertEquals(50000, dto.getAmount());
        assertEquals(100000, dto.getNewBalance());
        assertEquals(now, dto.getTimestamp());
        assertEquals("SUCCESS", dto.getStatus());
    }
    
    @Test
    @DisplayName("Should create TopUpResponseDTO with no-args constructor and setters")
    public void testCreateTopUpResponseDTONoArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        TopUpResponseDTO dto = new TopUpResponseDTO();
        dto.setTransactionId("transaction-123");
        dto.setUserId(1L);
        dto.setAmount(50000);
        dto.setNewBalance(100000);
        dto.setTimestamp(now);
        dto.setStatus("SUCCESS");
        
        assertEquals("transaction-123", dto.getTransactionId());
        assertEquals(1L, dto.getUserId());
        assertEquals(50000, dto.getAmount());
        assertEquals(100000, dto.getNewBalance());
        assertEquals(now, dto.getTimestamp());
        assertEquals("SUCCESS", dto.getStatus());
    }
    
    @Test
    @DisplayName("Should test equals, hashCode and toString methods")
    public void testEqualsHashCodeToString() {
        LocalDateTime now = LocalDateTime.now();
        TopUpResponseDTO dto1 = TopUpResponseDTO.builder()
                .transactionId("transaction-123")
                .userId(1L)
                .amount(50000)
                .newBalance(100000)
                .timestamp(now)
                .status("SUCCESS")
                .build();
        
        TopUpResponseDTO dto2 = TopUpResponseDTO.builder()
                .transactionId("transaction-123")
                .userId(1L)
                .amount(50000)
                .newBalance(100000)
                .timestamp(now)
                .status("SUCCESS")
                .build();
        
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        
        String toStringResult = dto1.toString();
        assertTrue(toStringResult.contains("transaction-123"));
        assertTrue(toStringResult.contains("50000"));
        assertTrue(toStringResult.contains("100000"));
        assertTrue(toStringResult.contains("SUCCESS"));
    }
}