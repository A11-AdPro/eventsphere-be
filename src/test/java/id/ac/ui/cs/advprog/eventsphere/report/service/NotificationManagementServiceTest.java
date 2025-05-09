package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.dto.response.NotificationDTO;
import id.ac.ui.cs.advprog.eventsphere.report.model.Notification;
import id.ac.ui.cs.advprog.eventsphere.report.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NotificationManagementServiceTest {

    private NotificationRepository notificationRepository;
    private NotificationManagementService notificationService;

    @BeforeEach
    public void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        notificationService = new NotificationManagementService(notificationRepository);
    }

    @Test
    public void testGetUserNotifications() {
        // Create test data
        UUID userId = UUID.randomUUID();
        Notification notification1 = new Notification(userId, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        Notification notification2 = new Notification(userId, "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());

        List<Notification> notificationList = Arrays.asList(notification1, notification2);

        // Mock repository method
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)).thenReturn(notificationList);

        // Call service method
        List<NotificationDTO> result = notificationService.getUserNotifications(userId);

        // Verify results
        assertEquals(2, result.size());
        assertEquals("Title 1", result.get(0).getTitle());
        assertEquals("Title 2", result.get(1).getTitle());

        // Verify repository interaction
        verify(notificationRepository).findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Test
    public void testGetUnreadUserNotifications() {
        // Create test data
        UUID userId = UUID.randomUUID();
        Notification notification1 = new Notification(userId, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        Notification notification2 = new Notification(userId, "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        notification2.setRead(false);

        List<Notification> notificationList = Arrays.asList(notification1, notification2);

        // Mock repository method
        when(notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false)).thenReturn(notificationList);

        // Call service method
        List<NotificationDTO> result = notificationService.getUnreadUserNotifications(userId);

        // Verify results
        assertEquals(2, result.size());
        assertFalse(result.get(0).isRead());
        assertFalse(result.get(1).isRead());

        // Verify repository interaction
        verify(notificationRepository).findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false);
    }

    @Test
    public void testCountUnreadNotifications() {
        // Create test data
        UUID userId = UUID.randomUUID();
        long expectedCount = 5;

        // Mock repository method
        when(notificationRepository.countByRecipientIdAndRead(userId, false)).thenReturn(expectedCount);

        // Call service method
        long result = notificationService.countUnreadNotifications(userId);

        // Verify results
        assertEquals(expectedCount, result);

        // Verify repository interaction
        verify(notificationRepository).countByRecipientIdAndRead(userId, false);
    }

    @Test
    public void testMarkNotificationAsRead() {
        // Create test data
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification(
                UUID.randomUUID(), "ADMIN", "Title", "Message", "TYPE", UUID.randomUUID());
        notification.setId(notificationId);
        notification.setRead(false);

        // Mock repository behavior
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        // Call service method
        NotificationDTO result = notificationService.markNotificationAsRead(notificationId);

        // Verify notification was marked as read
        assertTrue(result.isRead());

        // Verify repository interactions
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(notification);
    }

    @Test
    public void testMarkNotificationAsReadNotFound() {
        // Create test data
        UUID notificationId = UUID.randomUUID();

        // Mock repository behavior
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // Call service method and verify exception is thrown
        assertThrows(EntityNotFoundException.class, () -> {
            notificationService.markNotificationAsRead(notificationId);
        });

        // Verify repository interaction
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    public void testMarkAllNotificationsAsRead() {
        // Create test data
        UUID userId = UUID.randomUUID();
        Notification notification1 = new Notification(userId, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        Notification notification2 = new Notification(userId, "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        notification2.setRead(false);

        List<Notification> notificationList = Arrays.asList(notification1, notification2);

        // Mock repository method
        when(notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false)).thenReturn(notificationList);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        // Call service method
        notificationService.markAllNotificationsAsRead(userId);

        // Verify repository interactions
        verify(notificationRepository).findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false);
        verify(notificationRepository, times(2)).save(any(Notification.class));

        // Verify notifications were marked as read
        assertTrue(notification1.isRead());
        assertTrue(notification2.isRead());
    }

    @Test
    public void testDeleteNotification() {
        // Create test data
        UUID notificationId = UUID.randomUUID();

        // Mock repository behavior
        when(notificationRepository.existsById(notificationId)).thenReturn(true);
        doNothing().when(notificationRepository).deleteById(notificationId);

        // Call service method
        notificationService.deleteNotification(notificationId);

        // Verify repository interactions
        verify(notificationRepository).existsById(notificationId);
        verify(notificationRepository).deleteById(notificationId);
    }

    @Test
    public void testDeleteNotificationNotFound() {
        // Create test data
        UUID notificationId = UUID.randomUUID();

        // Mock repository behavior
        when(notificationRepository.existsById(notificationId)).thenReturn(false);

        // Call service method and verify exception is thrown
        assertThrows(EntityNotFoundException.class, () -> {
            notificationService.deleteNotification(notificationId);
        });

        // Verify repository interaction
        verify(notificationRepository).existsById(notificationId);
        verify(notificationRepository, never()).deleteById(any(UUID.class));
    }
}