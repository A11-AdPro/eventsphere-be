package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.event.service.EventService;
import id.ac.ui.cs.advprog.eventsphere.report.dto.request.CreateReportCommentRequest;
import id.ac.ui.cs.advprog.eventsphere.report.dto.request.CreateReportRequest;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportCommentDTO;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportSummaryDTO;
import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.repository.ReportRepository;
import id.ac.ui.cs.advprog.eventsphere.report.repository.ReportResponseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReportServiceTest {

    private ReportRepository reportRepository;
    private ReportResponseRepository responseRepository;
    private NotificationService notificationService;
    private UserRepository userRepository;
    private EventService eventService;
    private ReportService reportService;

    @BeforeEach
    public void setUp() {
        reportRepository = mock(ReportRepository.class);
        responseRepository = mock(ReportResponseRepository.class);
        notificationService = mock(NotificationService.class);
        userRepository = mock(UserRepository.class);
        eventService = mock(EventService.class);

        reportService = new ReportService(
                reportRepository,
                responseRepository,
                notificationService,
                userRepository,
                eventService
        );
    }

    @Test
    @DisplayName("Membuat laporan baru dengan email yang sudah disediakan")
    public void testCreateReport_WithEmail() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setUserId(1L);
        request.setUserEmail("user@example.com");
        request.setCategory(ReportCategory.PAYMENT);
        request.setDescription("Test description");

        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(UUID.randomUUID());
            return report;
        });

        // Act
        ReportResponseDTO result = reportService.createReport(request);

        // Assert
        assertEquals("user@example.com", result.getUserEmail());
        assertEquals(ReportStatus.PENDING, result.getStatus());
        verify(notificationService).notifyNewReport(any(Report.class));
    }

    @Test
    @DisplayName("Membuat laporan baru dengan email yang dicari dari database")
    public void testCreateReport_WithoutEmail() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setUserId(1L);
        request.setUserEmail(null); // Test null email path
        request.setCategory(ReportCategory.PAYMENT);
        request.setDescription("Test description");

        User user = new User();
        user.setEmail("found@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(UUID.randomUUID());
            return report;
        });

        // Act
        ReportResponseDTO result = reportService.createReport(request);

        // Assert
        assertEquals("found@example.com", result.getUserEmail());
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Melempar exception ketika pengguna tidak ditemukan saat membuat laporan")
    public void testCreateReport_UserNotFound() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setUserId(1L);
        request.setUserEmail(""); // Empty email to trigger user lookup

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> reportService.createReport(request)
        );
        assertEquals("User email not found for userId: 1", exception.getMessage());
    }

    @Test
    @DisplayName("Membuat laporan untuk event dengan notifikasi organizer")
    public void testCreateReport_WithEvent() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setUserId(1L);
        request.setUserEmail("user@example.com");
        request.setEventId(10L);
        request.setEventTitle("Event");
        request.setCategory(ReportCategory.EVENT);
        request.setDescription("Event issue");

        EventResponseDTO event = new EventResponseDTO();
        event.setOrganizerId(5L);

        User organizer = new User();
        organizer.setEmail("organizer@example.com");

        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(UUID.randomUUID());
            return report;
        });
        when(eventService.getActiveEventById(10L)).thenReturn(event);
        when(userRepository.findById(5L)).thenReturn(Optional.of(organizer));

        // Act
        ReportResponseDTO result = reportService.createReport(request);

        // Assert
        assertEquals(10L, result.getEventId());
        verify(eventService).getActiveEventById(10L);
        verify(userRepository).findById(5L);
    }

    @Test
    @DisplayName("Menangani exception saat mencari event ketika membuat laporan")
    public void testCreateReport_EventException() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setUserId(1L);
        request.setUserEmail("user@example.com");
        request.setEventId(10L);
        request.setCategory(ReportCategory.EVENT);
        request.setDescription("Event issue");

        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(UUID.randomUUID());
            return report;
        });
        when(eventService.getActiveEventById(10L)).thenThrow(new RuntimeException("Event error"));

        // Act & Assert
        assertDoesNotThrow(() -> reportService.createReport(request));
    }

    @Test
    @DisplayName("Menangani ketika organizer tidak ditemukan saat notifikasi event")
    public void testNotifyEventOrganizer_OrganizerNotFound() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setUserId(1L);
        request.setUserEmail("user@example.com");
        request.setEventId(10L);
        request.setCategory(ReportCategory.EVENT);
        request.setDescription("Event issue");

        EventResponseDTO event = new EventResponseDTO();
        event.setOrganizerId(5L);

        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(UUID.randomUUID());
            return report;
        });
        when(eventService.getActiveEventById(10L)).thenReturn(event);
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        // Act & Assert
        assertDoesNotThrow(() -> reportService.createReport(request));
    }

    @Test
    @DisplayName("Menangani exception saat mencari organizer untuk notifikasi")
    public void testNotifyEventOrganizer_Exception() {
        // Arrange
        CreateReportRequest request = new CreateReportRequest();
        request.setUserId(1L);
        request.setUserEmail("user@example.com");
        request.setEventId(10L);
        request.setCategory(ReportCategory.EVENT);
        request.setDescription("Event issue");

        EventResponseDTO event = new EventResponseDTO();
        event.setOrganizerId(5L);

        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(UUID.randomUUID());
            return report;
        });
        when(eventService.getActiveEventById(10L)).thenReturn(event);
        when(userRepository.findById(5L)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> reportService.createReport(request));
    }

    @Test
    @DisplayName("Memverifikasi laporan milik organizer event berhasil")
    public void testIsReportFromOrganizerEvent_True() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setEventId(10L);

        EventResponseDTO event = new EventResponseDTO();
        event.setOrganizerId(5L);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(eventService.getActiveEventById(10L)).thenReturn(event);

        // Act
        boolean result = reportService.isReportFromOrganizerEvent(reportId, 5L);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Memverifikasi laporan bukan milik organizer ketika eventId null")
    public void testIsReportFromOrganizerEvent_False_NullEventId() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setEventId(null);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // Act
        boolean result = reportService.isReportFromOrganizerEvent(reportId, 5L);

        // Assert
        assertFalse(result);
        verify(eventService, never()).getActiveEventById(any());
    }

    @Test
    @DisplayName("Memverifikasi laporan bukan milik organizer ketika terjadi exception")
    public void testIsReportFromOrganizerEvent_False_Exception() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setEventId(10L);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(eventService.getActiveEventById(10L)).thenThrow(new RuntimeException("Event error"));

        // Act
        boolean result = reportService.isReportFromOrganizerEvent(reportId, 5L);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Mendapatkan laporan organizer berdasarkan event dan status")
    public void testGetReportsByOrganizerEventsAndStatus_WithStatus() {
        // Arrange
        Long organizerId = 5L;
        User organizer = new User();
        EventResponseDTO event = new EventResponseDTO();
        event.setId(10L);

        Report report = new Report();
        report.setId(UUID.randomUUID());
        report.setStatus(ReportStatus.PENDING);

        when(userRepository.findById(organizerId)).thenReturn(Optional.of(organizer));
        when(eventService.getActiveEventsByOrganizer(organizer)).thenReturn(List.of(event));
        when(reportRepository.findByEventIdAndStatus(10L, ReportStatus.PENDING))
                .thenReturn(List.of(report));

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByOrganizerEventsAndStatus(organizerId, ReportStatus.PENDING);

        // Assert
        assertEquals(1, result.size());
        verify(reportRepository).findByEventIdAndStatus(10L, ReportStatus.PENDING);
    }

    @Test
    @DisplayName("Mendapatkan laporan organizer berdasarkan event tanpa filter status")
    public void testGetReportsByOrganizerEventsAndStatus_WithoutStatus() {
        // Arrange
        Long organizerId = 5L;
        User organizer = new User();
        EventResponseDTO event = new EventResponseDTO();
        event.setId(10L);

        Report report = new Report();
        report.setId(UUID.randomUUID());

        when(userRepository.findById(organizerId)).thenReturn(Optional.of(organizer));
        when(eventService.getActiveEventsByOrganizer(organizer)).thenReturn(List.of(event));
        when(reportRepository.findByEventId(10L)).thenReturn(List.of(report));

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByOrganizerEventsAndStatus(organizerId, null);

        // Assert
        assertEquals(1, result.size());
        verify(reportRepository).findByEventId(10L);
    }

    @Test
    @DisplayName("Melempar exception ketika organizer tidak ditemukan")
    public void testGetReportsByOrganizerEventsAndStatus_OrganizerNotFound() {
        // Arrange
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> reportService.getReportsByOrganizerEventsAndStatus(5L, null)
        );
        assertEquals("Organizer not found", exception.getMessage());
    }

    @Test
    @DisplayName("Mendapatkan laporan berdasarkan ID event")
    public void testGetReportsByEventId() {
        // Arrange
        Report report = new Report();
        report.setId(UUID.randomUUID());

        when(reportRepository.findByEventId(10L)).thenReturn(List.of(report));

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByEventId(10L);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Mendapatkan laporan berdasarkan ID")
    public void testGetReportById() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setId(reportId);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // Act
        ReportResponseDTO result = reportService.getReportById(reportId);

        // Assert
        assertEquals(reportId, result.getId());
    }

    @Test
    @DisplayName("Melempar exception ketika laporan tidak ditemukan berdasarkan ID")
    public void testGetReportById_NotFound() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> reportService.getReportById(reportId)
        );
        assertEquals("Report not found with id: " + reportId, exception.getMessage());
    }

    @Test
    @DisplayName("Mendapatkan laporan berdasarkan ID pengguna")
    public void testGetReportsByUserId() {
        // Arrange
        Report report = new Report();
        when(reportRepository.findByUserId(1L)).thenReturn(List.of(report));

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByUserId(1L);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Mendapatkan laporan berdasarkan email pengguna")
    public void testGetReportsByUserEmail() {
        // Arrange
        Report report = new Report();
        when(reportRepository.findByUserEmail("user@example.com")).thenReturn(List.of(report));

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByUserEmail("user@example.com");

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Mendapatkan laporan berdasarkan status tertentu")
    public void testGetReportsByStatus_WithStatus() {
        // Arrange
        Report report = new Report();
        when(reportRepository.findByStatus(ReportStatus.PENDING)).thenReturn(List.of(report));

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByStatus(ReportStatus.PENDING);

        // Assert
        assertEquals(1, result.size());
        verify(reportRepository).findByStatus(ReportStatus.PENDING);
    }

    @Test
    @DisplayName("Mendapatkan semua laporan tanpa filter status")
    public void testGetReportsByStatus_WithoutStatus() {
        // Arrange
        Report report = new Report();
        when(reportRepository.findAll()).thenReturn(List.of(report));

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByStatus(null);

        // Assert
        assertEquals(1, result.size());
        verify(reportRepository).findAll();
    }

    @Test
    @DisplayName("Memperbarui status laporan")
    public void testUpdateReportStatus() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setId(reportId);
        report.setStatus(ReportStatus.PENDING);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(reportRepository.save(report)).thenReturn(report);

        // Act
        ReportResponseDTO result = reportService.updateReportStatus(reportId, ReportStatus.ON_PROGRESS);

        // Assert
        assertEquals(ReportStatus.ON_PROGRESS, result.getStatus());
        assertNotNull(result.getUpdatedAt());
        verify(reportRepository).save(report);
    }

    @Test
    @DisplayName("Memperbarui status laporan dengan observer yang sudah ada")
    public void testUpdateReportStatus_ObserverAlreadyAdded() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setId(reportId);
        report.getObservers().add(notificationService);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(reportRepository.save(report)).thenReturn(report);

        // Act
        reportService.updateReportStatus(reportId, ReportStatus.ON_PROGRESS);

        // Assert
        assertEquals(1, report.getObservers().size()); // Should not add duplicate
    }

    @Test
    @DisplayName("Menambah komentar dari pemilik laporan berdasarkan email")
    public void testAddComment_ReportOwnerByEmail() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setId(reportId);
        report.setUserEmail("user@example.com");
        report.setStatus(ReportStatus.PENDING);

        CreateReportCommentRequest request = new CreateReportCommentRequest();
        request.setResponderEmail("user@example.com"); // Same as report owner
        request.setMessage("Comment");

        ReportResponse savedComment = new ReportResponse();
        savedComment.setId(UUID.randomUUID());

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(responseRepository.save(any(ReportResponse.class))).thenReturn(savedComment);
        when(reportRepository.save(report)).thenReturn(report);

        // Act
        ReportCommentDTO result = reportService.addComment(reportId, request);

        // Assert
        assertNotNull(result);
        assertEquals(ReportStatus.PENDING, report.getStatus()); // Should remain pending for owner
    }

    @Test
    @DisplayName("Menambah komentar dari pemilik laporan berdasarkan user ID")
    public void testAddComment_ReportOwnerByUserId() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setId(reportId);
        report.setUserId(1L);
        report.setUserEmail("user@example.com");
        report.setStatus(ReportStatus.PENDING);

        CreateReportCommentRequest request = new CreateReportCommentRequest();
        request.setResponderId(1L); // Same as report owner
        request.setResponderEmail("different@example.com");
        request.setMessage("Comment");

        ReportResponse savedComment = new ReportResponse();
        savedComment.setId(UUID.randomUUID());

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(responseRepository.save(any(ReportResponse.class))).thenReturn(savedComment);
        when(reportRepository.save(report)).thenReturn(report);

        // Act
        ReportCommentDTO result = reportService.addComment(reportId, request);

        // Assert
        assertNotNull(result);
        assertEquals(ReportStatus.PENDING, report.getStatus()); // Should remain pending for owner
    }

    @Test
    @DisplayName("Menambah komentar dari non-owner mengubah status ke ON_PROGRESS")
    public void testAddComment_NonOwner_StatusChange() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setId(reportId);
        report.setUserId(1L);
        report.setUserEmail("user@example.com");
        report.setStatus(ReportStatus.PENDING);

        CreateReportCommentRequest request = new CreateReportCommentRequest();
        request.setResponderId(2L); // Different from report owner
        request.setResponderEmail("admin@example.com");
        request.setMessage("Admin comment");

        ReportResponse savedComment = new ReportResponse();
        savedComment.setId(UUID.randomUUID());

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(responseRepository.save(any(ReportResponse.class))).thenReturn(savedComment);
        when(reportRepository.save(report)).thenReturn(report);

        // Act
        reportService.addComment(reportId, request);

        // Assert
        assertEquals(ReportStatus.ON_PROGRESS, report.getStatus()); // Should change to ON_PROGRESS
    }

    @Test
    @DisplayName("Menambah komentar dari non-owner ketika status bukan PENDING")
    public void testAddComment_NonOwner_StatusNotPending() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setId(reportId);
        report.setUserId(1L);
        report.setUserEmail("user@example.com");
        report.setStatus(ReportStatus.ON_PROGRESS); // Not PENDING

        CreateReportCommentRequest request = new CreateReportCommentRequest();
        request.setResponderId(2L); // Different from report owner
        request.setResponderEmail("admin@example.com");
        request.setMessage("Admin comment");

        ReportResponse savedComment = new ReportResponse();
        savedComment.setId(UUID.randomUUID());

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(responseRepository.save(any(ReportResponse.class))).thenReturn(savedComment);
        when(reportRepository.save(report)).thenReturn(report);

        // Act
        reportService.addComment(reportId, request);

        // Assert
        assertEquals(ReportStatus.ON_PROGRESS, report.getStatus()); // Should remain ON_PROGRESS
    }

    @Test
    @DisplayName("Menambah komentar dengan observer yang sudah ada")
    public void testAddComment_ObserverAlreadyAdded() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setId(reportId);
        report.setUserId(1L);
        report.setUserEmail("user@example.com");
        report.setStatus(ReportStatus.PENDING);
        report.getObservers().add(notificationService); // Already added

        CreateReportCommentRequest request = new CreateReportCommentRequest();
        request.setResponderId(2L);
        request.setResponderEmail("admin@example.com");
        request.setResponderRole("ADMIN");
        request.setMessage("Comment");

        ReportResponse savedComment = new ReportResponse();
        savedComment.setId(UUID.randomUUID());

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(responseRepository.save(any(ReportResponse.class))).thenReturn(savedComment);
        when(reportRepository.save(report)).thenReturn(report);

        // Act
        reportService.addComment(reportId, request);

        // Assert
        assertEquals(1, report.getObservers().size()); // Should not add duplicate
    }

    @Test
    @DisplayName("Menghapus laporan")
    public void testDeleteReport() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setId(reportId);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // Act
        reportService.deleteReport(reportId);

        // Assert
        verify(reportRepository).delete(report);
    }

    @Test
    @DisplayName("Mengonversi laporan ke ResponseDTO dengan responses null")
    public void testConvertToResponseDTO_WithNullResponses() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setId(reportId);
        report.setResponses(null);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // Act
        ReportResponseDTO result = reportService.getReportById(reportId);

        // Assert
        assertNotNull(result.getComments());
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    @DisplayName("Mengonversi komentar ke DTO dengan report null")
    public void testConvertToCommentDTO_WithNullReport() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setId(reportId);

        ReportResponse comment = new ReportResponse();
        comment.setId(UUID.randomUUID());
        comment.setReport(null); // Null report

        report.setResponses(List.of(comment));

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // Act
        ReportResponseDTO result = reportService.getReportById(reportId);

        // Assert
        assertNull(result.getComments().getFirst().getReportId());
    }

    @Test
    @DisplayName("Mengonversi komentar ke DTO dengan report yang valid")
    public void testConvertToCommentDTO_WithValidReport() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        Report report = new Report();
        report.setId(reportId);

        ReportResponse comment = new ReportResponse();
        comment.setId(commentId);
        comment.setReport(report); // Valid report - should return report.getId()

        report.setResponses(List.of(comment));

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // Act
        ReportResponseDTO result = reportService.getReportById(reportId);

        // Assert
        assertEquals(reportId, result.getComments().getFirst().getReportId());
    }

    @Test
    @DisplayName("Mengonversi laporan ke SummaryDTO dengan deskripsi panjang")
    public void testConvertToSummaryDTO_LongDescription() {
        // Arrange
        String longDescription = "This is a very long description that should be truncated because it exceeds fifty characters";
        Report report = new Report();
        report.setId(UUID.randomUUID());
        report.setDescription(longDescription);

        when(reportRepository.findByUserId(1L)).thenReturn(List.of(report));

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByUserId(1L);

        // Assert
        // The logic truncates to first 47 characters + "..."
        String expected = longDescription.substring(0, 47) + "...";
        assertEquals(expected, result.getFirst().getShortDescription());
    }

    @Test
    @DisplayName("Mengonversi laporan ke SummaryDTO dengan deskripsi pendek")
    public void testConvertToSummaryDTO_ShortDescription() {
        // Arrange
        Report report = new Report();
        report.setId(UUID.randomUUID());
        report.setDescription("Short");

        when(reportRepository.findByUserId(1L)).thenReturn(List.of(report));

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByUserId(1L);

        // Assert
        assertEquals("Short", result.getFirst().getShortDescription());
    }

    @Test
    @DisplayName("Mengonversi laporan ke SummaryDTO dengan deskripsi null")
    public void testConvertToSummaryDTO_NullDescription() {
        // Arrange
        Report report = new Report();
        report.setId(UUID.randomUUID());
        report.setDescription(null);

        when(reportRepository.findByUserId(1L)).thenReturn(List.of(report));

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByUserId(1L);

        // Assert
        assertNull(result.getFirst().getShortDescription());
    }

    @Test
    @DisplayName("Mengonversi laporan ke SummaryDTO dengan responses null")
    public void testConvertToSummaryDTO_NullResponses() {
        // Arrange
        Report report = new Report();
        report.setId(UUID.randomUUID());
        report.setResponses(null);

        when(reportRepository.findByUserId(1L)).thenReturn(List.of(report));

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByUserId(1L);

        // Assert
        assertEquals(0, result.getFirst().getCommentCount());
    }
}