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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    public void testCreateReport() throws IOException {
        // Create test data
        Long userId = 1L;
        String userEmail = "user@example.com";

        CreateReportRequest createRequest = new CreateReportRequest();
        createRequest.setUserId(userId);
        createRequest.setUserEmail(userEmail);
        createRequest.setCategory(ReportCategory.PAYMENT);
        createRequest.setDescription("Test description");

        // Create mock user
        User mockUser = new User();
        mockUser.setEmail(userEmail);

        // Mock repository behavior
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(UUID.randomUUID());
            return report;
        });

        // Call the service method
        ReportResponseDTO result = reportService.createReport(createRequest);

        // Verify repository and notification interactions
        verify(reportRepository).save(any(Report.class));
        verify(notificationService).notifyNewReport(any(Report.class));

        // Verify result
        assertNotNull(result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(userEmail, result.getUserEmail());
        assertEquals(ReportCategory.PAYMENT, result.getCategory());
        assertEquals("Test description", result.getDescription());
        assertEquals(ReportStatus.PENDING, result.getStatus());
        assertTrue(result.getAttachments().isEmpty());
    }

    @Test
    public void testCreateReport_withEmptyUserEmail() {
        // Create test data with empty email string
        Long userId = 1L;

        CreateReportRequest createRequest = new CreateReportRequest();
        createRequest.setUserId(userId);
        createRequest.setUserEmail(""); // Empty string email
        createRequest.setCategory(ReportCategory.PAYMENT);
        createRequest.setDescription("Test description");

        // Create mock user
        User mockUser = new User();
        mockUser.setEmail("user@example.com");

        // Mock repository behavior
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(UUID.randomUUID());
            return report;
        });

        // Call the service method
        ReportResponseDTO result = reportService.createReport(createRequest);

        // Verify result has email from user repository
        assertEquals("user@example.com", result.getUserEmail());

        // Verify userRepository was called to retrieve the email
        verify(userRepository).findById(userId);
    }

    @Test
    public void testGetReportById() {
        // Create test data
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

        // Mock repository behavior
        when(reportRepository.findById(id)).thenReturn(Optional.of(report));

        // Call the service method
        ReportResponseDTO result = reportService.getReportById(id);

        // Verify repository interactions
        verify(reportRepository).findById(id);

        // Verify result
        assertEquals(id, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(userEmail, result.getUserEmail());
        assertEquals(ReportCategory.PAYMENT, result.getCategory());
        assertEquals("Test report", result.getDescription());
        assertEquals(ReportStatus.PENDING, result.getStatus());
    }

    @Test
    public void testGetReportsByUserId() {
        // Create test data
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
    public void testGetReportsByUserEmail() {
        // Create test data
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

        // Mock repository behavior
        when(reportRepository.findByUserEmail(email)).thenReturn(reports);

        // Call the service method
        List<ReportSummaryDTO> result = reportService.getReportsByUserEmail(email);

        // Verify repository interactions
        verify(reportRepository).findByUserEmail(email);

        // Verify result
        assertEquals(2, result.size());
        assertEquals(ReportCategory.PAYMENT, result.get(0).getCategory());
        assertEquals(ReportCategory.TICKET, result.get(1).getCategory());
    }

    @Test
    public void testAddComment() {
        // Create test data
        UUID reportId = UUID.randomUUID();
        Long responderId = 1L;
        String responderEmail = "admin@example.com";
        String responderRole = "ADMIN";
        String message = "Test comment";

        Report report = new Report();
        report.setId(reportId);
        report.setStatus(ReportStatus.PENDING);  // Initially PENDING

        CreateReportCommentRequest commentRequest = new CreateReportCommentRequest();
        commentRequest.setResponderId(responderId);
        commentRequest.setResponderEmail(responderEmail);
        commentRequest.setResponderRole(responderRole);
        commentRequest.setMessage(message);

        // Mock repository behavior
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(responseRepository.save(any(ReportResponse.class))).thenAnswer(invocation -> {
            ReportResponse savedResponse = invocation.getArgument(0);
            savedResponse.setId(UUID.randomUUID()); // Mock saving the comment
            return savedResponse;
        });

        // Call the service method
        ReportCommentDTO result = reportService.addComment(reportId, commentRequest);

        // Verify repository interactions
        verify(reportRepository).findById(reportId);
        verify(responseRepository).save(any(ReportResponse.class));

        // Verify notification service was called
        verify(notificationService).onResponseAdded(eq(report), any(ReportResponse.class));

        // Verify the report status is updated to ON_PROGRESS
        assertEquals(ReportStatus.ON_PROGRESS, report.getStatus());

        // Verify the result
        assertNotNull(result.getId());
        assertEquals(reportId, result.getReportId());
        assertEquals(responderId, result.getResponderId());
        assertEquals(responderEmail, result.getResponderEmail());
        assertEquals(responderRole, result.getResponderRole());
        assertEquals(message, result.getMessage());
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

        // Mock repository behavior
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // Call the service method
        reportService.deleteReport(reportId);

        // Verify repository interactions
        verify(reportRepository).findById(reportId);
        verify(reportRepository).delete(report);
    }

    @Test
    public void testGetReportsByStatus_withStatus() {
        // Create test data for reports
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

        // Mock repository behavior for fetching reports with PENDING status
        when(reportRepository.findByStatus(ReportStatus.PENDING)).thenReturn(reports);

        // Call the service method with PENDING status
        List<ReportSummaryDTO> result = reportService.getReportsByStatus(ReportStatus.PENDING);

        // Verify repository interactions
        verify(reportRepository).findByStatus(ReportStatus.PENDING);

        // Verify the result
        assertEquals(2, result.size());
        assertEquals(ReportStatus.PENDING, result.get(0).getStatus());
        assertEquals(ReportStatus.PENDING, result.get(1).getStatus());
    }

    @Test
    public void testGetReportsByStatus_withNullStatus() {
        // Create test data for reports
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

        // Mock repository behavior for fetching all reports
        when(reportRepository.findAll()).thenReturn(reports);

        // Call the service method with null status (should fetch all reports)
        List<ReportSummaryDTO> result = reportService.getReportsByStatus(null);

        // Verify repository interactions
        verify(reportRepository).findAll();

        // Verify the result
        assertEquals(2, result.size());
        assertEquals(ReportStatus.PENDING, result.get(0).getStatus());
        assertEquals(ReportStatus.RESOLVED, result.get(1).getStatus());
    }

    @Test
    public void testCreateReport_withoutUserEmail() throws IOException {
        // Create test data
        Long userId = 1L;

        CreateReportRequest createRequest = new CreateReportRequest();
        createRequest.setUserId(userId);
        createRequest.setUserEmail(null); // No email provided
        createRequest.setCategory(ReportCategory.PAYMENT);
        createRequest.setDescription("Test description");

        // Create mock user
        User mockUser = new User();
        mockUser.setEmail("user@example.com");

        // Mock repository behavior
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(UUID.randomUUID());
            return report;
        });

        // Call the service method
        ReportResponseDTO result = reportService.createReport(createRequest);

        // Verify result has email from user repository
        assertEquals("user@example.com", result.getUserEmail());
    }

    @Test
    public void testAddComment_withNotificationServiceAlreadyObserver() {
        // Create test data
        UUID reportId = UUID.randomUUID();
        Long responderId = 1L;
        String responderEmail = "admin@example.com";
        String responderRole = "ADMIN";
        String message = "Test comment";

        // Create a report with PENDING status
        Report report = new Report();
        report.setId(reportId);
        report.setStatus(ReportStatus.PENDING);

        // Add notificationService as an observer before calling the method
        report.getObservers().add(notificationService);

        // Ensure that notificationService is already in the observers list
        assertTrue(report.getObservers().contains(notificationService));

        // Create a comment request
        CreateReportCommentRequest commentRequest = new CreateReportCommentRequest();
        commentRequest.setResponderId(responderId);
        commentRequest.setResponderEmail(responderEmail);
        commentRequest.setResponderRole(responderRole);
        commentRequest.setMessage(message);

        // Mock repository behavior
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(responseRepository.save(any(ReportResponse.class))).thenAnswer(invocation -> {
            ReportResponse savedResponse = invocation.getArgument(0);
            savedResponse.setId(UUID.randomUUID()); // Mock saving the comment
            return savedResponse;
        });

        // Call the service method
        ReportCommentDTO result = reportService.addComment(reportId, commentRequest);

        // Verify repository interactions
        verify(reportRepository).findById(reportId);
        verify(responseRepository).save(any(ReportResponse.class));

        // Verify that notificationService is NOT added again
        assertTrue(report.getObservers().contains(notificationService));

        // Verify result
        assertNotNull(result.getId());
        assertEquals(reportId, result.getReportId());
        assertEquals(responderId, result.getResponderId());
        assertEquals(responderEmail, result.getResponderEmail());
        assertEquals(responderRole, result.getResponderRole());
        assertEquals(message, result.getMessage());
    }

    @Test
    public void testUpdateReportStatus_withNotificationServiceAlreadyObserver() {
        // Create test data
        UUID reportId = UUID.randomUUID();
        ReportStatus newStatus = ReportStatus.RESOLVED;

        // Create a report with PENDING status
        Report report = new Report();
        report.setId(reportId);
        report.setStatus(ReportStatus.PENDING);

        // Add notificationService as an observer before calling the method
        report.getObservers().add(notificationService);

        // Ensure notificationService is already in the observers list
        assertTrue(report.getObservers().contains(notificationService));

        // Mock repository behavior
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report updatedReport = invocation.getArgument(0);
            updatedReport.setId(reportId);  // Mock updated report
            return updatedReport;
        });

        // Call the service method to update the status
        ReportResponseDTO result = reportService.updateReportStatus(reportId, newStatus);

        // Verify repository interactions
        verify(reportRepository).findById(reportId);
        verify(reportRepository).save(report);

        // Verify that notificationService was NOT added again
        assertTrue(report.getObservers().contains(notificationService));

        // Verify that the report status was updated
        assertEquals(newStatus, report.getStatus());

        // Verify result
        assertNotNull(result.getId());
        assertEquals(reportId, result.getId());
        assertEquals(newStatus, result.getStatus());
    }

    @Test
    public void testGetReportById_NotFound() {
        // Create test data
        UUID reportId = UUID.randomUUID();

        // Mock repository behavior to return empty
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // Verify that EntityNotFoundException is thrown
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> reportService.getReportById(reportId)
        );

        // Verify exception message contains the report ID
        assertTrue(exception.getMessage().contains(reportId.toString()));
    }

    @Test
    public void testAddComment_NonPendingStatus() {
        // Create test data
        UUID reportId = UUID.randomUUID();
        Long responderId = 1L;
        String responderEmail = "admin@example.com";

        // Create report with NON-PENDING status (already ON_PROGRESS)
        Report report = new Report();
        report.setId(reportId);
        report.setStatus(ReportStatus.ON_PROGRESS); // Already in ON_PROGRESS

        CreateReportCommentRequest commentRequest = new CreateReportCommentRequest();
        commentRequest.setResponderId(responderId);
        commentRequest.setResponderEmail(responderEmail);
        commentRequest.setResponderRole("ADMIN");
        commentRequest.setMessage("Test comment for non-pending report");

        // Mock repository behavior
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(responseRepository.save(any(ReportResponse.class))).thenAnswer(invocation -> {
            ReportResponse savedResponse = invocation.getArgument(0);
            savedResponse.setId(UUID.randomUUID());
            return savedResponse;
        });

        // Call the service method
        ReportCommentDTO result = reportService.addComment(reportId, commentRequest);

        // Verify repository interactions
        verify(reportRepository).findById(reportId);
        verify(responseRepository).save(any(ReportResponse.class));

        // Verify the status wasn't changed (since it wasn't PENDING)
        assertEquals(ReportStatus.ON_PROGRESS, report.getStatus());

        // Verify reportRepository.save wasn't called a second time to update status
        verify(reportRepository, never()).save(report);
    }

    @Test
    public void testConvertToResponseDTO_WithNullResponses() {
        // Create test data
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
        report.setResponses(null); // Explicitly set responses to null

        // Mock repository behavior
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // Call the service method
        ReportResponseDTO result = reportService.getReportById(reportId);

        // Verify repository interactions
        verify(reportRepository).findById(reportId);

        // Verify result
        assertEquals(reportId, result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(userEmail, result.getUserEmail());
        assertNotNull(result.getComments()); // Should have an empty list, not null
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    public void testConvertToCommentDTO_WithNullReport() {
        // Create test data
        UUID commentId = UUID.randomUUID();
        Long responderId = 1L;
        String responderEmail = "admin@example.com";

        // Create report response with null report reference
        ReportResponse response = new ReportResponse();
        response.setId(commentId);
        response.setResponderId(responderId);
        response.setResponderEmail(responderEmail);
        response.setResponderRole("ADMIN");
        response.setMessage("Test comment");
        response.setCreatedAt(LocalDateTime.now());
        response.setReport(null); // Explicitly set report to null

        // Create a report for findById
        UUID reportId = UUID.randomUUID();
        Report report = new Report();
        report.setId(reportId);
        report.setStatus(ReportStatus.PENDING);
        report.setResponses(Collections.singletonList(response));

        // Mock repository behavior
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // Call the service method
        ReportResponseDTO result = reportService.getReportById(reportId);

        // Verify repository interactions
        verify(reportRepository).findById(reportId);

        // Verify result
        assertNotNull(result.getComments());
        assertEquals(1, result.getComments().size());

        ReportCommentDTO commentDTO = result.getComments().get(0);
        assertEquals(commentId, commentDTO.getId());
        assertEquals(responderId, commentDTO.getResponderId());
        assertEquals(responderEmail, commentDTO.getResponderEmail());
        assertNull(commentDTO.getReportId()); // Should be null since report is null
    }

    @Test
    public void testDeleteReport_NotFound() throws IOException {
        // Create test data
        UUID reportId = UUID.randomUUID();

        // Mock repository behavior to return empty
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // Verify that EntityNotFoundException is thrown
        assertThrows(
                EntityNotFoundException.class,
                () -> reportService.deleteReport(reportId)
        );

        // Verify repository interactions
        verify(reportRepository).findById(reportId);
        verify(reportRepository, never()).delete(any(Report.class));
    }

    @Test
    public void testUpdateReportStatus_NotFound() {
        // Create test data
        UUID reportId = UUID.randomUUID();

        // Mock repository behavior to return empty
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        // Verify that EntityNotFoundException is thrown
        assertThrows(
                EntityNotFoundException.class,
                () -> reportService.updateReportStatus(reportId, ReportStatus.RESOLVED)
        );

        // Verify repository interactions
        verify(reportRepository).findById(reportId);
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    public void testLongDescriptionTruncation() {
        // Create test data with long description
        String longDescription = "This is a very long description that needs to be truncated because it exceeds fifty characters!";

        Report report = new Report();
        report.setId(UUID.randomUUID());
        report.setCategory(ReportCategory.PAYMENT);
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        report.setDescription(longDescription);

        List<Report> reports = List.of(report);

        // Mock repository
        when(reportRepository.findAll()).thenReturn(reports);

        // Call service method
        List<ReportSummaryDTO> result = reportService.getReportsByStatus(null);

        // Verify the result contains truncated description
        assertEquals(1, result.size());
        assertEquals(longDescription.substring(0, 47) + "...", result.get(0).getShortDescription());
    }

    @Test
    public void testShortDescription() {
        // Create test data with short description
        String shortDescription = "This is a short description.";

        Report report = new Report();
        report.setId(UUID.randomUUID());
        report.setCategory(ReportCategory.PAYMENT);
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        report.setDescription(shortDescription);

        List<Report> reports = List.of(report);

        // Mock repository
        when(reportRepository.findAll()).thenReturn(reports);

        // Call service method
        List<ReportSummaryDTO> result = reportService.getReportsByStatus(null);

        // Verify the result contains the original description (not truncated)
        assertEquals(1, result.size());
        assertEquals(shortDescription, result.get(0).getShortDescription());
    }

    @Test
    public void testNullDescription() {
        // Create test data with null description
        Report report = new Report();
        report.setId(UUID.randomUUID());
        report.setCategory(ReportCategory.PAYMENT);
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        report.setDescription(null); // Explicitly set description to null

        List<Report> reports = List.of(report);

        // Mock repository
        when(reportRepository.findAll()).thenReturn(reports);

        // Call service method
        List<ReportSummaryDTO> result = reportService.getReportsByStatus(null);

        // Verify the result has null or empty short description
        assertEquals(1, result.size());
        assertNull(result.get(0).getShortDescription());
    }

    @Test
    public void testNullResponsesList() {
        // Create test data with null responses list
        Report report = new Report();
        report.setId(UUID.randomUUID());
        report.setCategory(ReportCategory.PAYMENT);
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        report.setDescription("Test description");
        report.setResponses(null); // Explicitly set responses to null

        List<Report> reports = List.of(report);

        // Mock repository
        when(reportRepository.findAll()).thenReturn(reports);

        // Call service method
        List<ReportSummaryDTO> result = reportService.getReportsByStatus(null);

        // Verify the result has 0 comment count
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getCommentCount());
    }
}