package id.ac.ui.cs.advprog.eventsphere.report.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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
    private FileStorageService fileStorageService;
    private ReportService reportService;

    @BeforeEach
    public void setUp() {
        reportRepository = mock(ReportRepository.class);
        responseRepository = mock(ReportResponseRepository.class);
        notificationService = mock(NotificationService.class);
        fileStorageService = mock(FileStorageService.class);

        reportService = new ReportService(
                reportRepository,
                responseRepository,
                notificationService,
                fileStorageService
        );
    }

    @Test
    public void testCreateReport() throws IOException {
        // Create test data
        UUID userId = UUID.randomUUID();
        CreateReportRequest createRequest = new CreateReportRequest();
        createRequest.setUserId(userId);
        createRequest.setCategory(ReportCategory.PAYMENT);
        createRequest.setDescription("Test description");

        // Create mock attachments
        MultipartFile file1 = new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "test1".getBytes());
        MultipartFile file2 = new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "test2".getBytes());
        List<MultipartFile> attachments = Arrays.asList(file1, file2);

        // Mock repository behavior
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(UUID.randomUUID());
            return report;
        });

        // Mock fileStorage behavior
        when(fileStorageService.storeFile(file1)).thenReturn("test1-uuid.jpg");
        when(fileStorageService.storeFile(file2)).thenReturn("test2-uuid.jpg");

        // Call the service method
        ReportResponseDTO result = reportService.createReport(createRequest, attachments);

        // Verify repository and notification interactions
        verify(reportRepository).save(any(Report.class));
        verify(notificationService).notifyNewReport(any(Report.class));

        // Verify file storage interactions
        verify(fileStorageService).storeFile(file1);
        verify(fileStorageService).storeFile(file2);

        // Verify result
        assertNotNull(result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(ReportCategory.PAYMENT, result.getCategory());
        assertEquals("Test description", result.getDescription());
        assertEquals(ReportStatus.PENDING, result.getStatus());
        assertEquals(2, result.getAttachments().size());
    }

    @Test
    public void testGetReportById() {
        // Create test data
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Report report = new Report();
        report.setId(id);
        report.setUserId(userId);
        report.setCategory(ReportCategory.PAYMENT);
        report.setDescription("Test report");
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());

        // Mock repository behavior
        when(reportRepository.findById(id)).thenReturn(Optional.of(report));

        // Call the service method
        ReportResponseDTO result = reportService.getReportById(id);

        // Verify repository interactions
        verify(reportRepository).findById(id);

        // Verify result
        assertEquals(id, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(ReportCategory.PAYMENT, result.getCategory());
        assertEquals("Test report", result.getDescription());
        assertEquals(ReportStatus.PENDING, result.getStatus());
    }

    @Test
    public void testGetReportsByUserId() {
        // Create test data
        UUID userId = UUID.randomUUID();

        Report report1 = new Report();
        report1.setId(UUID.randomUUID());
        report1.setUserId(userId);
        report1.setCategory(ReportCategory.PAYMENT);
        report1.setDescription("User report 1");
        report1.setStatus(ReportStatus.PENDING);
        report1.setCreatedAt(LocalDateTime.now());

        Report report2 = new Report();
        report2.setId(UUID.randomUUID());
        report2.setUserId(userId);
        report2.setCategory(ReportCategory.TICKET);
        report2.setDescription("User report 2");
        report2.setStatus(ReportStatus.RESOLVED);
        report2.setCreatedAt(LocalDateTime.now());

        List<Report> reports = Arrays.asList(report1, report2);

        // Mock repository behavior
        when(reportRepository.findByUserId(userId)).thenReturn(reports);

        // Call the service method
        List<ReportSummaryDTO> result = reportService.getReportsByUserId(userId);

        // Verify repository interactions
        verify(reportRepository).findByUserId(userId);

        // Verify result
        assertEquals(2, result.size());
        assertEquals(ReportCategory.PAYMENT, result.get(0).getCategory());
        assertEquals(ReportCategory.TICKET, result.get(1).getCategory());
    }

    @Test
    public void testAddComment() {
        // Create test data
        UUID reportId = UUID.randomUUID();
        UUID responderId = UUID.randomUUID();

        Report report = new Report();
        report.setId(reportId);
        report.setStatus(ReportStatus.PENDING);

        CreateReportCommentRequest commentRequest = new CreateReportCommentRequest();
        commentRequest.setResponderId(responderId);
        commentRequest.setResponderRole("ADMIN");
        commentRequest.setMessage("Test comment");

        // Mock repository behavior
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(responseRepository.save(any(ReportResponse.class))).thenAnswer(invocation -> {
            ReportResponse response = invocation.getArgument(0);
            response.setId(UUID.randomUUID());
            return response;
        });

        // Call the service method
        ReportCommentDTO result = reportService.addComment(reportId, commentRequest);

        // Verify repository interactions
        verify(reportRepository).findById(reportId);
        verify(responseRepository).save(any(ReportResponse.class));

        // Verify observer was notified
        verify(notificationService).onResponseAdded(eq(report), any(ReportResponse.class));

        // Verify status was updated
        assertEquals(ReportStatus.ON_PROGRESS, report.getStatus());

        // Verify result
        assertNotNull(result.getId());
        assertEquals(reportId, result.getReportId());
        assertEquals(responderId, result.getResponderId());
        assertEquals("ADMIN", result.getResponderRole());
        assertEquals("Test comment", result.getMessage());
    }

    @Test
    public void testUpdateReportStatus() {
        // Create test data
        UUID reportId = UUID.randomUUID();

        Report report = new Report();
        report.setId(reportId);
        report.setStatus(ReportStatus.PENDING);

        // Mock repository behavior
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        // Call the service method
        ReportResponseDTO result = reportService.updateReportStatus(reportId, ReportStatus.RESOLVED);

        // Verify repository interactions
        verify(reportRepository).findById(reportId);
        verify(reportRepository).save(report);

        // Verify observer was notified
        verify(notificationService).onStatusChanged(report, ReportStatus.PENDING, ReportStatus.RESOLVED);

        // Verify result
        assertEquals(reportId, result.getId());
        assertEquals(ReportStatus.RESOLVED, result.getStatus());
    }

    @Test
    public void testDeleteReport() throws IOException {
        // Create test data
        UUID reportId = UUID.randomUUID();

        Report report = new Report();
        report.setId(reportId);

        List<String> attachments = Arrays.asList("file1.jpg", "file2.jpg");
        report.setAttachments(attachments);

        // Mock repository behavior
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // Call the service method
        reportService.deleteReport(reportId);

        // Verify repository interactions
        verify(reportRepository).findById(reportId);
        verify(reportRepository).delete(report);

        // Verify file deletion
        verify(fileStorageService).deleteFile("file1.jpg");
        verify(fileStorageService).deleteFile("file2.jpg");
    }
}