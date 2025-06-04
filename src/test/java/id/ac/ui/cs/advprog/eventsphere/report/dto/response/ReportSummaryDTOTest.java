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
    @DisplayName("Memeriksa fungsi setter dan getter untuk ReportSummaryDTO tanpa event")
    public void testReportSummaryDTOSettersAndGetters_WithoutEvent() {
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
        assertNull(dto.getEventId());
        assertNull(dto.getEventTitle());
        assertEquals(ReportCategory.PAYMENT, dto.getCategory());
        assertEquals(ReportStatus.PENDING, dto.getStatus());
        assertEquals("Short description...", dto.getShortDescription());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(5, dto.getCommentCount());
    }

    @Test
    @DisplayName("Memeriksa fungsi setter dan getter untuk ReportSummaryDTO dengan event")
    public void testReportSummaryDTOSettersAndGetters_WithEvent() {
        // Arrange
        UUID id = UUID.randomUUID();
        Long userId = 1L;
        String userEmail = "user@example.com";
        Long eventId = 10L;
        String eventTitle = "Annual Tech Conference 2024";
        LocalDateTime now = LocalDateTime.now();

        ReportSummaryDTO dto = new ReportSummaryDTO();

        // Act
        dto.setId(id);
        dto.setUserId(userId);
        dto.setUserEmail(userEmail);
        dto.setEventId(eventId);
        dto.setEventTitle(eventTitle);
        dto.setCategory(ReportCategory.EVENT);
        dto.setStatus(ReportStatus.ON_PROGRESS);
        dto.setShortDescription("Event issue summary...");
        dto.setCreatedAt(now);
        dto.setCommentCount(3);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(userId, dto.getUserId());
        assertEquals(userEmail, dto.getUserEmail());
        assertEquals(eventId, dto.getEventId());
        assertEquals(eventTitle, dto.getEventTitle());
        assertEquals(ReportCategory.EVENT, dto.getCategory());
        assertEquals(ReportStatus.ON_PROGRESS, dto.getStatus());
        assertEquals("Event issue summary...", dto.getShortDescription());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(3, dto.getCommentCount());
    }

    @Test
    @DisplayName("Memeriksa ReportSummaryDTO dengan semua kategori dan status")
    public void testReportSummaryDTO_AllCategoriesAndStatuses() {
        // Test all ReportCategory values
        ReportSummaryDTO dto = new ReportSummaryDTO();

        // Test all categories
        dto.setCategory(ReportCategory.PAYMENT);
        assertEquals(ReportCategory.PAYMENT, dto.getCategory());

        dto.setCategory(ReportCategory.TICKET);
        assertEquals(ReportCategory.TICKET, dto.getCategory());

        dto.setCategory(ReportCategory.EVENT);
        assertEquals(ReportCategory.EVENT, dto.getCategory());

        dto.setCategory(ReportCategory.OTHER);
        assertEquals(ReportCategory.OTHER, dto.getCategory());

        // Test all statuses
        dto.setStatus(ReportStatus.PENDING);
        assertEquals(ReportStatus.PENDING, dto.getStatus());

        dto.setStatus(ReportStatus.ON_PROGRESS);
        assertEquals(ReportStatus.ON_PROGRESS, dto.getStatus());

        dto.setStatus(ReportStatus.RESOLVED);
        assertEquals(ReportStatus.RESOLVED, dto.getStatus());
    }
}