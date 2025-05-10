package id.ac.ui.cs.advprog.eventsphere.report.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class ReportResponseTest {

    @Test
    public void testReportResponseSettersAndGetters() {
        ReportResponse response = new ReportResponse();
        UUID id = UUID.randomUUID();
        Long responderId = 1L;
        String responderEmail = "admin@example.com";
        Report report = new Report();
        LocalDateTime now = LocalDateTime.now();

        response.setId(id);
        response.setReport(report);
        response.setResponderId(responderId);
        response.setResponderEmail(responderEmail);
        response.setResponderRole("ADMIN");
        response.setMessage("Test response message");
        response.setCreatedAt(now);

        assertEquals(id, response.getId());
        assertEquals(report, response.getReport());
        assertEquals(responderId, response.getResponderId());
        assertEquals(responderEmail, response.getResponderEmail());
        assertEquals("ADMIN", response.getResponderRole());
        assertEquals("Test response message", response.getMessage());
        assertEquals(now, response.getCreatedAt());
    }

    @Test
    public void testReportResponseValidation() {
        ReportResponse response = new ReportResponse();

        // Create message that exceeds max length
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 501; i++) {
            longMessage.append("a");
        }

        // Set valid values
        response.setResponderId(1L);
        response.setResponderEmail("admin@example.com");
        response.setResponderRole("ADMIN");

        // Test message validation
        assertThrows(IllegalArgumentException.class, () -> {
            response.setMessage(longMessage.toString());
        });

        // Test with valid message
        String validMessage = "This is a valid message";
        response.setMessage(validMessage);
        assertEquals(validMessage, response.getMessage());
    }

    @Test
    public void testReportResponseConstructor() {
        Long responderId = 1L;
        String responderEmail = "admin@example.com";
        String responderRole = "ADMIN";
        String message = "Test message";
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Test report");

        ReportResponse response = new ReportResponse(responderId, responderEmail, responderRole, message, report);

        assertEquals(responderId, response.getResponderId());
        assertEquals(responderEmail, response.getResponderEmail());
        assertEquals(responderRole, response.getResponderRole());
        assertEquals(message, response.getMessage());
        assertEquals(report, response.getReport());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    public void testReportResponseSetMessageNull() {
        ReportResponse response = new ReportResponse();
        response.setMessage(null);
        assertNull(response.getMessage());
    }
}