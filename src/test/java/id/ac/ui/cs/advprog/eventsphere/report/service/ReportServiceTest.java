package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportType;
import id.ac.ui.cs.advprog.eventsphere.report.observer.ReportSubject;
import id.ac.ui.cs.advprog.eventsphere.report.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReportServiceTest {

    private ReportRepository reportRepository;
    private ReportSubject reportSubject;
    private ReportService reportService;
    private UUID testReportId;
    private UUID testAttendeeId;
    private Report testReport;

    @BeforeEach
    public void setUp() {
        reportRepository = Mockito.mock(ReportRepository.class);
        reportSubject = Mockito.mock(ReportSubject.class);
        reportService = new ReportServiceImpl(reportRepository, reportSubject);

        testReportId = UUID.randomUUID();
        testAttendeeId = UUID.randomUUID();
        testReport = new Report(
                testReportId,
                testAttendeeId,
                "Test Report",
                "Test Description",
                ReportType.PAYMENT,
                ReportStatus.PENDING,
                LocalDateTime.now()
        );
    }

    @Test
    public void testCreateReport() {
        when(reportRepository.save(any(Report.class))).thenReturn(testReport);

        Report createdReport = reportService.createReport(
                testAttendeeId,
                "Test Report",
                "Test Description",
                ReportType.PAYMENT
        );

        assertNotNull(createdReport);
        assertEquals("Test Report", createdReport.getTitle());
        assertEquals(ReportStatus.PENDING, createdReport.getStatus());
        verify(reportRepository, times(1)).save(any(Report.class));
        verify(reportSubject, times(1)).notifyNewReport(any(Report.class));
    }

    @Test
    public void testGetReportById() {
        when(reportRepository.findById(testReportId)).thenReturn(Optional.of(testReport));

        Optional<Report> foundReport = reportService.getReportById(testReportId);

        assertTrue(foundReport.isPresent());
        assertEquals(testReportId, foundReport.get().getId());
    }

    @Test
    public void testGetReportsByAttendeeId() {
        when(reportRepository.findByAttendeeId(testAttendeeId))
                .thenReturn(Collections.singletonList(testReport));

        List<Report> reports = reportService.getReportsByAttendeeId(testAttendeeId);

        assertFalse(reports.isEmpty());
        assertEquals(1, reports.size());
        assertEquals(testAttendeeId, reports.getFirst().getAttendeeId());
    }

    @Test
    public void testUpdateReportStatus() {
        Report updatedReport = new Report(
                testReportId,
                testAttendeeId,
                "Test Report",
                "Test Description",
                ReportType.PAYMENT,
                ReportStatus.ON_PROGRESS,
                testReport.getCreatedAt()
        );

        when(reportRepository.findById(testReportId)).thenReturn(Optional.of(testReport));
        when(reportRepository.update(any(Report.class))).thenReturn(updatedReport);

        boolean result = reportService.updateReportStatus(testReportId, ReportStatus.ON_PROGRESS);

        assertTrue(result);
        verify(reportRepository, times(1)).update(any(Report.class));
        verify(reportSubject, times(1)).notifyStatusChange(any(Report.class), eq(ReportStatus.ON_PROGRESS));
    }

    @Test
    public void testUpdateReportStatusWithInvalidId() {
        when(reportRepository.findById(testReportId)).thenReturn(Optional.empty());

        boolean result = reportService.updateReportStatus(testReportId, ReportStatus.ON_PROGRESS);

        assertFalse(result);
        verify(reportRepository, never()).update(any(Report.class));
        verify(reportSubject, never()).notifyStatusChange(any(Report.class), any(ReportStatus.class));
    }

    @Test
    public void testDeleteReport() {
        when(reportRepository.findById(testReportId)).thenReturn(Optional.of(testReport));
        doNothing().when(reportRepository).delete(testReportId);

        boolean result = reportService.deleteReport(testReportId);

        assertTrue(result);
        verify(reportRepository, times(1)).delete(testReportId);
    }

    @Test
    public void testDeleteReportWithInvalidId() {
        when(reportRepository.findById(testReportId)).thenReturn(Optional.empty());

        boolean result = reportService.deleteReport(testReportId);

        assertFalse(result);
        verify(reportRepository, never()).delete(testReportId);
    }
}
