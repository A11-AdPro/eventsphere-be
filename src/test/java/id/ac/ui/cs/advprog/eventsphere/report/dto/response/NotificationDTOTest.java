package id.ac.ui.cs.advprog.eventsphere.report.dto.response;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class NotificationDTOTest {

    @Test
    public void testNotificationDTOSettersAndGetters() {
        UUID id = UUID.randomUUID();
        Long recipientId = 1L;
        String recipientEmail = "user@example.com";
        UUID relatedEntityId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        NotificationDTO dto = new NotificationDTO();
        dto.setId(id);
        dto.setRecipientId(recipientId);
        dto.setRecipientEmail(recipientEmail);
        dto.setSenderRole("ADMIN");
        dto.setTitle("Test Title");
        dto.setMessage("Test Message");
        dto.setRead(true);
        dto.setType("TEST_TYPE");
        dto.setRelatedEntityId(relatedEntityId);
        dto.setCreatedAt(now);

        assertEquals(id, dto.getId());
        assertEquals(recipientId, dto.getRecipientId());
        assertEquals(recipientEmail, dto.getRecipientEmail());
        assertEquals("ADMIN", dto.getSenderRole());
        assertEquals("Test Title", dto.getTitle());
        assertEquals("Test Message", dto.getMessage());
        assertTrue(dto.isRead());
        assertEquals("TEST_TYPE", dto.getType());
        assertEquals(relatedEntityId, dto.getRelatedEntityId());
        assertEquals(now, dto.getCreatedAt());
    }
}