package id.ac.ui.cs.advprog.eventsphere.report.dto.request;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class CreateReportCommentRequestTest {

    @Test
    public void testCreateReportCommentRequestSettersAndGetters() {
        CreateReportCommentRequest dto = new CreateReportCommentRequest();
        UUID responderId = UUID.randomUUID();

        dto.setResponderId(responderId);
        dto.setResponderRole("ADMIN");
        dto.setMessage("Test message");

        assertEquals(responderId, dto.getResponderId());
        assertEquals("ADMIN", dto.getResponderRole());
        assertEquals("Test message", dto.getMessage());
    }
}
