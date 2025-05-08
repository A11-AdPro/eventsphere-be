package id.ac.ui.cs.advprog.eventsphere.report.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReportStatusTest {

    @Test
    public void testEnumValues() {
        assertEquals("PENDING", ReportStatus.PENDING.name());
        assertEquals("ON_PROGRESS", ReportStatus.ON_PROGRESS.name());
        assertEquals("RESOLVED", ReportStatus.RESOLVED.name());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Pending", ReportStatus.PENDING.getDisplayName());
        assertEquals("On Progress", ReportStatus.ON_PROGRESS.getDisplayName());
        assertEquals("Resolved", ReportStatus.RESOLVED.getDisplayName());
    }
}
