package id.ac.ui.cs.advprog.eventsphere.report.model;

import id.ac.ui.cs.advprog.eventsphere.report.observer.ReportObserver;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReportTest {

    @Test
    public void testCreateReport() {
        Long userId = 1L;
        String userEmail = "user@example.com";
        ReportCategory category = ReportCategory.PAYMENT;
        String description = "Payment failed but money was deducted";

        Report report = new Report(userId, userEmail, category, description);

        // ID akan null sampai disimpan ke database
        assertEquals(userId, report.getUserId());
        assertEquals(userEmail, report.getUserEmail());
        assertEquals(category, report.getCategory());
        assertEquals(description, report.getDescription());
        assertEquals(ReportStatus.PENDING, report.getStatus());
        assertNotNull(report.getCreatedAt());
        assertNull(report.getUpdatedAt());
        assertTrue(report.getAttachments().isEmpty());
        assertTrue(report.getResponses().isEmpty());
    }

    @Test
    public void testReportSettersAndGetters() {
        Report report = new Report();
        UUID id = UUID.randomUUID();
        Long userId = 1L;
        String userEmail = "user@example.com";
        LocalDateTime now = LocalDateTime.now();

        report.setId(id);
        report.setUserId(userId);
        report.setUserEmail(userEmail);
        report.setCategory(ReportCategory.EVENT);
        report.setDescription("Test description");
        report.setStatus(ReportStatus.ON_PROGRESS);
        report.setCreatedAt(now);
        report.setUpdatedAt(now);
        report.setAttachments(new ArrayList<>());

        assertEquals(id, report.getId());
        assertEquals(userId, report.getUserId());
        assertEquals(userEmail, report.getUserEmail());
        assertEquals(ReportCategory.EVENT, report.getCategory());
        assertEquals("Test description", report.getDescription());
        assertEquals(ReportStatus.ON_PROGRESS, report.getStatus());
        assertEquals(now, report.getCreatedAt());
        assertEquals(now, report.getUpdatedAt());
        assertNotNull(report.getAttachments());
    }

    @Test
    public void testUpdateStatus() {
        // Create a report
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Test description");
        report.setStatus(ReportStatus.PENDING);

        // Create and register a mock observer
        ReportObserver mockObserver = mock(ReportObserver.class);
        report.getObservers().add(mockObserver);

        // Update the status
        report.updateStatus(ReportStatus.ON_PROGRESS);

        // Verify the status was updated
        assertEquals(ReportStatus.ON_PROGRESS, report.getStatus());
        assertNotNull(report.getUpdatedAt());

        // Verify the observer was notified
        verify(mockObserver).onStatusChanged(report, ReportStatus.PENDING, ReportStatus.ON_PROGRESS);
    }

    @Test
    public void testAddAttachment() {
        // Create a report
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Test description");

        // Add an attachment
        report.getAttachments().add("file1.jpg");

        // Verify attachment was added
        assertEquals(1, report.getAttachments().size());
        assertEquals("file1.jpg", report.getAttachments().get(0));
    }

    @Test
    public void testAddResponse() {
        // Create a report
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Test description");

        // Create a response
        ReportResponse response = new ReportResponse();
        response.setResponderId(2L);
        response.setResponderEmail("admin@example.com");
        response.setResponderRole("ADMIN");
        response.setMessage("Admin response");

        // Create and register a mock observer
        ReportObserver mockObserver = mock(ReportObserver.class);
        report.getObservers().add(mockObserver);

        // Add the response
        report.addResponse(response);

        // Verify response was added
        assertEquals(1, report.getResponses().size());
        assertEquals(response, report.getResponses().get(0));
        assertEquals(report, response.getReport());

        // Verify observer was notified
        verify(mockObserver).onResponseAdded(report, response);
    }

    @Test
    public void testRemoveObserver() {
        // Create a report
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Test description");

        // Create and register a mock observer
        ReportObserver mockObserver = mock(ReportObserver.class);
        report.getObservers().add(mockObserver);

        // Verify observer was added
        assertEquals(1, report.getObservers().size());

        // Remove the observer
        report.removeObserver(mockObserver);

        // Verify observer was removed
        assertEquals(0, report.getObservers().size());
    }

    @Test
    public void testAddObserver() {
        // Create a report
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Test description");

        // Create a mock observer
        ReportObserver mockObserver = mock(ReportObserver.class);

        // Add observer using the method
        report.addObserver(mockObserver);

        // Verify the observer is in the list
        assertTrue(report.getObservers().contains(mockObserver));

        // Try adding the same observer again
        report.addObserver(mockObserver);

        // Ensure it was not added twice
        assertEquals(1, report.getObservers().size());
    }
}