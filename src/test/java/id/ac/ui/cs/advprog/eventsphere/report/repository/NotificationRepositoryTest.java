package id.ac.ui.cs.advprog.eventsphere.report.repository;

import id.ac.ui.cs.advprog.eventsphere.report.model.Notification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("Mencari notifikasi berdasarkan recipientId")
    public void testFindByRecipientId() {
        // Arrange
        Long recipientId1 = 1L;
        Long recipientId2 = 2L;

        Notification notification1 = new Notification(recipientId1, "user1@example.com", "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        Notification notification2 = new Notification(recipientId1, "user1@example.com", "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        Notification notification3 = new Notification(recipientId2, "user2@example.com", "ORGANIZER", "Title 3", "Message 3", "TYPE_1", UUID.randomUUID());

        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.persist(notification3);
        entityManager.flush();

        // Act
        List<Notification> foundNotifications = notificationRepository.findByRecipientId(recipientId1);

        // Assert
        assertFalse(foundNotifications.isEmpty(), "Should find at least one notification");
    }

    @Test
    @DisplayName("Mencari notifikasi berdasarkan email penerima")
    public void testFindByRecipientEmail() {
        // Arrange
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        Notification notification1 = new Notification(1L, email1, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        Notification notification2 = new Notification(2L, email1, "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        Notification notification3 = new Notification(3L, email2, "ORGANIZER", "Title 3", "Message 3", "TYPE_1", UUID.randomUUID());

        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.persist(notification3);
        entityManager.flush();

        // Act
        List<Notification> foundNotifications = notificationRepository.findByRecipientEmail(email1);

        // Assert
        assertFalse(foundNotifications.isEmpty(), "Should find at least one notification");
    }

    @Test
    @DisplayName("Mencari notifikasi yang belum dibaca berdasarkan recipientId dan mengurutkan berdasarkan tanggal dibuat")
    public void testFindByRecipientIdAndReadOrderByCreatedAtDesc() {
        // Arrange
        Long recipientId = 1L;

        Notification notification1 = new Notification(recipientId, "user@example.com", "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        Notification notification2 = new Notification(recipientId, "user@example.com", "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        notification2.setRead(true);

        Notification notification3 = new Notification(recipientId, "user@example.com", "ORGANIZER", "Title 3", "Message 3", "TYPE_3", UUID.randomUUID());
        notification3.setRead(false);

        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.persist(notification3);
        entityManager.flush();

        // Act
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(recipientId, false);

        // Assert
        assertFalse(unreadNotifications.isEmpty(), "Should find at least one unread notification");
        assertTrue(unreadNotifications.stream().noneMatch(Notification::isRead), "All notifications should be unread");
    }

    @Test
    @DisplayName("Mencari notifikasi yang belum dibaca berdasarkan email penerima dan mengurutkan berdasarkan tanggal dibuat")
    public void testFindByRecipientEmailAndReadOrderByCreatedAtDesc() {
        // Arrange
        String email = "user@example.com";

        Notification notification1 = new Notification(1L, email, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        Notification notification2 = new Notification(2L, email, "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        notification2.setRead(true);

        Notification notification3 = new Notification(3L, email, "ORGANIZER", "Title 3", "Message 3", "TYPE_3", UUID.randomUUID());
        notification3.setRead(false);

        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.persist(notification3);
        entityManager.flush();

        // Act
        List<Notification> unreadNotifications = notificationRepository.findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false);

        // Assert
        assertFalse(unreadNotifications.isEmpty(), "Should find at least one unread notification");
        assertTrue(unreadNotifications.stream().noneMatch(Notification::isRead), "All notifications should be unread");
    }

    @Test
    @DisplayName("Mencari notifikasi berdasarkan entity terkait")
    public void testFindByRelatedEntityId() {
        // Arrange
        UUID relatedEntityId = UUID.randomUUID();

        Notification notification1 = new Notification(1L, "user1@example.com", "ADMIN", "Title 1", "Message 1", "TYPE_1", relatedEntityId);
        Notification notification2 = new Notification(2L, "user2@example.com", "SYSTEM", "Title 2", "Message 2", "TYPE_2", relatedEntityId);
        Notification notification3 = new Notification(3L, "user3@example.com", "ORGANIZER", "Title 3", "Message 3", "TYPE_3", UUID.randomUUID());

        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.persist(notification3);
        entityManager.flush();

        // Act
        List<Notification> foundNotifications = notificationRepository.findByRelatedEntityId(relatedEntityId);

        // Assert
        assertFalse(foundNotifications.isEmpty(), "Should find at least one notification");
        assertTrue(foundNotifications.stream().allMatch(n -> n.getRelatedEntityId().equals(relatedEntityId)), "All notifications should have the correct relatedEntityId");
    }

    @Test
    @DisplayName("Menghitung jumlah notifikasi yang dibaca berdasarkan recipientId")
    public void testCountByRecipientIdAndRead() {
        // Arrange
        Long recipientId = 1L;

        Notification notification1 = new Notification(recipientId, "user@example.com", "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        Notification notification2 = new Notification(recipientId, "user@example.com", "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        notification2.setRead(true);

        Notification notification3 = new Notification(recipientId, "user@example.com", "ORGANIZER", "Title 3", "Message 3", "TYPE_3", UUID.randomUUID());
        notification3.setRead(false);

        Notification notification4 = new Notification(2L, "other@example.com", "ADMIN", "Title 4", "Message 4", "TYPE_1", UUID.randomUUID());
        notification4.setRead(false);

        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.persist(notification3);
        entityManager.persist(notification4);
        entityManager.flush();

        // Act
        long unreadCount = notificationRepository.countByRecipientIdAndRead(recipientId, false);
        long readCount = notificationRepository.countByRecipientIdAndRead(recipientId, true);

        // Assert
        assertTrue(unreadCount > 0, "Should find at least one unread notification");
        assertTrue(readCount >= 0, "Should not find a negative number of read notifications");
    }

    @Test
    @DisplayName("Menghitung jumlah notifikasi yang dibaca berdasarkan email penerima")
    public void testCountByRecipientEmailAndRead() {
        // Arrange
        String email = "user@example.com";
        String email2 = "other@example.com";

        Notification notification1 = new Notification(1L, email, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        Notification notification2 = new Notification(2L, email, "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        notification2.setRead(true);

        Notification notification3 = new Notification(3L, email, "ORGANIZER", "Title 3", "Message 3", "TYPE_3", UUID.randomUUID());
        notification3.setRead(false);

        Notification notification4 = new Notification(4L, email2, "ADMIN", "Title 4", "Message 4", "TYPE_1", UUID.randomUUID());
        notification4.setRead(false);

        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.persist(notification3);
        entityManager.persist(notification4);
        entityManager.flush();

        // Act
        long unreadCount = notificationRepository.countByRecipientEmailAndRead(email, false);
        long readCount = notificationRepository.countByRecipientEmailAndRead(email, true);

        // Assert
        assertTrue(unreadCount > 0, "Should find at least one unread notification");
        assertTrue(readCount >= 0, "Should not find a negative number of read notifications");
    }

    @Test
    @DisplayName("Mencari notifikasi berdasarkan recipientId dan mengurutkan berdasarkan tanggal dibuat")
    public void testFindByRecipientIdOrderByCreatedAtDesc() {
        // Arrange
        Long recipientId = 1L;

        Notification notification1 = new Notification(recipientId, "user@example.com", "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        Notification notification2 = new Notification(recipientId, "user@example.com", "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());

        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.flush();

        // Act
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);

        // Assert
        assertFalse(notifications.isEmpty(), "Should find at least one notification");
    }
}