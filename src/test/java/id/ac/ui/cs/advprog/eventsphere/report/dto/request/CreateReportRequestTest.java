package id.ac.ui.cs.advprog.eventsphere.report.dto.request;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class CreateReportRequestTest {

    @Test
    @DisplayName("Memeriksa fungsi setter dan getter untuk CreateReportRequest")
    public void testCreateReportRequestSettersAndGetters() {
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
    }
}