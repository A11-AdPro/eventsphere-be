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
        UUID responderId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        ReportCommentDTO dto = new ReportCommentDTO();
        dto.setId(id);
        dto.setReportId(reportId);
        dto.setResponderId(responderId);
        dto.setResponderRole("ADMIN");
        dto.setMessage("Test message");
        dto.setCreatedAt(now);

        assertEquals(id, dto.getId());
        assertEquals(reportId, dto.getReportId());
        assertEquals(responderId, dto.getResponderId());
        assertEquals("ADMIN", dto.getResponderRole());
        assertEquals("Test message", dto.getMessage());
        assertEquals(now, dto.getCreatedAt());
    }
}