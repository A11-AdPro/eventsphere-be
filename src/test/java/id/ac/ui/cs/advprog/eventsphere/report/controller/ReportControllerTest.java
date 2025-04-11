package id.ac.ui.cs.advprog.eventsphere.report.controller;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportType;
import id.ac.ui.cs.advprog.eventsphere.report.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReportControllerTest {

    private ReportService reportService;
    private ReportController reportController;
    private UUID reportId;
    private UUID attendeeId;
    private Report report;

    @BeforeEach
    public void setUp() {
        reportService = mock(ReportService.class);
        reportController = new ReportController(reportService);
        reportId = UUID.randomUUID();
        attendeeId = UUID.randomUUID();

        report = new Report(
                reportId,
                attendeeId,
                "Test Title",
                "Test Description",
                ReportType.PAYMENT,
                ReportStatus.PENDING,
                LocalDateTime.now()
        );
    }

    @Test
    public void testCreateReport() {
        when(reportService.createReport(any(), anyString(), anyString(), any())).thenReturn(report);

        ResponseEntity<Report> response = reportController.createReport(report);

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(reportId, response.getBody().getId());
        verify(reportService, times(1)).createReport(any(), any(), any(), any());
    }

    @Test
    public void testGetReportById_found() {
        when(reportService.getReportById(reportId)).thenReturn(Optional.of(report));

        ResponseEntity<Report> response = reportController.getReportById(reportId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(reportId, response.getBody().getId());
    }

    @Test
    public void testGetReportById_notFound() {
        when(reportService.getReportById(reportId)).thenReturn(Optional.empty());

        ResponseEntity<Report> response = reportController.getReportById(reportId);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    public void testGetReportsByAttendeeId() {
        when(reportService.getReportsByAttendeeId(attendeeId)).thenReturn(List.of(report));

        ResponseEntity<List<Report>> response = reportController.getReportsByAttendeeId(attendeeId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }

    @Test
    public void testUpdateReportStatus_success() {
        when(reportService.updateReportStatus(reportId, ReportStatus.RESOLVED)).thenReturn(true);

        report.setStatus(ReportStatus.RESOLVED);
        ResponseEntity<Void> response = reportController.updateReportStatus(reportId, report);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testUpdateReportStatus_fail() {
        when(reportService.updateReportStatus(reportId, ReportStatus.RESOLVED)).thenReturn(false);

        report.setStatus(ReportStatus.RESOLVED);
        ResponseEntity<Void> response = reportController.updateReportStatus(reportId, report);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void testDeleteReport_success() {
        when(reportService.deleteReport(reportId)).thenReturn(true);

        ResponseEntity<Void> response = reportController.deleteReport(reportId);

        assertEquals(204, response.getStatusCode().value());
    }

    @Test
    public void testDeleteReport_notFound() {
        when(reportService.deleteReport(reportId)).thenReturn(false);

        ResponseEntity<Void> response = reportController.deleteReport(reportId);

        assertEquals(404, response.getStatusCode().value());
    }
}
