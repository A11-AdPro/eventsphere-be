package id.ac.ui.cs.advprog.eventsphere.report.controller;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.AuthService;
import id.ac.ui.cs.advprog.eventsphere.report.dto.request.CreateReportCommentRequest;
import id.ac.ui.cs.advprog.eventsphere.report.dto.request.CreateReportRequest;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportCommentDTO;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportSummaryDTO;
import id.ac.ui.cs.advprog.eventsphere.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendee/reports")
public class AttendeeReportController {

    private final ReportService reportService;
    private final AuthService authService;

    @Autowired
    public AttendeeReportController(ReportService reportService, AuthService authService) {
        this.reportService = reportService;
        this.authService = authService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<ReportResponseDTO> createReport(@RequestBody CreateReportRequest createRequest) {

        User currentUser = authService.getCurrentUser();

        createRequest.setUserId(currentUser.getId());
        createRequest.setUserEmail(currentUser.getEmail());

        ReportResponseDTO createdReport = reportService.createReport(createRequest);
        return new ResponseEntity<>(createdReport, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<List<ReportSummaryDTO>> getMyReports() {
        User currentUser = authService.getCurrentUser();
        List<ReportSummaryDTO> reports = reportService.getReportsByUserEmail(currentUser.getEmail());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<ReportResponseDTO> getReportById(@PathVariable UUID id) {
        ReportResponseDTO report = reportService.getReportById(id);

        User currentUser = authService.getCurrentUser();
        if (!report.getUserEmail().equals(currentUser.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(report);
    }

    @PostMapping("/{reportId}/comments")
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<ReportCommentDTO> addComment(
            @PathVariable UUID reportId,
            @RequestBody CreateReportCommentRequest commentRequest) {

        User currentUser = authService.getCurrentUser();

        ReportResponseDTO report = reportService.getReportById(reportId);
        if (!report.getUserEmail().equals(currentUser.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        commentRequest.setResponderId(currentUser.getId());
        commentRequest.setResponderRole("ATTENDEE");
        commentRequest.setResponderEmail(currentUser.getEmail());

        ReportCommentDTO comment = reportService.addComment(reportId, commentRequest);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }
}