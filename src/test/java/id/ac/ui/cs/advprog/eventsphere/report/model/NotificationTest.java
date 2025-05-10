package id.ac.ui.cs.advprog.eventsphere.report.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class NotificationTest {

    @Test
    public void testCreateNotification() {
        Long recipientId = 1L;
        String recipientEmail = "user@example.com";
        UUID relatedEntityId = UUID.randomUUID();
        String senderRole = "ADMIN";
        String title = "Test Title";
        String message = "Test Message";
        String type = "TEST_TYPE";

        Notification notification = new Notification(
                recipientId, recipientEmail, senderRole, title, message, type, relatedEntityId);

        // ID akan null sampai disimpan ke database
        // assertNotNull(notification.getId());
        assertEquals(recipientId, notification.getRecipientId());
        assertEquals(recipientEmail, notification.getRecipientEmail());
        assertEquals(senderRole, notification.getSenderRole());
        assertEquals(title, notification.getTitle());
        assertEquals(message, notification.getMessage());
        assertEquals(type, notification.getType());
        assertEquals(relatedEntityId, notification.getRelatedEntityId());
        assertNotNull(notification.getCreatedAt());
        assertFalse(notification.isRead());
    }

    @Test
    public void testNotificationSettersAndGetters() {
        Notification notification = new Notification();
        UUID id = UUID.randomUUID();
        Long recipientId = 1L;
        String recipientEmail = "user@example.com";
        UUID relatedEntityId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        notification.setId(id);
        notification.setRecipientId(recipientId);
        notification.setRecipientEmail(recipientEmail);
        notification.setSenderRole("ORGANIZER");
        notification.setTitle("Test Title");
        notification.setMessage("Test Message");
        notification.setRead(true);
        notification.setType("TEST_TYPE");
        notification.setRelatedEntityId(relatedEntityId);
        notification.setCreatedAt(now);

        assertEquals(id, notification.getId());
        assertEquals(recipientId, notification.getRecipientId());
        assertEquals(recipientEmail, notification.getRecipientEmail());
        assertEquals("ORGANIZER", notification.getSenderRole());
        assertEquals("Test Title", notification.getTitle());
        assertEquals("Test Message", notification.getMessage());
        assertTrue(notification.isRead());
        assertEquals("TEST_TYPE", notification.getType());
        assertEquals(relatedEntityId, notification.getRelatedEntityId());
        assertEquals(now, notification.getCreatedAt());
    }

    @Test
    public void testMarkAsRead() {
        Notification notification = new Notification(
                1L, "user@example.com", "ADMIN", "Title", "Message", "TYPE", UUID.randomUUID());

        assertFalse(notification.isRead());

        notification.markAsRead();

        assertTrue(notification.isRead());
    }
}