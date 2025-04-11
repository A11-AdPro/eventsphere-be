package id.ac.ui.cs.advprog.eventsphere.report.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ReportTest {

    @Test
    public void testCreateReport() {
        UUID id = UUID.randomUUID();
        UUID attendeeId = UUID.randomUUID();
        String title = "Payment Issue";
        String description = "I was charged twice for my ticket";
        ReportType type = ReportType.PAYMENT;
        ReportStatus status = ReportStatus.PENDING;
        LocalDateTime createdAt = LocalDateTime.now();

        Report report = new Report(id, attendeeId, title, description, type, status, createdAt);

        assertEquals(id, report.getId());
        assertEquals(attendeeId, report.getAttendeeId());
        assertEquals(title, report.getTitle());
        assertEquals(description, report.getDescription());
        assertEquals(type, report.getType());
        assertEquals(status, report.getStatus());
        assertEquals(createdAt, report.getCreatedAt());
    }

    @Test
    public void testUpdateStatus() {
        Report report = new Report(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Ticket Issue",
                "Cannot download my ticket",
                ReportType.TICKET,
                ReportStatus.PENDING,
                LocalDateTime.now()
        );

        report.setStatus(ReportStatus.ON_PROGRESS);
        assertEquals(ReportStatus.ON_PROGRESS, report.getStatus());

        report.setStatus(ReportStatus.RESOLVED);
        assertEquals(ReportStatus.RESOLVED, report.getStatus());
    }
}
