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
        UUID userId = UUID.randomUUID();
        Report report = new Report(userId, ReportCategory.PAYMENT, "Payment issue");
        report.setId(UUID.randomUUID());

        // Call the method being tested
        notificationService.onStatusChanged(report, ReportStatus.PENDING, ReportStatus.ON_PROGRESS);

        // Capture and verify the notification created
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());

        Notification notification = notificationCaptor.getValue();
        assertEquals(userId, notification.getRecipientId());
        assertEquals("SYSTEM", notification.getSenderRole());
        assertTrue(notification.getTitle().contains("Report Status Updated"));
        assertTrue(notification.getMessage().contains("PENDING"));
        assertTrue(notification.getMessage().contains("ON_PROGRESS"));
        assertEquals("STATUS_UPDATE", notification.getType());
        assertEquals(report.getId(), notification.getRelatedEntityId());
        assertFalse(notification.isRead());
    }

    @Test
    public void testOnResponseAdded() {
        // Create a test report
        UUID userId = UUID.randomUUID();
        Report report = new Report(userId, ReportCategory.TICKET, "Ticket issue");
        report.setId(UUID.randomUUID());

        // Create a test response
        ReportResponse response = new ReportResponse(UUID.randomUUID(), "ADMIN", "Admin response", report);

        // Call the method being tested
        notificationService.onResponseAdded(report, response);

        // Capture and verify the notification created
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());

        Notification notification = notificationCaptor.getValue();
        assertEquals(userId, notification.getRecipientId());
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
        Report report = new Report(UUID.randomUUID(), ReportCategory.EVENT, "Event issue");
        report.setId(UUID.randomUUID());

        // Mock userService
        List<UUID> adminIds = Arrays.asList(
                UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"),
                UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12")
        );
        when(userService.getAdminIds()).thenReturn(adminIds);

        // Call the method being tested
        notificationService.notifyNewReport(report);

        // Verify notifications were created for both admins
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    public void testNotifyOrganizerOfReport() {
        // Create a test report
        Report report = new Report(UUID.randomUUID(), ReportCategory.EVENT, "Event issue");
        report.setId(UUID.randomUUID());

        // Create event ID
        UUID eventId = UUID.randomUUID();

        // Mock userService
        List<UUID> organizerIds = Arrays.asList(
                UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13")
        );
        when(userService.getOrganizerIds(eventId)).thenReturn(organizerIds);

        // Call the method being tested
        notificationService.notifyOrganizerOfReport(report, eventId);

        // Verify notification was created for the organizer
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    public void testGetUserNotifications() {
        // Create test data
        UUID userId = UUID.randomUUID();
        Notification notification1 = new Notification(userId, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        Notification notification2 = new Notification(userId, "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());

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
    public void testGetUnreadUserNotifications() {
        // Create test data
        UUID userId = UUID.randomUUID();
        Notification notification1 = new Notification(userId, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
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
    public void testCountUnreadNotifications() {
        // Create test data
        UUID userId = UUID.randomUUID();
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
}