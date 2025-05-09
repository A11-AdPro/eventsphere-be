// File: src/main/java/id/ac/ui/cs/advprog/eventsphere/report/controller/AttendeeReportController.java
package id.ac.ui.cs.advprog.eventsphere.report.controller;

import id.ac.ui.cs.advprog.eventsphere.report.dto.request.CreateReportCommentRequest;
import id.ac.ui.cs.advprog.eventsphere.report.dto.request.CreateReportRequest;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportCommentDTO;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportSummaryDTO;
import id.ac.ui.cs.advprog.eventsphere.report.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendee/reports")
public class AttendeeReportController {

    private final ReportService reportService;

    @Autowired
    public AttendeeReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportResponseDTO> createReport(
            @RequestPart("request") CreateReportRequest createRequest,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments)
            throws IOException {
        ReportResponseDTO createdReport = reportService.createReport(createRequest, attachments);
        return new ResponseEntity<>(createdReport, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ReportSummaryDTO>> getReportsByUserId(@RequestParam UUID userId) {
        List<ReportSummaryDTO> reports = reportService.getReportsByUserId(userId);
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
}