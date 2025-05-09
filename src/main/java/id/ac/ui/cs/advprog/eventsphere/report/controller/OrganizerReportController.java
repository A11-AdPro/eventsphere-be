package id.ac.ui.cs.advprog.eventsphere.report.controller;

import id.ac.ui.cs.advprog.eventsphere.report.dto.request.CreateReportCommentRequest;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportCommentDTO;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportSummaryDTO;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizer/reports")
public class OrganizerReportController {

    private final ReportService reportService;

    @Autowired
    public OrganizerReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<List<ReportSummaryDTO>> getReportsByStatus(@RequestParam(required = false) ReportStatus status) {
        List<ReportSummaryDTO> reports;
        if (status != null) {
            reports = reportService.getReportsByStatus(status);
        } else {
            // Default to PENDING if no status is provided
            reports = reportService.getReportsByStatus(ReportStatus.PENDING);
        }
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
}