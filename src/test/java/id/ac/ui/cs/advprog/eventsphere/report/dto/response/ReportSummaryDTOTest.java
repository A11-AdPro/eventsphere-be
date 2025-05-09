package id.ac.ui.cs.advprog.eventsphere.report.dto.response;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class ReportSummaryDTOTest {

    @Test
    public void testReportSummaryDTOSettersAndGetters() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        ReportSummaryDTO dto = new ReportSummaryDTO();
        dto.setId(id);
        dto.setCategory(ReportCategory.PAYMENT);
        dto.setStatus(ReportStatus.PENDING);
        dto.setShortDescription("Short description...");
        dto.setCreatedAt(now);
        dto.setCommentCount(5);

        assertEquals(id, dto.getId());
        assertEquals(ReportCategory.PAYMENT, dto.getCategory());
        assertEquals(ReportStatus.PENDING, dto.getStatus());
        assertEquals("Short description...", dto.getShortDescription());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(5, dto.getCommentCount());
    }
}
