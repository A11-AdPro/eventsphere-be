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
import java.util.Optional;
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
    @DisplayName("Membuat notifikasi ketika status laporan berubah")
    public void testOnStatusChanged() {
        // Arrange
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Payment issue");
        report.setId(UUID.randomUUID());

        // Act
        notificationService.onStatusChanged(report, ReportStatus.PENDING, ReportStatus.ON_PROGRESS);

        // Assert
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification notification = captor.getValue();
        assertEquals("Report Status Updated", notification.getTitle());
        assertEquals("STATUS_UPDATE", notification.getType());
        assertFalse(notification.isRead());
    }

    @Test
    @DisplayName("Memberi notifikasi ke admin dan organizer ketika attendee merespon laporan event")
    public void testOnResponseAdded_AttendeeToStaff() {
        // Arrange
        Report report = new Report(1L, "user@example.com", 10L, "Event", ReportCategory.EVENT, "Issue");
        report.setId(UUID.randomUUID());
        ReportResponse response = new ReportResponse(1L, "user@example.com", "ATTENDEE", "Response", report);

        when(userService.getAdminIds()).thenReturn(List.of(2L));
        when(userService.getUserEmail(2L)).thenReturn("admin@example.com");
        when(userService.getOrganizerIds(10L)).thenReturn(List.of(3L));
        when(userService.getUserEmail(3L)).thenReturn("organizer@example.com");

        // Act
        notificationService.onResponseAdded(report, response);

        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Memberi notifikasi ke attendee ketika staff merespon laporan")
    public void testOnResponseAdded_StaffToAttendee() {
        // Arrange
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Issue");
        report.setId(UUID.randomUUID());
        ReportResponse response = new ReportResponse(2L, "admin@example.com", "ADMIN", "Admin response", report);

        // Act
        notificationService.onResponseAdded(report, response);

        // Assert
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification notification = captor.getValue();
        assertEquals("Response to Your Report", notification.getTitle());
        assertEquals("STAFF_RESPONSE", notification.getType());
    }

    @Test
    @DisplayName("Memberi notifikasi ke organizer ketika admin merespon laporan event")
    public void testOnResponseAdded_AdminToEventOrganizers() {
        // Arrange
        Report report = new Report(1L, "user@example.com", 10L, "Event", ReportCategory.EVENT, "Issue");
        report.setId(UUID.randomUUID());
        ReportResponse response = new ReportResponse(2L, "admin@example.com", "ADMIN", "Admin response", report);

        when(userService.getOrganizerIds(10L)).thenReturn(Arrays.asList(3L, 2L)); // Include admin in list
        when(userService.getUserEmail(3L)).thenReturn("organizer@example.com");

        // Act
        notificationService.onResponseAdded(report, response);

        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Menangani exception ketika memberi notifikasi ke organizer tentang respon admin")
    public void testOnResponseAdded_AdminToEventOrganizers_WithException() {
        // Arrange
        Report report = new Report(1L, "user@example.com", 10L, "Event", ReportCategory.EVENT, "Issue");
        report.setId(UUID.randomUUID());
        ReportResponse response = new ReportResponse(2L, "admin@example.com", "ADMIN", "Admin response", report);

        when(userService.getOrganizerIds(10L)).thenReturn(List.of(3L));
        when(userService.getUserEmail(3L)).thenThrow(new RuntimeException("Failed to get organizer email"));

        // Act & Assert
        assertDoesNotThrow(() -> notificationService.onResponseAdded(report, response));

        // Assert
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Mengidentifikasi attendee berdasarkan userId ketika email berbeda")
    public void testOnResponseAdded_AttendeeByUserId() {
        // Arrange
        Report report = new Report(1L, "user@example.com", ReportCategory.EVENT, "Issue");
        report.setId(UUID.randomUUID());
        ReportResponse response = new ReportResponse(1L, "different@example.com", "ATTENDEE", "Response", report);

        when(userService.getAdminIds()).thenReturn(List.of(2L));
        when(userService.getUserEmail(2L)).thenReturn("admin@example.com");

        // Act
        notificationService.onResponseAdded(report, response);

        // Assert
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Memberi notifikasi ke organizer dengan fallback nama event ketika eventTitle null")
    public void testOnResponseAdded_AttendeeToEventOrganizers_NullTitle() {
        // Arrange - Test null eventTitle in notifyEventOrganizersOfResponse
        Report report = new Report(1L, "user@example.com", 10L, null, ReportCategory.EVENT, "Issue");
        report.setId(UUID.randomUUID());
        ReportResponse response = new ReportResponse(1L, "user@example.com", "ATTENDEE", "Response", report);

        when(userService.getAdminIds()).thenReturn(List.of(2L));
        when(userService.getUserEmail(2L)).thenReturn("admin@example.com");
        when(userService.getOrganizerIds(10L)).thenReturn(List.of(3L));
        when(userService.getUserEmail(3L)).thenReturn("organizer@example.com");

        // Act
        notificationService.onResponseAdded(report, response);

        // Assert
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        List<Notification> notifications = captor.getAllValues();
        assertTrue(notifications.stream().anyMatch(n -> n.getMessage().contains("Event #10")));
    }

    @Test
    @DisplayName("Memberi notifikasi admin ke organizer dengan fallback nama event ketika eventTitle null")
    public void testOnResponseAdded_AdminToEventOrganizers_NullTitle() {
        // Arrange - Test null eventTitle in notifyEventOrganizersOfAdminResponse
        Report report = new Report(1L, "user@example.com", 10L, null, ReportCategory.EVENT, "Issue");
        report.setId(UUID.randomUUID());
        ReportResponse response = new ReportResponse(2L, "admin@example.com", "ADMIN", "Admin response", report);

        when(userService.getOrganizerIds(10L)).thenReturn(List.of(3L));
        when(userService.getUserEmail(3L)).thenReturn("organizer@example.com");

        // Act
        notificationService.onResponseAdded(report, response);

        // Assert
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, times(2)).save(captor.capture());

        List<Notification> notifications = captor.getAllValues();
        assertTrue(notifications.stream().anyMatch(n -> n.getMessage().contains("Event #10")));
    }

    @Test
    @DisplayName("Tidak membuat notifikasi ketika respon dari role yang tidak dikenal")
    public void testOnResponseAdded_UnknownRole() {
        // Arrange
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Issue");
        report.setId(UUID.randomUUID());
        ReportResponse response = new ReportResponse(99L, "unknown@example.com", "UNKNOWN_ROLE", "Unknown response", report);

        // Act
        notificationService.onResponseAdded(report, response);

        // Assert - Should not save any notifications (neither attendee nor staff)
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(userService, never()).getAdminIds();
        verify(userService, never()).getOrganizerIds(any());
    }

    @Test
    @DisplayName("Memberi notifikasi ke attendee ketika organizer merespon laporan")
    public void testOnResponseAdded_OrganizerResponse() {
        // Arrange
        Report report = new Report(1L, "user@example.com", 10L, "Event", ReportCategory.EVENT, "Issue");
        report.setId(UUID.randomUUID());
        ReportResponse response = new ReportResponse(3L, "organizer@example.com", "ORGANIZER", "Organizer response", report);

        // Act
        notificationService.onResponseAdded(report, response);

        // Assert
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification notification = captor.getValue();
        assertEquals("ORGANIZER", notification.getSenderRole());
        assertEquals("STAFF_RESPONSE", notification.getType());
    }

    @Test
    @DisplayName("Menangani exception ketika memberi notifikasi ke organizer tentang respon attendee")
    public void testOnResponseAdded_ExceptionHandling() {
        // Arrange
        Report report = new Report(1L, "user@example.com", 10L, "Event", ReportCategory.EVENT, "Issue");
        report.setId(UUID.randomUUID());
        ReportResponse response = new ReportResponse(1L, "user@example.com", "ATTENDEE", "Response", report);

        when(userService.getAdminIds()).thenReturn(List.of(2L));
        when(userService.getUserEmail(2L)).thenReturn("admin@example.com");
        when(userService.getOrganizerIds(10L)).thenReturn(List.of(3L));
        when(userService.getUserEmail(3L)).thenThrow(new RuntimeException("User not found"));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> notificationService.onResponseAdded(report, response));
        verify(notificationRepository, times(1)).save(any(Notification.class)); // Only admin notification
    }

    @Test
    @DisplayName("Memberi notifikasi laporan baru ke admin untuk laporan umum")
    public void testNotifyNewReport_GeneralReport() {
        // Arrange
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Issue");
        report.setId(UUID.randomUUID());

        when(userService.getAdminIds()).thenReturn(List.of(2L));
        when(userService.getUserEmail(2L)).thenReturn("admin@example.com");

        // Act
        notificationService.notifyNewReport(report);

        // Assert
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification notification = captor.getValue();
        assertEquals("New Report Submitted", notification.getTitle());
        assertEquals("NEW_REPORT", notification.getType());
        assertTrue(notification.getMessage().contains("general report"));
    }

    @Test
    @DisplayName("Memberi notifikasi laporan baru ke admin dan organizer untuk laporan event")
    public void testNotifyNewReport_EventReport() {
        // Arrange
        Report report = new Report(1L, "user@example.com", 10L, "Event Title", ReportCategory.EVENT, "Issue");
        report.setId(UUID.randomUUID());

        when(userService.getAdminIds()).thenReturn(List.of(2L));
        when(userService.getUserEmail(2L)).thenReturn("admin@example.com");
        when(userService.getOrganizerIds(10L)).thenReturn(List.of(3L));
        when(userService.getUserEmail(3L)).thenReturn("organizer@example.com");

        // Act
        notificationService.notifyNewReport(report);

        // Assert
        verify(notificationRepository, times(2)).save(any(Notification.class)); // Admin + Organizer
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, atLeast(1)).save(captor.capture());

        List<Notification> notifications = captor.getAllValues();
        assertTrue(notifications.stream().anyMatch(n -> n.getMessage().contains("Event Title")));
    }

    @Test
    @DisplayName("Memberi notifikasi laporan event dengan fallback nama ketika eventTitle null")
    public void testNotifyNewReport_EventReportWithoutTitle() {
        // Arrange
        Report report = new Report(1L, "user@example.com", 10L, null, ReportCategory.EVENT, "Issue");
        report.setId(UUID.randomUUID());

        when(userService.getAdminIds()).thenReturn(List.of(2L));
        when(userService.getUserEmail(2L)).thenReturn("admin@example.com");
        when(userService.getOrganizerIds(10L)).thenReturn(List.of(3L));
        when(userService.getUserEmail(3L)).thenReturn("organizer@example.com");

        // Act
        notificationService.notifyNewReport(report);

        // Assert
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, atLeast(1)).save(captor.capture());

        List<Notification> notifications = captor.getAllValues();
        assertTrue(notifications.stream().anyMatch(n -> n.getMessage().contains("Event #10")));
    }

    @Test
    @DisplayName("Tidak memanggil organizer ketika eventId null")
    public void testNotifyNewReport_GeneralReportNullEventId() {
        // Arrange
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Issue");
        report.setId(UUID.randomUUID());

        when(userService.getAdminIds()).thenReturn(List.of(2L));
        when(userService.getUserEmail(2L)).thenReturn("admin@example.com");

        // Act
        notificationService.notifyNewReport(report);

        // Assert
        verify(notificationRepository, times(1)).save(any(Notification.class)); // Only admin
        verify(userService, never()).getOrganizerIds(any()); // Should not be called
    }

    @Test
    @DisplayName("Menangani exception ketika memberi notifikasi ke organizer tentang laporan baru")
    public void testNotifyNewReport_ExceptionHandling() {
        // Arrange
        Report report = new Report(1L, "user@example.com", 10L, "Event", ReportCategory.EVENT, "Issue");
        report.setId(UUID.randomUUID());

        when(userService.getAdminIds()).thenReturn(List.of(2L));
        when(userService.getUserEmail(2L)).thenReturn("admin@example.com");
        when(userService.getOrganizerIds(10L)).thenReturn(List.of(3L));
        when(userService.getUserEmail(3L)).thenThrow(new RuntimeException("Error"));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> notificationService.notifyNewReport(report));
        verify(notificationRepository, times(1)).save(any(Notification.class)); // Only admin
    }

    @Test
    @DisplayName("Mendapatkan notifikasi pengguna berdasarkan ID")
    public void testGetUserNotifications() {
        // Arrange
        Long userId = 1L;
        List<Notification> notifications = List.of(
                new Notification(userId, "user@example.com", "ADMIN", "Title", "Message", "TYPE", UUID.randomUUID())
        );
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getUserNotifications(userId);

        // Assert
        assertEquals(notifications, result);
        verify(notificationRepository).findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("Mendapatkan notifikasi pengguna berdasarkan email")
    public void testGetUserNotificationsByEmail() {
        // Arrange
        String email = "user@example.com";
        List<Notification> notifications = List.of(
                new Notification(1L, email, "ADMIN", "Title", "Message", "TYPE", UUID.randomUUID())
        );
        when(notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email)).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getUserNotificationsByEmail(email);

        // Assert
        assertEquals(notifications, result);
        verify(notificationRepository).findByRecipientEmailOrderByCreatedAtDesc(email);
    }

    @Test
    @DisplayName("Mendapatkan notifikasi belum dibaca pengguna berdasarkan ID")
    public void testGetUnreadUserNotifications() {
        // Arrange
        Long userId = 1L;
        List<Notification> notifications = List.of(
                new Notification(userId, "user@example.com", "ADMIN", "Title", "Message", "TYPE", UUID.randomUUID())
        );
        when(notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false)).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getUnreadUserNotifications(userId);

        // Assert
        assertEquals(notifications, result);
        verify(notificationRepository).findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false);
    }

    @Test
    @DisplayName("Mendapatkan notifikasi belum dibaca pengguna berdasarkan email")
    public void testGetUnreadUserNotificationsByEmail() {
        // Arrange
        String email = "user@example.com";
        List<Notification> notifications = List.of(
                new Notification(1L, email, "ADMIN", "Title", "Message", "TYPE", UUID.randomUUID())
        );
        when(notificationRepository.findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false)).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getUnreadUserNotificationsByEmail(email);

        // Assert
        assertEquals(notifications, result);
        verify(notificationRepository).findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false);
    }

    @Test
    @DisplayName("Menghitung jumlah notifikasi belum dibaca berdasarkan ID pengguna")
    public void testCountUnreadNotifications() {
        // Arrange
        Long userId = 1L;
        when(notificationRepository.countByRecipientIdAndRead(userId, false)).thenReturn(3L);

        // Act
        long result = notificationService.countUnreadNotifications(userId);

        // Assert
        assertEquals(3L, result);
        verify(notificationRepository).countByRecipientIdAndRead(userId, false);
    }

    @Test
    @DisplayName("Menghitung jumlah notifikasi belum dibaca berdasarkan email pengguna")
    public void testCountUnreadNotificationsByEmail() {
        // Arrange
        String email = "user@example.com";
        when(notificationRepository.countByRecipientEmailAndRead(email, false)).thenReturn(5L);

        // Act
        long result = notificationService.countUnreadNotificationsByEmail(email);

        // Assert
        assertEquals(5L, result);
        verify(notificationRepository).countByRecipientEmailAndRead(email, false);
    }

    @Test
    @DisplayName("Menandai notifikasi sebagai sudah dibaca")
    public void testMarkNotificationAsRead() {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification(1L, "user@example.com", "ADMIN", "Title", "Message", "TYPE", UUID.randomUUID());
        notification.setId(notificationId);
        notification.setRead(false);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        // Act
        Notification result = notificationService.markNotificationAsRead(notificationId);

        // Assert
        assertTrue(result.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("Melempar exception ketika menandai notifikasi tidak ditemukan sebagai sudah dibaca")
    public void testMarkNotificationAsRead_NotFound() {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> notificationService.markNotificationAsRead(notificationId)
        );

        assertEquals("Notification not found", exception.getMessage());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Menandai semua notifikasi sebagai sudah dibaca berdasarkan ID pengguna")
    public void testMarkAllNotificationsAsRead() {
        // Arrange
        Long userId = 1L;
        Notification notification1 = new Notification(userId, "user@example.com", "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        Notification notification2 = new Notification(userId, "user@example.com", "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        notification1.setRead(false);
        notification2.setRead(false);

        when(notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false))
                .thenReturn(Arrays.asList(notification1, notification2));

        // Act
        notificationService.markAllNotificationsAsRead(userId);

        // Assert
        assertTrue(notification1.isRead());
        assertTrue(notification2.isRead());
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Menandai semua notifikasi sebagai sudah dibaca berdasarkan email pengguna")
    public void testMarkAllNotificationsAsReadByEmail() {
        // Arrange
        String email = "user@example.com";
        Notification notification = new Notification(1L, email, "ADMIN", "Title", "Message", "TYPE", UUID.randomUUID());
        notification.setRead(false);

        when(notificationRepository.findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false))
                .thenReturn(List.of(notification));

        // Act
        notificationService.markAllNotificationsAsReadByEmail(email);

        // Assert
        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }
}