package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ReportServiceTest {

    private ReportRepository reportRepository;
    private ReportResponseRepository responseRepository;
    private NotificationService notificationService;
    private UserRepository userRepository;
    private ReportService reportService;

    @BeforeEach
    public void setUp() {
        reportRepository = mock(ReportRepository.class);
        responseRepository = mock(ReportResponseRepository.class);
        notificationService = mock(NotificationService.class);
        userRepository = mock(UserRepository.class);

        reportService = new ReportService(
                reportRepository,
                responseRepository,
                notificationService,
                userRepository
        );
    }

    @Test
    @DisplayName("Membuat laporan baru dengan data permintaan yang valid")
    public void testCreateReport() {
        // Arrange
        Long userId = 1L;
        String userEmail = "user@example.com";

        CreateReportRequest createRequest = new CreateReportRequest();
        createRequest.setUserId(userId);
        createRequest.setUserEmail(userEmail);
        createRequest.setCategory(ReportCategory.PAYMENT);
        createRequest.setDescription("Test description");

        User mockUser = new User();
        mockUser.setEmail(userEmail);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(UUID.randomUUID());
            return report;
        });

        // Act
        ReportResponseDTO result = reportService.createReport(createRequest);

        // Assert
        verify(reportRepository).save(any(Report.class));
        verify(notificationService).notifyNewReport(any(Report.class));

        assertNotNull(result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(userEmail, result.getUserEmail());
        assertEquals(ReportCategory.PAYMENT, result.getCategory());
        assertEquals("Test description", result.getDescription());
        assertEquals(ReportStatus.PENDING, result.getStatus());
    }

    @Test
    @DisplayName("Mengambil email pengguna dari repository berdasarkan userId jika tidak disediakan dalam permintaan")
    public void testCreateReport_withEmptyUserEmail() {
        // Arrange
        Long userId = 1L;
        String repositoryEmail = "user@example.com";

        CreateReportRequest createRequest = new CreateReportRequest();
        createRequest.setUserId(userId);
        createRequest.setUserEmail(""); // Email kosong
        createRequest.setCategory(ReportCategory.PAYMENT);
        createRequest.setDescription("Test description");

        User mockUser = new User();
        mockUser.setEmail(repositoryEmail);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(UUID.randomUUID());
            return report;
        });

        // Act
        ReportResponseDTO result = reportService.createReport(createRequest);

        // Assert
        assertEquals(repositoryEmail, result.getUserEmail());
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Mengambil laporan berdasarkan ID")
    public void testGetReportById() {
        // Arrange
        UUID id = UUID.randomUUID();
        Long userId = 1L;
        String userEmail = "user@example.com";

        Report report = new Report();
        report.setId(id);
        report.setUserId(userId);
        report.setUserEmail(userEmail);
        report.setCategory(ReportCategory.PAYMENT);
        report.setDescription("Test report");
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());

        when(reportRepository.findById(id)).thenReturn(Optional.of(report));

        // Act
        ReportResponseDTO result = reportService.getReportById(id);

        // Assert
        verify(reportRepository).findById(id);
        assertEquals(id, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(userEmail, result.getUserEmail());
        assertEquals(ReportCategory.PAYMENT, result.getCategory());
        assertEquals("Test report", result.getDescription());
        assertEquals(ReportStatus.PENDING, result.getStatus());
    }

    @Test
    @DisplayName("Mengambil daftar laporan berdasarkan ID pengguna")
    public void testGetReportsByUserId() {
        // Arrange
        Long userId = 1L;
        String userEmail = "user@example.com";

        Report report1 = new Report();
        report1.setId(UUID.randomUUID());
        report1.setUserId(userId);
        report1.setUserEmail(userEmail);
        report1.setCategory(ReportCategory.PAYMENT);
        report1.setDescription("User report 1");
        report1.setStatus(ReportStatus.PENDING);
        report1.setCreatedAt(LocalDateTime.now());

        Report report2 = new Report();
        report2.setId(UUID.randomUUID());
        report2.setUserId(userId);
        report2.setUserEmail(userEmail);
        report2.setCategory(ReportCategory.TICKET);
        report2.setDescription("User report 2");
        report2.setStatus(ReportStatus.RESOLVED);
        report2.setCreatedAt(LocalDateTime.now());

        List<Report> reports = Arrays.asList(report1, report2);
        when(reportRepository.findByUserId(userId)).thenReturn(reports);

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByUserId(userId);

        // Assert
        verify(reportRepository).findByUserId(userId);
        assertEquals(2, result.size());
        assertEquals(ReportCategory.PAYMENT, result.get(0).getCategory());
        assertEquals(ReportCategory.TICKET, result.get(1).getCategory());
    }

    @Test
    @DisplayName("Mengambil daftar laporan berdasarkan email pengguna")
    public void testGetReportsByUserEmail() {
        // Arrange
        String email = "user@example.com";

        Report report1 = new Report();
        report1.setId(UUID.randomUUID());
        report1.setUserId(1L);
        report1.setUserEmail(email);
        report1.setCategory(ReportCategory.PAYMENT);
        report1.setDescription("User report 1");
        report1.setStatus(ReportStatus.PENDING);
        report1.setCreatedAt(LocalDateTime.now());

        Report report2 = new Report();
        report2.setId(UUID.randomUUID());
        report2.setUserId(1L);
        report2.setUserEmail(email);
        report2.setCategory(ReportCategory.TICKET);
        report2.setDescription("User report 2");
        report2.setStatus(ReportStatus.RESOLVED);
        report2.setCreatedAt(LocalDateTime.now());

        List<Report> reports = Arrays.asList(report1, report2);
        when(reportRepository.findByUserEmail(email)).thenReturn(reports);

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByUserEmail(email);

        // Assert
        verify(reportRepository).findByUserEmail(email);
        assertEquals(2, result.size());
        assertEquals(ReportCategory.PAYMENT, result.get(0).getCategory());
        assertEquals(ReportCategory.TICKET, result.get(1).getCategory());
    }

    @Test
    @DisplayName("Menambahkan komentar pada laporan dan memperbarui status jika masih pending")
    public void testAddComment() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Long responderId = 1L;
        String responderEmail = "admin@example.com";
        String responderRole = "ADMIN";
        String message = "Test comment";

        Report report = new Report();
        report.setId(reportId);
        report.setStatus(ReportStatus.PENDING);

        CreateReportCommentRequest commentRequest = new CreateReportCommentRequest();
        commentRequest.setResponderId(responderId);
        commentRequest.setResponderEmail(responderEmail);
        commentRequest.setResponderRole(responderRole);
        commentRequest.setMessage(message);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(responseRepository.save(any(ReportResponse.class))).thenAnswer(invocation -> {
            ReportResponse savedResponse = invocation.getArgument(0);
            savedResponse.setId(UUID.randomUUID());
            return savedResponse;
        });

        // Act
        ReportCommentDTO result = reportService.addComment(reportId, commentRequest);

        // Assert
        verify(reportRepository).findById(reportId);
        verify(responseRepository).save(any(ReportResponse.class));
        verify(notificationService).onResponseAdded(eq(report), any(ReportResponse.class));

        assertEquals(ReportStatus.ON_PROGRESS, report.getStatus());
        assertNotNull(result.getId());
        assertEquals(reportId, result.getReportId());
        assertEquals(responderId, result.getResponderId());
        assertEquals(responderEmail, result.getResponderEmail());
        assertEquals(responderRole, result.getResponderRole());
        assertEquals(message, result.getMessage());
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
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        // Act
        ReportResponseDTO result = reportService.updateReportStatus(reportId, ReportStatus.RESOLVED);

        // Assert
        verify(reportRepository).findById(reportId);
        verify(reportRepository).save(report);
        verify(notificationService).onStatusChanged(report, ReportStatus.PENDING, ReportStatus.RESOLVED);

        assertEquals(reportId, result.getId());
        assertEquals(ReportStatus.RESOLVED, result.getStatus());
    }

    @Test
    @DisplayName("Menghapus laporan berdasarkan ID")
    public void testDeleteReport() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setId(reportId);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // Act
        reportService.deleteReport(reportId);

        // Assert
        verify(reportRepository).findById(reportId);
        verify(reportRepository).delete(report);
    }

    @Test
    @DisplayName("Mengambil daftar laporan berdasarkan status tertentu")
    public void testGetReportsByStatus_withStatus() {
        // Arrange
        Report report1 = new Report();
        report1.setId(UUID.randomUUID());
        report1.setCategory(ReportCategory.PAYMENT);
        report1.setStatus(ReportStatus.PENDING);
        report1.setCreatedAt(LocalDateTime.now());

        Report report2 = new Report();
        report2.setId(UUID.randomUUID());
        report2.setCategory(ReportCategory.TICKET);
        report2.setStatus(ReportStatus.PENDING);
        report2.setCreatedAt(LocalDateTime.now());

        List<Report> reports = Arrays.asList(report1, report2);
        when(reportRepository.findByStatus(ReportStatus.PENDING)).thenReturn(reports);

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByStatus(ReportStatus.PENDING);

        // Assert
        verify(reportRepository).findByStatus(ReportStatus.PENDING);
        assertEquals(2, result.size());
        assertEquals(ReportStatus.PENDING, result.get(0).getStatus());
        assertEquals(ReportStatus.PENDING, result.get(1).getStatus());
    }

    @Test
    @DisplayName("Mengambil semua laporan")
    public void testGetReportsByStatus_withNulStaltus() {
        // Arrange
        Report report1 = new Report();
        report1.setId(UUID.randomUUID());
        report1.setCategory(ReportCategory.PAYMENT);
        report1.setStatus(ReportStatus.PENDING);
        report1.setCreatedAt(LocalDateTime.now());

        Report report2 = new Report();
        report2.setId(UUID.randomUUID());
        report2.setCategory(ReportCategory.TICKET);
        report2.setStatus(ReportStatus.RESOLVED);
        report2.setCreatedAt(LocalDateTime.now());

        List<Report> reports = Arrays.asList(report1, report2);
        when(reportRepository.findAll()).thenReturn(reports);

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByStatus(null);

        // Assert
        verify(reportRepository).findAll();
        assertEquals(2, result.size());
        assertEquals(ReportStatus.PENDING, result.get(0).getStatus());
        assertEquals(ReportStatus.RESOLVED, result.get(1).getStatus());
    }

    @Test
    @DisplayName("Mencari email pengguna dari repository jika null dalam permintaan")
    public void testCreateReport_withoutUserEmail() {
        // Arrange
        Long userId = 1L;

        CreateReportRequest createRequest = new CreateReportRequest();
        createRequest.setUserId(userId);
        createRequest.setUserEmail(null); // Email null
        createRequest.setCategory(ReportCategory.PAYMENT);
        createRequest.setDescription("Test description");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> reportService.createReport(createRequest)
        );

        assertTrue(exception.getMessage().contains("User email not found for userId: " + userId));
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Tidak menambah observer notifikasi jika sudah terdaftar")
    public void testAddComment_withNotificationServiceAlreadyObserver() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Long responderId = 1L;
        String responderEmail = "admin@example.com";
        String responderRole = "ADMIN";
        String message = "Test comment";

        Report report = new Report();
        report.setId(reportId);
        report.setStatus(ReportStatus.PENDING);
        report.getObservers().add(notificationService);

        CreateReportCommentRequest commentRequest = new CreateReportCommentRequest();
        commentRequest.setResponderId(responderId);
        commentRequest.setResponderEmail(responderEmail);
        commentRequest.setResponderRole(responderRole);
        commentRequest.setMessage(message);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(responseRepository.save(any(ReportResponse.class))).thenAnswer(invocation -> {
            ReportResponse savedResponse = invocation.getArgument(0);
            savedResponse.setId(UUID.randomUUID());
            return savedResponse;
        });

        // Act
        ReportCommentDTO result = reportService.addComment(reportId, commentRequest);

        // Assert
        verify(reportRepository).findById(reportId);
        verify(responseRepository).save(any(ReportResponse.class));
        assertTrue(report.getObservers().contains(notificationService));

        assertNotNull(result.getId());
        assertEquals(reportId, result.getReportId());
        assertEquals(responderId, result.getResponderId());
        assertEquals(responderEmail, result.getResponderEmail());
        assertEquals(responderRole, result.getResponderRole());
        assertEquals(message, result.getMessage());
    }

    @Test
    @DisplayName("Tidak menambah observer notifikasi pada update status jika sudah terdaftar")
    public void testUpdateReportStatus_withNotificationServiceAlreadyObserver() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        ReportStatus newStatus = ReportStatus.RESOLVED;

        Report report = new Report();
        report.setId(reportId);
        report.setStatus(ReportStatus.PENDING);
        report.getObservers().add(notificationService);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report updatedReport = invocation.getArgument(0);
            updatedReport.setId(reportId);
            return updatedReport;
        });

        // Act
        ReportResponseDTO result = reportService.updateReportStatus(reportId, newStatus);

        // Assert
        verify(reportRepository).findById(reportId);
        verify(reportRepository).save(report);
        assertTrue(report.getObservers().contains(notificationService));
        assertEquals(newStatus, report.getStatus());

        assertNotNull(result.getId());
        assertEquals(reportId, result.getId());
        assertEquals(newStatus, result.getStatus());
    }

    @Test
    @DisplayName("Melempar EntityNotFoundException ketika laporan tidak ditemukan berdasarkan ID")
    public void testGetReportById_NotFound() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> reportService.getReportById(reportId)
        );
        assertTrue(exception.getMessage().contains(reportId.toString()));
    }

    @Test
    @DisplayName("Tidak mengubah status laporan yang sudah dalam proses")
    public void testAddComment_NonPendingStatus() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Long responderId = 1L;
        String responderEmail = "admin@example.com";

        Report report = new Report();
        report.setId(reportId);
        report.setStatus(ReportStatus.ON_PROGRESS); // Sudah dalam ON_PROGRESS

        CreateReportCommentRequest commentRequest = new CreateReportCommentRequest();
        commentRequest.setResponderId(responderId);
        commentRequest.setResponderEmail(responderEmail);
        commentRequest.setResponderRole("ADMIN");
        commentRequest.setMessage("Test comment for non-pending report");

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(responseRepository.save(any(ReportResponse.class))).thenAnswer(invocation -> {
            ReportResponse savedResponse = invocation.getArgument(0);
            savedResponse.setId(UUID.randomUUID());
            return savedResponse;
        });

        // Act
        ReportCommentDTO result = reportService.addComment(reportId, commentRequest);

        // Assert
        verify(reportRepository).findById(reportId);
        verify(responseRepository).save(any(ReportResponse.class));
        assertEquals(ReportStatus.ON_PROGRESS, report.getStatus());
        verify(reportRepository, never()).save(report);
    }

    @Test
    @DisplayName("Menangani respons null dengan mengembalikan daftar komentar kosong")
    public void testConvertToResponseDTO_WithNullResponses() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Long userId = 1L;
        String userEmail = "user@example.com";

        Report report = new Report();
        report.setId(reportId);
        report.setUserId(userId);
        report.setUserEmail(userEmail);
        report.setCategory(ReportCategory.PAYMENT);
        report.setDescription("Test report");
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        report.setResponses(null);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // Act
        ReportResponseDTO result = reportService.getReportById(reportId);

        // Assert
        verify(reportRepository).findById(reportId);
        assertEquals(reportId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(userEmail, result.getUserEmail());
        assertNotNull(result.getComments());
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    @DisplayName("Menangani respons tanpa reference ke laporan")
    public void testConvertToCommentDTO_WithNullReport() {
        // Arrange
        UUID commentId = UUID.randomUUID();
        Long responderId = 1L;
        String responderEmail = "admin@example.com";

        ReportResponse response = new ReportResponse();
        response.setId(commentId);
        response.setResponderId(responderId);
        response.setResponderEmail(responderEmail);
        response.setResponderRole("ADMIN");
        response.setMessage("Test comment");
        response.setCreatedAt(LocalDateTime.now());
        response.setReport(null);

        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setId(reportId);
        report.setStatus(ReportStatus.PENDING);
        report.setResponses(Collections.singletonList(response));

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // Act
        ReportResponseDTO result = reportService.getReportById(reportId);

        // Assert
        verify(reportRepository).findById(reportId);
        assertNotNull(result.getComments());
        assertEquals(1, result.getComments().size());

        ReportCommentDTO commentDTO = result.getComments().get(0);
        assertEquals(commentId, commentDTO.getId());
        assertEquals(responderId, commentDTO.getResponderId());
        assertEquals(responderEmail, commentDTO.getResponderEmail());
        assertNull(commentDTO.getReportId());
    }

    @Test
    @DisplayName("Melempar EntityNotFoundException ketika menghapus laporan yang tidak ditemukan")
    public void testDeleteReport_NotFound() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> reportService.deleteReport(reportId)
        );
        verify(reportRepository).findById(reportId);
        verify(reportRepository, never()).delete(any(Report.class));
    }

    @Test
    @DisplayName("Melempar EntityNotFoundException ketika memperbarui status laporan yang tidak ditemukan")
    public void testUpdateReportStatus_NotFound() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                EntityNotFoundException.class,
                () -> reportService.updateReportStatus(reportId, ReportStatus.RESOLVED)
        );
        verify(reportRepository).findById(reportId);
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    @DisplayName("Memotong deskripsi panjang menjadi deskripsi ringkas")
    public void testLongDescriptionTruncation() {
        // Arrange
        String longDescription = "This is a very long description that needs to be truncated because it exceeds fifty characters!";

        Report report = new Report();
        report.setId(UUID.randomUUID());
        report.setCategory(ReportCategory.PAYMENT);
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        report.setDescription(longDescription);

        List<Report> reports = List.of(report);
        when(reportRepository.findAll()).thenReturn(reports);

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByStatus(null);

        // Assert
        assertEquals(1, result.size());
        assertEquals(longDescription.substring(0, 47) + "...", result.get(0).getShortDescription());
    }

    @Test
    @DisplayName("Tidak memotong deskripsi pendek dalam ringkasan laporan")
    public void testShortDescription() {
        // Arrange
        String shortDescription = "This is a short description.";

        Report report = new Report();
        report.setId(UUID.randomUUID());
        report.setCategory(ReportCategory.PAYMENT);
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        report.setDescription(shortDescription);

        List<Report> reports = List.of(report);
        when(reportRepository.findAll()).thenReturn(reports);

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByStatus(null);

        // Assert
        assertEquals(1, result.size());
        assertEquals(shortDescription, result.get(0).getShortDescription());
    }

    @Test
    @DisplayName("Menangani deskripsi null dalam ringkasan laporan")
    public void testNullDescription() {
        // Arrange
        Report report = new Report();
        report.setId(UUID.randomUUID());
        report.setCategory(ReportCategory.PAYMENT);
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        report.setDescription(null);

        List<Report> reports = List.of(report);
        when(reportRepository.findAll()).thenReturn(reports);

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByStatus(null);

        // Assert
        assertEquals(1, result.size());
        assertNull(result.get(0).getShortDescription());
    }

    @Test
    @DisplayName("Menangani daftar respons null saat menghitung jumlah komentar")
    public void testNullResponsesList() {
        // Arrange
        Report report = new Report();
        report.setId(UUID.randomUUID());
        report.setCategory(ReportCategory.PAYMENT);
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        report.setDescription("Test description");
        report.setResponses(null);

        List<Report> reports = List.of(report);
        when(reportRepository.findAll()).thenReturn(reports);

        // Act
        List<ReportSummaryDTO> result = reportService.getReportsByStatus(null);

        // Assert
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getCommentCount());
    }

    @Test
    @DisplayName("Melempar EntityNotFoundException jika pengguna tidak ditemukan")
    public void testCreateReport_userNotFound() {
        // Arrange
        Long userId = 999L;

        CreateReportRequest createRequest = new CreateReportRequest();
        createRequest.setUserId(userId);
        createRequest.setUserEmail(null); // Email null
        createRequest.setCategory(ReportCategory.PAYMENT);
        createRequest.setDescription("Test description");

        // Mock untuk userRepository yang mengembalikan Optional.empty (pengguna tidak ditemukan)
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(UUID.randomUUID());
            return report;
        });

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> reportService.createReport(createRequest)
        );

        // Memastikan pesan error sesuai dengan yang diharapkan
        assertTrue(exception.getMessage().contains("User email not found for userId: " + userId));

        // Verifikasi bahwa method findById dipanggil dengan parameter yang benar
        verify(userRepository).findById(userId);
    }
}