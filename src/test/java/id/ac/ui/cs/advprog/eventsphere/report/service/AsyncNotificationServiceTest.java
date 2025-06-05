package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.model.Notification;
import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AsyncNotificationService asyncNotificationService;

    private Report testReport;
    private ReportResponse testResponse;

    @BeforeEach
    void setUp() {
        testReport = new Report();
        testReport.setId(UUID.randomUUID());
        testReport.setUserId(1L);
        testReport.setUserEmail("user@test.com");
        testReport.setEventId(1L);
        testReport.setEventTitle("Test Event");
        testReport.setCategory(ReportCategory.PAYMENT);
        testReport.setDescription("Test description");
        testReport.setStatus(ReportStatus.PENDING);

        testResponse = new ReportResponse();
        testResponse.setResponderId(2L);
        testResponse.setResponderEmail("responder@test.com");
        testResponse.setResponderRole("ADMIN");
        testResponse.setMessage("Test response");
        testResponse.setReport(testReport);
    }

    @Test
    @DisplayName("Berhasil memproses notifikasi laporan baru untuk admin dan organizer")
    void processNewReportNotificationsAsync_Success() throws Exception {
        // Arrange
        when(userService.getAdminIds()).thenReturn(Arrays.asList(1L, 2L));
        when(userService.getOrganizerIds(anyLong())).thenReturn(List.of(3L));
        when(userService.getUserEmail(anyLong())).thenReturn("admin@test.com");
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        CompletableFuture<String> result = asyncNotificationService.processNewReportNotificationsAsync(testReport);

        // Assert
        assertTrue(result.get().contains("Successfully processed"));
        verify(userService).getAdminIds();
        verify(userService).getOrganizerIds(1L);
        verify(notificationRepository, times(3)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Menangani exception saat memproses notifikasi laporan baru")
    void processNewReportNotificationsAsync_WithException() throws Exception {
        // Arrange
        when(userService.getAdminIds()).thenThrow(new RuntimeException("Database error"));

        // Act
        CompletableFuture<String> result = asyncNotificationService.processNewReportNotificationsAsync(testReport);

        // Assert
        assertTrue(result.get().contains("Error processing notifications"));
    }

    @Test
    @DisplayName("Memproses notifikasi hanya untuk admin ketika laporan tidak memiliki event ID")
    void processNewReportNotificationsAsync_NoEventId() throws Exception {
        // Arrange
        testReport.setEventId(null);
        when(userService.getAdminIds()).thenReturn(List.of(1L));
        when(userService.getUserEmail(anyLong())).thenReturn("admin@test.com");
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        CompletableFuture<String> result = asyncNotificationService.processNewReportNotificationsAsync(testReport);

        // Assert
        assertTrue(result.get().contains("Successfully processed"));
        verify(userService, never()).getOrganizerIds(anyLong());
    }

    @Test
    @DisplayName("Berhasil mengirimkan notifikasi perubahan status kepada pembuat laporan")
    void processStatusChangeNotificationsAsync_Success() throws Exception {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        CompletableFuture<String> result = asyncNotificationService.processStatusChangeNotificationsAsync(
                testReport, ReportStatus.PENDING, ReportStatus.ON_PROGRESS);

        // Assert
        assertTrue(result.get().contains("Successfully sent status update"));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Menangani nilai null pada status dalam notifikasi perubahan status")
    void processStatusChangeNotificationsAsync_WithNullStatus() throws Exception {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        CompletableFuture<String> result = asyncNotificationService.processStatusChangeNotificationsAsync(
                testReport, null, null);

        // Assert
        assertTrue(result.get().contains("Successfully sent status update"));
    }

    @Test
    @DisplayName("Menangani exception selama pemrosesan notifikasi perubahan status")
    void processStatusChangeNotificationsAsync_WithException() throws Exception {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenThrow(new RuntimeException("Save error"));

        // Act
        CompletableFuture<String> result = asyncNotificationService.processStatusChangeNotificationsAsync(
                testReport, ReportStatus.PENDING, ReportStatus.ON_PROGRESS);

        // Assert
        assertTrue(result.get().contains("Error processing status change"));
    }

    @Test
    @DisplayName("Memberi pemberitahuan kepada staf ketika tanggapan berasal dari peserta")
    void processResponseNotificationsAsync_FromAttendee() throws Exception {
        // Arrange - tanggapan dari pengguna yang sama dengan pembuat laporan
        testResponse.setResponderEmail("user@test.com");
        testResponse.setResponderId(1L);

        when(userService.getAdminIds()).thenReturn(List.of(2L));
        when(userService.getOrganizerIds(anyLong())).thenReturn(List.of(3L));
        when(userService.getUserEmail(anyLong())).thenReturn("admin@test.com");
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        CompletableFuture<String> result = asyncNotificationService.processResponseNotificationsAsync(testReport, testResponse);

        // Assert
        assertTrue(result.get().contains("Successfully processed response"));
        verify(userService).getAdminIds();
        verify(userService).getOrganizerIds(1L);
    }

    @Test
    @DisplayName("Memberi pemberitahuan kepada peserta ketika tanggapan berasal dari staf")
    void processResponseNotificationsAsync_FromStaff() throws Exception {
        // Arrange - tanggapan dari pengguna lain (staf)
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        CompletableFuture<String> result = asyncNotificationService.processResponseNotificationsAsync(testReport, testResponse);

        // Assert
        assertTrue(result.get().contains("Successfully processed response"));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Menangani exception di UserService saat membuat notifikasi")
    void createAndSaveNotification_WithUserServiceException() throws Exception {
        // Arrange
        when(userService.getAdminIds()).thenReturn(List.of(1L));
        when(userService.getUserEmail(1L)).thenThrow(new RuntimeException("User not found"));

        // Act
        CompletableFuture<String> result = asyncNotificationService.processNewReportNotificationsAsync(testReport);

        // Assert
        assertTrue(result.get().contains("Successfully processed"));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Memberi pemberitahuan hanya kepada admin ketika laporan tidak memiliki event ID selama pemrosesan tanggapan")
    void notifyStaffAsync_NoEventId() throws Exception {
        // Arrange
        testReport.setEventId(null);
        testResponse.setResponderEmail("user@test.com");

        when(userService.getAdminIds()).thenReturn(List.of(2L));
        when(userService.getUserEmail(anyLong())).thenReturn("admin@test.com");
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        CompletableFuture<String> result = asyncNotificationService.processResponseNotificationsAsync(testReport, testResponse);

        // Assert
        assertTrue(result.get().contains("Successfully processed"));
        verify(userService, never()).getOrganizerIds(anyLong());
    }

    @Test
    @DisplayName("Menangani nilai null dalam pemberitahuan peserta")
    void notifyAttendeeAsync_WithNullValues() throws Exception {
        // Arrange
        testResponse.setResponderRole(null);
        testResponse.setMessage(null);
        testReport.setCategory(null);
        testReport.setStatus(null);

        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        CompletableFuture<String> result = asyncNotificationService.processResponseNotificationsAsync(testReport, testResponse);

        // Assert
        assertTrue(result.get().contains("Successfully processed"));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Menangani exception selama pembuatan notifikasi tanggapan")
    void createResponseNotification_WithException() throws Exception {
        // Arrange
        testResponse.setResponderEmail("user@test.com");
        when(userService.getAdminIds()).thenReturn(List.of(2L));
        when(userService.getUserEmail(2L)).thenThrow(new RuntimeException("User not found"));

        // Act
        CompletableFuture<String> result = asyncNotificationService.processResponseNotificationsAsync(testReport, testResponse);

        // Assert
        assertTrue(result.get().contains("Successfully processed"));
    }

    @Test
    @DisplayName("Menangani nilai null pada field laporan selama pembuatan notifikasi")
    void createAndSaveNotification_WithNullValues() throws Exception {
        // Arrange
        testReport.setEventTitle(null);
        testReport.setCategory(null);
        testReport.setDescription(null);

        when(userService.getAdminIds()).thenReturn(List.of(1L));
        when(userService.getUserEmail(1L)).thenReturn("admin@test.com");
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        CompletableFuture<String> result = asyncNotificationService.processNewReportNotificationsAsync(testReport);

        // Assert
        assertTrue(result.get().contains("Successfully processed"));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Test isFromAttendee dengan userId dan userEmail null")
    void processResponseNotificationsAsync_WithNullUserIdAndEmail() throws Exception {
        // Arrange - test null checking dalam isFromAttendee
        testReport.setUserId(null);
        testReport.setUserEmail(null);
        testResponse.setResponderEmail("staff@test.com");
        testResponse.setResponderId(2L);

        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        CompletableFuture<String> result = asyncNotificationService.processResponseNotificationsAsync(testReport, testResponse);

        // Assert
        assertTrue(result.get().contains("Successfully processed"));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Test isFromAttendee dengan userId matching tapi userEmail null")
    void processResponseNotificationsAsync_UserIdMatches_EmailNull() throws Exception {
        // Arrange
        testReport.setUserId(2L);
        testReport.setUserEmail(null);
        testResponse.setResponderId(2L);
        testResponse.setResponderEmail("different@test.com");

        when(userService.getAdminIds()).thenReturn(List.of(3L));
        when(userService.getOrganizerIds(anyLong())).thenReturn(List.of(4L));
        when(userService.getUserEmail(anyLong())).thenReturn("admin@test.com");
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        CompletableFuture<String> result = asyncNotificationService.processResponseNotificationsAsync(testReport, testResponse);

        // Assert
        assertTrue(result.get().contains("Successfully processed"));
        verify(userService).getAdminIds();
    }

    @Test
    @DisplayName("Test isFromAttendee dengan userEmail matching tapi userId null")
    void processResponseNotificationsAsync_EmailMatches_UserIdNull() throws Exception {
        // Arrange
        testReport.setUserId(null);
        testReport.setUserEmail("user@test.com");
        testResponse.setResponderId(2L);
        testResponse.setResponderEmail("user@test.com");

        when(userService.getAdminIds()).thenReturn(List.of(3L));
        when(userService.getOrganizerIds(anyLong())).thenReturn(List.of(4L));
        when(userService.getUserEmail(anyLong())).thenReturn("admin@test.com");
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        CompletableFuture<String> result = asyncNotificationService.processResponseNotificationsAsync(testReport, testResponse);

        // Assert
        assertTrue(result.get().contains("Successfully processed"));
        verify(userService).getAdminIds();
    }

    @Test
    @DisplayName("Test exception handling dalam processResponseNotificationsAsync")
    void processResponseNotificationsAsync_WithException() throws Exception {
        // Arrange
        testResponse.setResponderEmail("user@test.com");
        when(userService.getAdminIds()).thenThrow(new RuntimeException("Service error"));

        // Act
        CompletableFuture<String> result = asyncNotificationService.processResponseNotificationsAsync(testReport, testResponse);

        // Assert
        assertTrue(result.get().contains("Error processing response"));
    }

    @Test
    @DisplayName("Test exception handling dalam notifyAttendeeAsync")
    void notifyAttendeeAsync_WithSaveException() throws Exception {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenThrow(new RuntimeException("Save failed"));

        // Act
        CompletableFuture<String> result = asyncNotificationService.processResponseNotificationsAsync(testReport, testResponse);

        // Assert
        assertTrue(result.get().contains("Successfully processed")); // Masih berhasil karena exception di-catch
    }

    @Test
    @DisplayName("Test createResponseNotification dengan null values di response")
    void createResponseNotification_WithNullResponseValues() throws Exception {
        // Arrange
        testResponse.setResponderEmail("user@test.com");
        testResponse.setResponderRole(null);
        testResponse.setMessage(null);
        testReport.setCategory(null);
        testReport.setStatus(null);

        when(userService.getAdminIds()).thenReturn(List.of(2L));
        when(userService.getUserEmail(2L)).thenReturn("admin@test.com");
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        CompletableFuture<String> result = asyncNotificationService.processResponseNotificationsAsync(testReport, testResponse);

        // Assert
        assertTrue(result.get().contains("Successfully processed"));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Test createResponseNotification dengan getUserEmail exception")
    void createResponseNotification_WithGetUserEmailException() throws Exception {
        // Arrange
        testResponse.setResponderEmail("user@test.com");
        when(userService.getAdminIds()).thenReturn(List.of(2L));
        when(userService.getUserEmail(2L)).thenThrow(new RuntimeException("User not found"));

        // Act
        CompletableFuture<String> result = asyncNotificationService.processResponseNotificationsAsync(testReport, testResponse);

        // Assert
        assertTrue(result.get().contains("Successfully processed"));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Test processStatusChangeNotificationsAsync dengan semua null values")
    void processStatusChangeNotificationsAsync_AllNullValues() throws Exception {
        // Arrange
        testReport.setCategory(null);
        testReport.setDescription(null);
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        CompletableFuture<String> result = asyncNotificationService.processStatusChangeNotificationsAsync(
                testReport, null, null);

        // Assert
        assertTrue(result.get().contains("Successfully sent status update"));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Test createAndSaveNotification dengan semua null report values")
    void createAndSaveNotification_AllNullReportValues() throws Exception {
        // Arrange
        Report nullReport = new Report();
        nullReport.setId(UUID.randomUUID());
        nullReport.setUserId(1L);
        nullReport.setUserEmail("user@test.com");
        nullReport.setEventTitle(null);
        nullReport.setCategory(null);
        nullReport.setDescription(null);

        when(userService.getAdminIds()).thenReturn(List.of(1L));
        when(userService.getUserEmail(1L)).thenReturn("admin@test.com");
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        CompletableFuture<String> result = asyncNotificationService.processNewReportNotificationsAsync(nullReport);

        // Assert
        assertTrue(result.get().contains("Successfully processed"));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Test dengan responder role null dalam createResponseNotification")
    void createResponseNotification_ResponderRoleNull() throws Exception {
        // Arrange
        testResponse.setResponderEmail("user@test.com");
        testResponse.setResponderRole(null);

        when(userService.getAdminIds()).thenReturn(List.of(2L));
        when(userService.getUserEmail(2L)).thenReturn("admin@test.com");
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        CompletableFuture<String> result = asyncNotificationService.processResponseNotificationsAsync(testReport, testResponse);

        // Assert
        assertTrue(result.get().contains("Successfully processed"));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("Test notifyAttendeeAsync dengan responder role null")
    void notifyAttendeeAsync_ResponderRoleNull() throws Exception {
        // Arrange
        testResponse.setResponderRole(null);
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        // Act
        CompletableFuture<String> result = asyncNotificationService.processResponseNotificationsAsync(testReport, testResponse);

        // Assert
        assertTrue(result.get().contains("Successfully processed"));
        verify(notificationRepository).save(any(Notification.class));
    }
}