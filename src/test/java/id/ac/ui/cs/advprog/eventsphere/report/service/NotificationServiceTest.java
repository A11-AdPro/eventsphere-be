package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.model.Notification;
import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
    @DisplayName("Membuat notifikasi saat status laporan berubah")
    public void testOnStatusChanged() {
        // Arrange
        Long userId = 1L;
        Report report = new Report(userId, "user@example.com", ReportCategory.PAYMENT, "Payment issue");
        report.setId(UUID.randomUUID());
        ReportStatus oldStatus = ReportStatus.PENDING;
        ReportStatus newStatus = ReportStatus.ON_PROGRESS;

        // Act
        notificationService.onStatusChanged(report, oldStatus, newStatus);

        // Assert
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
    @DisplayName("Membuat notifikasi saat respons ditambahkan ke laporan")
    public void testOnResponseAdded() {
        // Arrange
        Long userId = 1L;
        Report report = new Report(userId, "user@example.com", ReportCategory.TICKET, "Ticket issue");
        report.setId(UUID.randomUUID());
        ReportResponse response = new ReportResponse(2L, "admin@example.com", "ADMIN", "Admin response", report);

        // Act
        notificationService.onResponseAdded(report, response);

        // Assert
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
    @DisplayName("Membuat notifikasi untuk admin saat laporan baru dibuat")
    public void testNotifyNewReport() throws Exception {
        // Arrange
        Report report = new Report(1L, "attendee@example.com", ReportCategory.EVENT, "Event issue");
        report.setId(UUID.randomUUID());

        List<Long> adminIds = Arrays.asList(1L, 2L);
        when(userService.getAdminIds()).thenReturn(adminIds);
        when(userService.getUserEmail(1L)).thenReturn("admin1@example.com");
        when(userService.getUserEmail(2L)).thenReturn("admin2@example.com");

        // Act
        CompletableFuture<Void> future = notificationService.notifyNewReportAsync(report);

        // Wait for async operation to complete
        future.get(5, TimeUnit.SECONDS);

        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(userService).getAdminIds();
        verify(userService).getUserEmail(1L);
        verify(userService).getUserEmail(2L);
    }

    @Test
    @DisplayName("Membuat notifikasi untuk admin saat laporan baru dibuat - metode backward compatibility")
    public void testNotifyNewReportBackwardCompatibility() {
        // Arrange
        Report report = new Report(1L, "attendee@example.com", ReportCategory.EVENT, "Event issue");
        report.setId(UUID.randomUUID());

        List<Long> adminIds = Arrays.asList(1L, 2L);
        when(userService.getAdminIds()).thenReturn(adminIds);
        when(userService.getUserEmail(1L)).thenReturn("admin1@example.com");
        when(userService.getUserEmail(2L)).thenReturn("admin2@example.com");

        // Act - Test the backward compatibility method
        assertDoesNotThrow(() -> notificationService.notifyNewReport(report));

        // Assert - We can't verify async operations here as they run in the background
        // This test ensures the method doesn't throw exceptions
    }

    @Test
    @DisplayName("Membuat notifikasi untuk organizer saat laporan terkait event dibuat")
    public void testNotifyOrganizerOfReport() throws Exception {
        // Arrange
        Report report = new Report(1L, "attendee@example.com", ReportCategory.EVENT, "Event issue");
        report.setId(UUID.randomUUID());
        UUID eventId = UUID.randomUUID();

        List<Long> organizerIds = List.of(3L);
        when(userService.getOrganizerIds(eventId)).thenReturn(organizerIds);
        when(userService.getUserEmail(3L)).thenReturn("organizer@example.com");

        // Act
        CompletableFuture<Void> future = notificationService.notifyOrganizerOfReportAsync(report, eventId);

        // Wait for async operation to complete
        future.get(5, TimeUnit.SECONDS);

        // Assert
        verify(notificationRepository).save(any(Notification.class));
        verify(userService).getOrganizerIds(eventId);
        verify(userService).getUserEmail(3L);
    }

    @Test
    @DisplayName("Membuat notifikasi untuk organizer saat laporan terkait event dibuat - metode backward compatibility")
    public void testNotifyOrganizerOfReportBackwardCompatibility() {
        // Arrange
        Report report = new Report(1L, "attendee@example.com", ReportCategory.EVENT, "Event issue");
        report.setId(UUID.randomUUID());
        UUID eventId = UUID.randomUUID();

        List<Long> organizerIds = List.of(3L);
        when(userService.getOrganizerIds(eventId)).thenReturn(organizerIds);
        when(userService.getUserEmail(3L)).thenReturn("organizer@example.com");

        // Act - Test the backward compatibility method
        assertDoesNotThrow(() -> notificationService.notifyOrganizerOfReport(report, eventId));

        // Assert - We can't verify async operations here as they run in the background
        // This test ensures the method doesn't throw exceptions
    }

    @Test
    @DisplayName("Menangani error dalam async new report notification")
    public void testNotifyNewReportAsyncError() {
        // Arrange
        Report report = new Report(1L, "attendee@example.com", ReportCategory.EVENT, "Event issue");
        report.setId(UUID.randomUUID());

        when(userService.getAdminIds()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        CompletableFuture<Void> future = notificationService.notifyNewReportAsync(report);

        assertThrows(Exception.class, () -> future.get(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Menangani error dalam async organizer notification")
    public void testNotifyOrganizerOfReportAsyncError() {
        // Arrange
        Report report = new Report(1L, "attendee@example.com", ReportCategory.EVENT, "Event issue");
        report.setId(UUID.randomUUID());
        UUID eventId = UUID.randomUUID();

        when(userService.getOrganizerIds(eventId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        CompletableFuture<Void> future = notificationService.notifyOrganizerOfReportAsync(report, eventId);

        assertThrows(Exception.class, () -> future.get(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Mendapatkan daftar notifikasi pengguna berdasarkan ID")
    public void testGetUserNotifications() {
        // Arrange
        Long userId = 1L;
        Notification notification1 = new Notification(userId, "user@example.com", "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        Notification notification2 = new Notification(userId, "user@example.com", "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());

        List<Notification> notifications = Arrays.asList(notification1, notification2);
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getUserNotifications(userId);

        // Assert
        assertEquals(2, result.size());
        assertEquals(notifications, result);
        verify(notificationRepository).findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("Mendapatkan daftar notifikasi pengguna berdasarkan email")
    public void testGetUserNotificationsByEmail() {
        // Arrange
        String email = "user@example.com";
        Notification notification1 = new Notification(1L, email, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        Notification notification2 = new Notification(1L, email, "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());

        List<Notification> notifications = Arrays.asList(notification1, notification2);
        when(notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email)).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getUserNotificationsByEmail(email);

        // Assert
        assertEquals(2, result.size());
        assertEquals(notifications, result);
        verify(notificationRepository).findByRecipientEmailOrderByCreatedAtDesc(email);
    }

    @Test
    @DisplayName("Mendapatkan daftar notifikasi yang belum dibaca berdasarkan ID pengguna")
    public void testGetUnreadUserNotifications() {
        // Arrange
        Long userId = 1L;
        Notification notification1 = new Notification(userId, "user@example.com", "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        List<Notification> notifications = List.of(notification1);
        when(notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false)).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getUnreadUserNotifications(userId);

        // Assert
        assertEquals(1, result.size());
        assertEquals(notifications, result);
        verify(notificationRepository).findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false);
    }

    @Test
    @DisplayName("Mendapatkan daftar notifikasi yang belum dibaca berdasarkan email pengguna")
    public void testGetUnreadUserNotificationsByEmail() {
        // Arrange
        String email = "user@example.com";
        Notification notification1 = new Notification(1L, email, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        List<Notification> notifications = List.of(notification1);
        when(notificationRepository.findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false)).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getUnreadUserNotificationsByEmail(email);

        // Assert
        assertEquals(1, result.size());
        assertEquals(notifications, result);
        verify(notificationRepository).findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false);
    }

    @Test
    @DisplayName("Menghitung jumlah notifikasi yang belum dibaca berdasarkan ID pengguna")
    public void testCountUnreadNotifications() {
        // Arrange
        Long userId = 1L;
        long expectedCount = 3;
        when(notificationRepository.countByRecipientIdAndRead(userId, false)).thenReturn(expectedCount);

        // Act
        long result = notificationService.countUnreadNotifications(userId);

        // Assert
        assertEquals(expectedCount, result);
        verify(notificationRepository).countByRecipientIdAndRead(userId, false);
    }

    @Test
    @DisplayName("Menghitung jumlah notifikasi yang belum dibaca berdasarkan email pengguna")
    public void testCountUnreadNotificationsByEmail() {
        // Arrange
        String email = "user@example.com";
        long expectedCount = 3;
        when(notificationRepository.countByRecipientEmailAndRead(email, false)).thenReturn(expectedCount);

        // Act
        long result = notificationService.countUnreadNotificationsByEmail(email);

        // Assert
        assertEquals(expectedCount, result);
        verify(notificationRepository).countByRecipientEmailAndRead(email, false);
    }

    @Test
    @DisplayName("Menandai notifikasi sebagai telah dibaca")
    public void testMarkNotificationAsRead() {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        Long userId = 1L;
        Notification notification = new Notification(userId, "user@example.com", "ADMIN", "Title", "Message", "TYPE_1", UUID.randomUUID());
        notification.setId(notificationId);
        notification.setRead(false);

        when(notificationRepository.findById(notificationId)).thenReturn(java.util.Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        // Act
        Notification updatedNotification = notificationService.markNotificationAsRead(notificationId);

        // Assert
        assertTrue(updatedNotification.isRead(), "Notifikasi seharusnya ditandai sebagai telah dibaca");
        verify(notificationRepository).save(updatedNotification);
    }

    @Test
    @DisplayName("Menandai semua notifikasi sebagai telah dibaca berdasarkan ID pengguna")
    public void testMarkAllNotificationsAsRead() {
        // Arrange
        Long userId = 1L;
        Notification notification1 = new Notification(userId, "user@example.com", "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        Notification notification2 = new Notification(userId, "user@example.com", "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        notification2.setRead(false);

        List<Notification> unreadNotifications = Arrays.asList(notification1, notification2);
        when(notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false)).thenReturn(unreadNotifications);

        // Act
        notificationService.markAllNotificationsAsRead(userId);

        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class));
        assertTrue(notification1.isRead());
        assertTrue(notification2.isRead());
    }

    @Test
    @DisplayName("Menandai semua notifikasi sebagai telah dibaca berdasarkan email pengguna")
    public void testMarkAllNotificationsAsReadByEmail() {
        // Arrange
        String email = "user@example.com";
        Notification notification1 = new Notification(1L, email, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        Notification notification2 = new Notification(2L, email, "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        notification2.setRead(false);

        List<Notification> unreadNotifications = Arrays.asList(notification1, notification2);
        when(notificationRepository.findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false)).thenReturn(unreadNotifications);

        // Act
        notificationService.markAllNotificationsAsReadByEmail(email);

        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class));
        assertTrue(notification1.isRead());
        assertTrue(notification2.isRead());
    }

    @Test
    @DisplayName("Menangani error saat notifikasi yang akan ditandai tidak ditemukan")
    public void testMarkNotificationAsRead_NotificationNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(notificationRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> notificationService.markNotificationAsRead(nonExistentId)
        );

        assertEquals("Notification not found", exception.getMessage());
        verify(notificationRepository).findById(nonExistentId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Menangani error saat membuat notifikasi status change")
    public void testOnStatusChanged_WithException() {
        // Arrange
        Long userId = 1L;
        Report report = new Report(userId, "user@example.com", ReportCategory.PAYMENT, "Payment issue");
        report.setId(UUID.randomUUID());
        ReportStatus oldStatus = ReportStatus.PENDING;
        ReportStatus newStatus = ReportStatus.ON_PROGRESS;

        // Mock repository to throw exception
        when(notificationRepository.save(any(Notification.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act - this should not throw exception, but should log the error
        assertDoesNotThrow(() -> notificationService.onStatusChanged(report, oldStatus, newStatus));

        // Assert - verify that save was attempted
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Menangani error saat membuat notifikasi response added")
    public void testOnResponseAdded_WithException() {
        // Arrange
        Long userId = 1L;
        Report report = new Report(userId, "user@example.com", ReportCategory.TICKET, "Ticket issue");
        report.setId(UUID.randomUUID());
        ReportResponse response = new ReportResponse(2L, "admin@example.com", "ADMIN", "Admin response", report);

        // Mock repository to throw exception
        when(notificationRepository.save(any(Notification.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act - this should not throw exception, but should log the error
        assertDoesNotThrow(() -> notificationService.onResponseAdded(report, response));

        // Assert - verify that save was attempted
        verify(notificationRepository).save(any(Notification.class));
    }
}