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
        UUID responderId = UUID.randomUUID();
        Report report = new Report();
        LocalDateTime now = LocalDateTime.now();

        response.setId(id);
        response.setReport(report);
        response.setResponderId(responderId);
        response.setResponderRole("ADMIN");
        response.setMessage("Test response message");
        response.setCreatedAt(now);

        assertEquals(id, response.getId());
        assertEquals(report, response.getReport());
        assertEquals(responderId, response.getResponderId());
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
        response.setResponderId(UUID.randomUUID());
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
}