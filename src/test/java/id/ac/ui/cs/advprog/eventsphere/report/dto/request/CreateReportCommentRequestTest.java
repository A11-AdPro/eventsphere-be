package id.ac.ui.cs.advprog.eventsphere.report.dto.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class CreateReportCommentRequestTest {

    @Test
    @DisplayName("Memeriksa fungsi setter dan getter untuk CreateReportCommentRequest")
    public void testCreateReportCommentRequestSettersAndGetters() {
        // Arrange
        CreateReportCommentRequest dto = new CreateReportCommentRequest();
        Long responderId = 1L;
        String responderEmail = "admin@example.com";

        // Act
        dto.setResponderId(responderId);
        dto.setResponderEmail(responderEmail);
        dto.setResponderRole("ADMIN");
        dto.setMessage("Test message");

        // Assert
        assertEquals(responderId, dto.getResponderId());
        assertEquals(responderEmail, dto.getResponderEmail());
        assertEquals("ADMIN", dto.getResponderRole());
        assertEquals("Test message", dto.getMessage());
    }
}