package id.ac.ui.cs.advprog.eventsphere.report.dto.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CreateReportCommentRequestTest {

    @Test
    public void testCreateReportCommentRequestSettersAndGetters() {
        CreateReportCommentRequest dto = new CreateReportCommentRequest();
        Long responderId = 1L;
        String responderEmail = "admin@example.com";

        dto.setResponderId(responderId);
        dto.setResponderEmail(responderEmail);
        dto.setResponderRole("ADMIN");
        dto.setMessage("Test message");

        assertEquals(responderId, dto.getResponderId());
        assertEquals(responderEmail, dto.getResponderEmail());
        assertEquals("ADMIN", dto.getResponderRole());
        assertEquals("Test message", dto.getMessage());
    }
}