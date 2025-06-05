package id.ac.ui.cs.advprog.eventsphere.report.dto.request;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class CreateReportRequestTest {

    @Test
    @DisplayName("Memeriksa fungsi setter dan getter untuk CreateReportRequest tanpa event")
    public void testCreateReportRequestSettersAndGetters_WithoutEvent() {
        // Arrange
        CreateReportRequest dto = new CreateReportRequest();
        Long userId = 1L;
        String userEmail = "user@example.com";

        // Act
        dto.setUserId(userId);
        dto.setUserEmail(userEmail);
        dto.setCategory(ReportCategory.PAYMENT);
        dto.setDescription("Test description");

        // Assert
        assertEquals(userId, dto.getUserId());
        assertEquals(userEmail, dto.getUserEmail());
        assertEquals(ReportCategory.PAYMENT, dto.getCategory());
        assertEquals("Test description", dto.getDescription());
        assertNull(dto.getEventId());
        assertNull(dto.getEventTitle());
    }

    @Test
    @DisplayName("Memeriksa fungsi setter dan getter untuk CreateReportRequest dengan event")
    public void testCreateReportRequestSettersAndGetters_WithEvent() {
        // Arrange
        CreateReportRequest dto = new CreateReportRequest();
        Long userId = 1L;
        String userEmail = "user@example.com";
        Long eventId = 10L;
        String eventTitle = "Annual Conference 2024";

        // Act
        dto.setUserId(userId);
        dto.setUserEmail(userEmail);
        dto.setEventId(eventId);
        dto.setEventTitle(eventTitle);
        dto.setCategory(ReportCategory.EVENT);
        dto.setDescription("Event-related issue");

        // Assert
        assertEquals(userId, dto.getUserId());
        assertEquals(userEmail, dto.getUserEmail());
        assertEquals(eventId, dto.getEventId());
        assertEquals(eventTitle, dto.getEventTitle());
        assertEquals(ReportCategory.EVENT, dto.getCategory());
        assertEquals("Event-related issue", dto.getDescription());
    }

    @Test
    @DisplayName("Memeriksa semua kategori laporan dalam CreateReportRequest")
    public void testCreateReportRequest_AllCategories() {
        // Test all ReportCategory enum values
        CreateReportRequest dto = new CreateReportRequest();

        // Test PAYMENT category
        dto.setCategory(ReportCategory.PAYMENT);
        assertEquals(ReportCategory.PAYMENT, dto.getCategory());

        // Test TICKET category
        dto.setCategory(ReportCategory.TICKET);
        assertEquals(ReportCategory.TICKET, dto.getCategory());

        // Test EVENT category
        dto.setCategory(ReportCategory.EVENT);
        assertEquals(ReportCategory.EVENT, dto.getCategory());

        // Test OTHER category
        dto.setCategory(ReportCategory.OTHER);
        assertEquals(ReportCategory.OTHER, dto.getCategory());
    }
}