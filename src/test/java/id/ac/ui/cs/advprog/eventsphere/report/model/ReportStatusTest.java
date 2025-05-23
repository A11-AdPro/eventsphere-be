package id.ac.ui.cs.advprog.eventsphere.report.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

public class ReportStatusTest {

    @Test
    @DisplayName("Memverifikasi nilai enum yang benar untuk status laporan")
    public void testEnumValues() {
        assertEquals("PENDING", ReportStatus.PENDING.name());
        assertEquals("ON_PROGRESS", ReportStatus.ON_PROGRESS.name());
        assertEquals("RESOLVED", ReportStatus.RESOLVED.name());
    }

    @Test
    @DisplayName("Memverifikasi nama tampilan yang benar untuk status laporan")
    public void testGetDisplayName() {
        assertEquals("Pending", ReportStatus.PENDING.getDisplayName());
        assertEquals("On Progress", ReportStatus.ON_PROGRESS.getDisplayName());
        assertEquals("Resolved", ReportStatus.RESOLVED.getDisplayName());
    }
}
