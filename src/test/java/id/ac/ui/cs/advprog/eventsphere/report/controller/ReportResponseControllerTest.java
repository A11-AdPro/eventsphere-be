package id.ac.ui.cs.advprog.eventsphere.report.controller;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.UserRole;
import id.ac.ui.cs.advprog.eventsphere.report.service.ReportResponseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReportResponseControllerTest {

    private ReportResponseService responseService;
    private ReportResponseController responseController;

    @BeforeEach
    public void setUp() {
        responseService = mock(ReportResponseService.class);
        responseController = new ReportResponseController(responseService);
    }

    @Test
    public void testAddResponse_success() {
        UUID reportId = UUID.randomUUID();
        UUID responderId = UUID.randomUUID();
        ReportResponse response = new ReportResponse(
                UUID.randomUUID(),
                reportId,
                responderId,
                UserRole.ADMIN,
                "This is a response",
                LocalDateTime.now()
        );

        when(responseService.addResponse(
                eq(reportId),
                eq(responderId),
                eq(UserRole.ADMIN),
                eq("This is a response")
        )).thenReturn(response);

        ResponseEntity<ReportResponse> result = responseController.addResponse(reportId, response);

        assertEquals(201, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(reportId, result.getBody().getReportId());
    }

    @Test
    public void testAddResponse_badRequest() {
        UUID reportId = UUID.randomUUID();
        ReportResponse invalidResponse = new ReportResponse(
                UUID.randomUUID(),
                reportId,
                null, // Simulate bad input
                UserRole.ADMIN,
                "Invalid",
                LocalDateTime.now()
        );

        when(responseService.addResponse(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid input"));

        ResponseEntity<ReportResponse> result = responseController.addResponse(reportId, invalidResponse);

        assertEquals(400, result.getStatusCode().value());
        assertNull(result.getBody());
    }

    @Test
    public void testGetResponsesByReportId() {
        UUID reportId = UUID.randomUUID();
        UUID responderId = UUID.randomUUID();
        ReportResponse response = new ReportResponse(
                UUID.randomUUID(),
                reportId,
                responderId,
                UserRole.ADMIN,
                "Sample response",
                LocalDateTime.now()
        );

        when(responseService.getResponsesByReportId(reportId)).thenReturn(List.of(response));

        ResponseEntity<List<ReportResponse>> result = responseController.getResponsesByReportId(reportId);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isEmpty());
        assertEquals(reportId, result.getBody().getFirst().getReportId());
    }
}
