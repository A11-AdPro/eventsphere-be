package id.ac.ui.cs.advprog.eventsphere.report.dto.request;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class CreateReportRequestTest {

    @Test
    public void testCreateReportRequestSettersAndGetters() {
        CreateReportRequest dto = new CreateReportRequest();
        UUID userId = UUID.randomUUID();

        dto.setUserId(userId);
        dto.setCategory(ReportCategory.PAYMENT);
        dto.setDescription("Test description");

        assertEquals(userId, dto.getUserId());
        assertEquals(ReportCategory.PAYMENT, dto.getCategory());
        assertEquals("Test description", dto.getDescription());
    }
}