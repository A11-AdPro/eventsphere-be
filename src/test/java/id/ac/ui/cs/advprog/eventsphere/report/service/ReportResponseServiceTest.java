package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.model.*;
import id.ac.ui.cs.advprog.eventsphere.report.observer.ReportSubject;
import id.ac.ui.cs.advprog.eventsphere.report.repository.ReportRepository;
import id.ac.ui.cs.advprog.eventsphere.report.repository.ReportResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReportResponseServiceTest {

    private ReportResponseRepository responseRepository;
    private ReportRepository reportRepository;
    private ReportSubject reportSubject;
    private ReportResponseService responseService;

    private UUID reportId;
    private UUID responderId;
    private Report testReport;

    @BeforeEach
    public void setUp() {
        responseRepository = Mockito.mock(ReportResponseRepository.class);
        reportRepository = Mockito.mock(ReportRepository.class);
        reportSubject = Mockito.mock(ReportSubject.class);
        responseService = new ReportResponseServiceImpl(responseRepository, reportRepository, reportSubject);

        reportId = UUID.randomUUID();
        responderId = UUID.randomUUID();
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
    public void testAddResponse() {
        String content = "We are looking into your issue";

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(testReport));
        when(responseRepository.save(any(ReportResponse.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportResponse addedResponse = responseService.addResponse(reportId, responderId, UserRole.ADMIN, content);

        assertNotNull(addedResponse);
        assertEquals(reportId, addedResponse.getReportId());
        assertEquals(responderId, addedResponse.getResponderId());
        assertEquals(UserRole.ADMIN, addedResponse.getResponderRole());
        assertEquals(content, addedResponse.getContent());

        verify(responseRepository, times(1)).save(any(ReportResponse.class));
        verify(reportSubject, times(1)).notifyNewResponse(eq(testReport), eq(content));
    }

    @Test
    public void testAddResponseWithInvalidReportId() {
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> responseService.addResponse(reportId, responderId, UserRole.ADMIN, "Test content"));

        verify(responseRepository, never()).save(any(ReportResponse.class));
        verify(reportSubject, never()).notifyNewResponse(any(Report.class), anyString());
    }

    @Test
    public void testGetResponsesByReportId() {
        ReportResponse response = new ReportResponse(
                UUID.randomUUID(),
                reportId,
                responderId,
                UserRole.ADMIN,
                "Test response",
                LocalDateTime.now()
        );

        when(responseRepository.findByReportId(reportId)).thenReturn(List.of(response));

        List<ReportResponse> responses = responseService.getResponsesByReportId(reportId);

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals(reportId, responses.getFirst().getReportId());
    }
}
