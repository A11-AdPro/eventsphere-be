package id.ac.ui.cs.advprog.eventsphere.report.repository;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ReportRepositoryTest {

    private ReportRepository reportRepository;
    private Report testReport;
    private UUID reportId;

    @BeforeEach
    public void setUp() {
        reportRepository = new InMemoryReportRepository();
        reportId = UUID.randomUUID();
        testReport = new Report(
                reportId,
                UUID.randomUUID(),
                "Test Report",
                "Test Description",
                ReportType.PAYMENT,
                ReportStatus.PENDING,
                LocalDateTime.now()
        );
    }

    @Test
    public void testSaveAndFindById() {
        reportRepository.save(testReport);
        Optional<Report> foundReport = reportRepository.findById(reportId);

        assertTrue(foundReport.isPresent());
        assertEquals(testReport.getId(), foundReport.get().getId());
        assertEquals(testReport.getTitle(), foundReport.get().getTitle());
    }

    @Test
    public void testFindByAttendeeId() {
        UUID attendeeId = testReport.getAttendeeId();
        reportRepository.save(testReport);

        List<Report> reports = reportRepository.findByAttendeeId(attendeeId);

        assertFalse(reports.isEmpty());
        Report firstReport = reports.stream().findFirst().orElseThrow();
        assertEquals(attendeeId, firstReport.getAttendeeId());
    }

    @Test
    public void testFindByStatus() {
        reportRepository.save(testReport);

        List<Report> pendingReports = reportRepository.findByStatus(ReportStatus.PENDING);
        List<Report> resolvedReports = reportRepository.findByStatus(ReportStatus.RESOLVED);

        assertFalse(pendingReports.isEmpty());
        assertEquals(1, pendingReports.size());
        assertTrue(resolvedReports.isEmpty());
    }

    @Test
    public void testUpdate() {
        reportRepository.save(testReport);

        Report updatedReport = new Report(
                reportId,
                testReport.getAttendeeId(),
                testReport.getTitle(),
                testReport.getDescription(),
                testReport.getType(),
                ReportStatus.ON_PROGRESS,
                testReport.getCreatedAt()
        );

        reportRepository.update(updatedReport);
        Optional<Report> foundReport = reportRepository.findById(reportId);

        assertTrue(foundReport.isPresent());
        assertEquals(ReportStatus.ON_PROGRESS, foundReport.get().getStatus());
    }

    @Test
    public void testDelete() {
        reportRepository.save(testReport);
        reportRepository.delete(reportId);

        Optional<Report> deletedReport = reportRepository.findById(reportId);
        assertTrue(deletedReport.isEmpty());
    }
}
