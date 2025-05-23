package id.ac.ui.cs.advprog.eventsphere.report.controller;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.AuthService;
import id.ac.ui.cs.advprog.eventsphere.report.dto.request.CreateReportCommentRequest;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportCommentDTO;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportSummaryDTO;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final ReportService reportService;
    private final AuthService authService;

    @Autowired
    public AdminReportController(ReportService reportService, AuthService authService) {
        this.reportService = reportService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<ReportSummaryDTO>> getReports(@RequestParam(required = false) ReportStatus status) {
        List<ReportSummaryDTO> reports = reportService.getReportsByStatus(status);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponseDTO> getReportById(@PathVariable UUID id) {
        ReportResponseDTO report = reportService.getReportById(id);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/{reportId}/comments")
    public ResponseEntity<ReportCommentDTO> addComment(
            @PathVariable UUID reportId,
            @RequestBody CreateReportCommentRequest commentRequest) {

        User currentUser = authService.getCurrentUser();
        commentRequest.setResponderId(currentUser.getId());
        commentRequest.setResponderRole("ADMIN");

        ReportCommentDTO comment = reportService.addComment(reportId, commentRequest);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReportResponseDTO> updateReportStatus(
            @PathVariable UUID id,
            @RequestParam ReportStatus status) {
        ReportResponseDTO updatedReport = reportService.updateReportStatus(id, status);
        return ResponseEntity.ok(updatedReport);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable UUID id) {
        reportService.deleteReport(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}