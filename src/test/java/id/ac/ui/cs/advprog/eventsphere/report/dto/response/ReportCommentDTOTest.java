package id.ac.ui.cs.advprog.eventsphere.report.dto.response;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class ReportCommentDTOTest {

    @Test
    public void testReportCommentDTOSettersAndGetters() {
        UUID id = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();
        Long responderId = 1L;
        String responderEmail = "admin@example.com";
        LocalDateTime now = LocalDateTime.now();

        ReportCommentDTO dto = new ReportCommentDTO();
        dto.setId(id);
        dto.setReportId(reportId);
        dto.setResponderId(responderId);
        dto.setResponderEmail(responderEmail);
        dto.setResponderRole("ADMIN");
        dto.setMessage("Test message");
        dto.setCreatedAt(now);

        assertEquals(id, dto.getId());
        assertEquals(reportId, dto.getReportId());
        assertEquals(responderId, dto.getResponderId());
        assertEquals(responderEmail, dto.getResponderEmail());
        assertEquals("ADMIN", dto.getResponderRole());
        assertEquals("Test message", dto.getMessage());
        assertEquals(now, dto.getCreatedAt());
    }
}