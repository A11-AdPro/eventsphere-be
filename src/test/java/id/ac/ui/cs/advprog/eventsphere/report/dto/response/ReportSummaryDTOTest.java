package id.ac.ui.cs.advprog.eventsphere.report.dto.response;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class ReportSummaryDTOTest {

    @Test
    @DisplayName("Memeriksa fungsi setter dan getter untuk ReportSummaryDTO")
    public void testReportSummaryDTOSettersAndGetters() {
        // Arrange
        UUID id = UUID.randomUUID();
        Long userId = 1L;
        String userEmail = "user@example.com";
        LocalDateTime now = LocalDateTime.now();

        ReportSummaryDTO dto = new ReportSummaryDTO();

        // Act
        dto.setId(id);
        dto.setUserId(userId);
        dto.setUserEmail(userEmail);
        dto.setCategory(ReportCategory.PAYMENT);
        dto.setStatus(ReportStatus.PENDING);
        dto.setShortDescription("Short description...");
        dto.setCreatedAt(now);
        dto.setCommentCount(5);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(userId, dto.getUserId());
        assertEquals(userEmail, dto.getUserEmail());
        assertEquals(ReportCategory.PAYMENT, dto.getCategory());
        assertEquals(ReportStatus.PENDING, dto.getStatus());
        assertEquals("Short description...", dto.getShortDescription());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(5, dto.getCommentCount());
    }
}