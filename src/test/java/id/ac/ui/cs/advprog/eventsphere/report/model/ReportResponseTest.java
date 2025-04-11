package id.ac.ui.cs.advprog.eventsphere.report.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ReportResponseTest {

    @Test
    public void testCreateReportResponse() {
        UUID id = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        UUID responderId = UUID.randomUUID();
        UserRole responderRole = UserRole.ADMIN;
        String content = "We are looking into your issue";
        LocalDateTime createdAt = LocalDateTime.now();

        ReportResponse response = new ReportResponse(id, reportId, responderId, responderRole, content, createdAt);

        assertEquals(id, response.getId());
        assertEquals(reportId, response.getReportId());
        assertEquals(responderId, response.getResponderId());
        assertEquals(responderRole, response.getResponderRole());
        assertEquals(content, response.getContent());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    public void testContentValidation() {
        String longContent = "0123456789".repeat(51);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> new ReportResponse(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UserRole.ORGANIZER,
                        longContent,
                        LocalDateTime.now()
                )
        );

        assertTrue(exception.getMessage().contains("exceed"));
    }
}
