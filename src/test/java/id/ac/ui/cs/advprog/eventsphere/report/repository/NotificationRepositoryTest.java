package id.ac.ui.cs.advprog.eventsphere.report.repository;

import id.ac.ui.cs.advprog.eventsphere.report.model.Notification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    public void testFindByRecipientId() {
        // Create test data
        UUID recipientId1 = UUID.randomUUID();
        UUID recipientId2 = UUID.randomUUID();

        Notification notification1 = new Notification(recipientId1, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        Notification notification2 = new Notification(recipientId1, "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        Notification notification3 = new Notification(recipientId2, "ORGANIZER", "Title 3", "Message 3", "TYPE_1", UUID.randomUUID());

        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.persist(notification3);
        entityManager.flush();

        // Test repository method
        List<Notification> foundNotifications = notificationRepository.findByRecipientId(recipientId1);

        // Verify results
        assertEquals(2, foundNotifications.size());
        assertTrue(foundNotifications.stream().anyMatch(n -> n.getTitle().equals("Title 1")));
        assertTrue(foundNotifications.stream().anyMatch(n -> n.getTitle().equals("Title 2")));
    }

    @Test
    public void testFindByRecipientIdAndReadOrderByCreatedAtDesc() {
        // Create test data
        UUID recipientId = UUID.randomUUID();

        Notification notification1 = new Notification(recipientId, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        Notification notification2 = new Notification(recipientId, "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        notification2.setRead(true);

        Notification notification3 = new Notification(recipientId, "ORGANIZER", "Title 3", "Message 3", "TYPE_3", UUID.randomUUID());
        notification3.setRead(false);

        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.persist(notification3);
        entityManager.flush();

        // Test repository method
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(recipientId, false);

        // Verify results
        assertEquals(2, unreadNotifications.size());
        assertTrue(unreadNotifications.stream().allMatch(n -> !n.isRead()));
    }

    @Test
    public void testFindByRelatedEntityId() {
        // Create test data
        UUID relatedEntityId = UUID.randomUUID();

        Notification notification1 = new Notification(UUID.randomUUID(), "ADMIN", "Title 1", "Message 1", "TYPE_1", relatedEntityId);
        Notification notification2 = new Notification(UUID.randomUUID(), "SYSTEM", "Title 2", "Message 2", "TYPE_2", relatedEntityId);
        Notification notification3 = new Notification(UUID.randomUUID(), "ORGANIZER", "Title 3", "Message 3", "TYPE_3", UUID.randomUUID());

        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.persist(notification3);
        entityManager.flush();

        // Test repository method
        List<Notification> foundNotifications = notificationRepository.findByRelatedEntityId(relatedEntityId);

        // Verify results
        assertEquals(2, foundNotifications.size());
        assertTrue(foundNotifications.stream().allMatch(n -> n.getRelatedEntityId().equals(relatedEntityId)));
    }

    @Test
    public void testCountByRecipientIdAndRead() {
        // Create test data
        UUID recipientId = UUID.randomUUID();

        Notification notification1 = new Notification(recipientId, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        Notification notification2 = new Notification(recipientId, "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        notification2.setRead(true);

        Notification notification3 = new Notification(recipientId, "ORGANIZER", "Title 3", "Message 3", "TYPE_3", UUID.randomUUID());
        notification3.setRead(false);

        Notification notification4 = new Notification(UUID.randomUUID(), "ADMIN", "Title 4", "Message 4", "TYPE_1", UUID.randomUUID());
        notification4.setRead(false);

        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.persist(notification3);
        entityManager.persist(notification4);
        entityManager.flush();

        // Test repository method
        long unreadCount = notificationRepository.countByRecipientIdAndRead(recipientId, false);
        long readCount = notificationRepository.countByRecipientIdAndRead(recipientId, true);

        // Verify results
        assertEquals(2, unreadCount);
        assertEquals(1, readCount);
    }
}