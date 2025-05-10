package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.model.Notification;
import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private UserService userService;
    private NotificationService notificationService;

    @BeforeEach
    public void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        userService = mock(UserService.class);
        notificationService = new NotificationService(notificationRepository, userService);
    }

    @Test
    public void testOnStatusChanged() {
        // Create a test report
        Long userId = 1L;
        Report report = new Report(userId, "user@example.com", ReportCategory.PAYMENT, "Payment issue");
        report.setId(UUID.randomUUID());

        // Setup ReportStatus display names
        ReportStatus oldStatus = ReportStatus.PENDING;
        ReportStatus newStatus = ReportStatus.ON_PROGRESS;

        // Call the method being tested
        notificationService.onStatusChanged(report, oldStatus, newStatus);

        // Capture and verify the notification created
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());

        Notification notification = notificationCaptor.getValue();
        assertEquals(userId, notification.getRecipientId());
        assertEquals("user@example.com", notification.getRecipientEmail());
        assertEquals("SYSTEM", notification.getSenderRole());
        assertTrue(notification.getTitle().contains("Report Status Updated"));
        assertTrue(notification.getMessage().contains(oldStatus.getDisplayName()));
        assertTrue(notification.getMessage().contains(newStatus.getDisplayName()));
        assertEquals("STATUS_UPDATE", notification.getType());
        assertEquals(report.getId(), notification.getRelatedEntityId());
        assertFalse(notification.isRead());
    }

    @Test
    public void testOnResponseAdded() {
        // Create a test report
        Long userId = 1L;
        Report report = new Report(userId, "user@example.com", ReportCategory.TICKET, "Ticket issue");
        report.setId(UUID.randomUUID());

        // Create a test response
        ReportResponse response = new ReportResponse(2L, "admin@example.com", "ADMIN", "Admin response", report);

        // Call the method being tested
        notificationService.onResponseAdded(report, response);

        // Capture and verify the notification created
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());

        Notification notification = notificationCaptor.getValue();
        assertEquals(userId, notification.getRecipientId());
        assertEquals("user@example.com", notification.getRecipientEmail());
        assertEquals("ADMIN", notification.getSenderRole());
        assertTrue(notification.getTitle().contains("New Response"));
        assertTrue(notification.getMessage().contains("Admin response"));
        assertEquals("NEW_RESPONSE", notification.getType());
        assertEquals(report.getId(), notification.getRelatedEntityId());
        assertFalse(notification.isRead());
    }

    @Test
    public void testNotifyNewReport() {
        // Create a test report
        Report report = new Report(1L, "attendee@example.com", ReportCategory.EVENT, "Event issue");
        report.setId(UUID.randomUUID());

        // Mock userService
        List<Long> adminIds = Arrays.asList(1L, 2L);
        when(userService.getAdminIds()).thenReturn(adminIds);
        when(userService.getUserEmail(1L)).thenReturn("admin1@example.com");
        when(userService.getUserEmail(2L)).thenReturn("admin2@example.com");

        // Call the method being tested
        notificationService.notifyNewReport(report);

        // Verify notifications were created for both admins
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    public void testNotifyOrganizerOfReport() {
        // Create a test report
        Report report = new Report(1L, "attendee@example.com", ReportCategory.EVENT, "Event issue");
        report.setId(UUID.randomUUID());

        // Create event ID
        UUID eventId = UUID.randomUUID();

        // Mock userService
        List<Long> organizerIds = Arrays.asList(3L);
        when(userService.getOrganizerIds(eventId)).thenReturn(organizerIds);
        when(userService.getUserEmail(3L)).thenReturn("organizer@example.com");

        // Call the method being tested
        notificationService.notifyOrganizerOfReport(report, eventId);

        // Verify notification was created for the organizer
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    public void testGetUserNotifications() {
        // Create test data
        Long userId = 1L;
        Notification notification1 = new Notification(userId, "user@example.com", "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        Notification notification2 = new Notification(userId, "user@example.com", "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());

        List<Notification> notifications = Arrays.asList(notification1, notification2);

        // Mock repository behavior
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)).thenReturn(notifications);

        // Call service method
        List<Notification> result = notificationService.getUserNotifications(userId);

        // Verify results
        assertEquals(2, result.size());
        assertEquals(notifications, result);

        // Verify repository interaction
        verify(notificationRepository).findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Test
    public void testGetUserNotificationsByEmail() {
        // Create test data
        String email = "user@example.com";
        Notification notification1 = new Notification(1L, email, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        Notification notification2 = new Notification(1L, email, "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());

        List<Notification> notifications = Arrays.asList(notification1, notification2);

        // Mock repository behavior
        when(notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email)).thenReturn(notifications);

        // Call service method
        List<Notification> result = notificationService.getUserNotificationsByEmail(email);

        // Verify results
        assertEquals(2, result.size());
        assertEquals(notifications, result);

        // Verify repository interaction
        verify(notificationRepository).findByRecipientEmailOrderByCreatedAtDesc(email);
    }

    @Test
    public void testGetUnreadUserNotifications() {
        // Create test data
        Long userId = 1L;
        Notification notification1 = new Notification(userId, "user@example.com", "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        List<Notification> notifications = Arrays.asList(notification1);

        // Mock repository behavior
        when(notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false)).thenReturn(notifications);

        // Call service method
        List<Notification> result = notificationService.getUnreadUserNotifications(userId);

        // Verify results
        assertEquals(1, result.size());
        assertEquals(notifications, result);

        // Verify repository interaction
        verify(notificationRepository).findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false);
    }

    @Test
    public void testGetUnreadUserNotificationsByEmail() {
        // Create test data
        String email = "user@example.com";
        Notification notification1 = new Notification(1L, email, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        List<Notification> notifications = Arrays.asList(notification1);

        // Mock repository behavior
        when(notificationRepository.findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false)).thenReturn(notifications);

        // Call service method
        List<Notification> result = notificationService.getUnreadUserNotificationsByEmail(email);

        // Verify results
        assertEquals(1, result.size());
        assertEquals(notifications, result);

        // Verify repository interaction
        verify(notificationRepository).findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false);
    }

    @Test
    public void testCountUnreadNotifications() {
        // Create test data
        Long userId = 1L;
        long expectedCount = 3;

        // Mock repository behavior
        when(notificationRepository.countByRecipientIdAndRead(userId, false)).thenReturn(expectedCount);

        // Call service method
        long result = notificationService.countUnreadNotifications(userId);

        // Verify result
        assertEquals(expectedCount, result);

        // Verify repository interaction
        verify(notificationRepository).countByRecipientIdAndRead(userId, false);
    }

    @Test
    public void testCountUnreadNotificationsByEmail() {
        // Create test data
        String email = "user@example.com";
        long expectedCount = 3;

        // Mock repository behavior
        when(notificationRepository.countByRecipientEmailAndRead(email, false)).thenReturn(expectedCount);

        // Call service method
        long result = notificationService.countUnreadNotificationsByEmail(email);

        // Verify result
        assertEquals(expectedCount, result);

        // Verify repository interaction
        verify(notificationRepository).countByRecipientEmailAndRead(email, false);
    }

    @Test
    public void testMarkNotificationAsRead() {
        UUID notificationId = UUID.randomUUID();
        Long userId = 1L;

        // Create a test notification
        Notification notification = new Notification(userId, "user@example.com", "ADMIN", "Title", "Message", "TYPE_1", UUID.randomUUID());
        notification.setId(notificationId);
        notification.setRead(false);  // Set as unread initially

        // Mock the repository behavior
        when(notificationRepository.findById(notificationId)).thenReturn(java.util.Optional.of(notification));

        // Mock the behavior of the save method
        when(notificationRepository.save(notification)).thenReturn(notification);

        // Call the service method to mark as read
        Notification updatedNotification = notificationService.markNotificationAsRead(notificationId);

        // Verify the notification was marked as read
        assertTrue(updatedNotification.isRead(), "The notification should be marked as read.");

        // Verify repository interaction
        verify(notificationRepository).save(updatedNotification);
    }

    @Test
    public void testMarkAllNotificationsAsRead() {
        Long userId = 1L;

        // Create some unread notifications
        Notification notification1 = new Notification(userId, "user@example.com", "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);
        Notification notification2 = new Notification(userId, "user@example.com", "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        notification2.setRead(false);

        List<Notification> unreadNotifications = Arrays.asList(notification1, notification2);

        // Mock the repository behavior
        when(notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false)).thenReturn(unreadNotifications);

        // Call the service method to mark all notifications as read
        notificationService.markAllNotificationsAsRead(userId);

        // Verify that save was called for each notification
        verify(notificationRepository, times(2)).save(any(Notification.class));

        // Verify that the notifications are now marked as read
        assertTrue(notification1.isRead());
        assertTrue(notification2.isRead());
    }

    @Test
    public void testMarkAllNotificationsAsReadByEmail() {
        String email = "user@example.com";

        // Create some unread notifications
        Notification notification1 = new Notification(1L, email, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);
        Notification notification2 = new Notification(2L, email, "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        notification2.setRead(false);

        List<Notification> unreadNotifications = Arrays.asList(notification1, notification2);

        // Mock the repository behavior
        when(notificationRepository.findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false)).thenReturn(unreadNotifications);

        // Call the service method to mark all notifications as read
        notificationService.markAllNotificationsAsReadByEmail(email);

        // Verify that save was called for each notification
        verify(notificationRepository, times(2)).save(any(Notification.class));

        // Verify that the notifications are now marked as read
        assertTrue(notification1.isRead());
        assertTrue(notification2.isRead());
    }

    @Test
    public void testMarkNotificationAsRead_NotificationNotFound() {
        // Create a random UUID for a non-existent notification
        UUID nonExistentId = UUID.randomUUID();

        // Mock the repository to return empty optional (notification not found)
        when(notificationRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        // Call the service method and verify that RuntimeException is thrown with correct message
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> notificationService.markNotificationAsRead(nonExistentId)
        );

        // Verify exception message
        assertEquals("Notification not found", exception.getMessage());

        // Verify repository was called but save was not
        verify(notificationRepository).findById(nonExistentId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }
}