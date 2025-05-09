package id.ac.ui.cs.advprog.eventsphere.report.controller;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.service.ReportResponseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports/{reportId}/responses")
public class ReportResponseController {

    private final ReportResponseService responseService;

    public ReportResponseController(ReportResponseService responseService) {
        this.responseService = responseService;
    }

    @PostMapping
    public ResponseEntity<ReportResponse> addResponse(
            @PathVariable UUID reportId,
            @RequestBody ReportResponse request) {
        try {
            ReportResponse response = responseService.addResponse(
                    reportId,
                    request.getResponderId(),
                    request.getResponderRole(),
                    request.getContent()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> getResponsesByReportId(@PathVariable UUID reportId) {
        List<ReportResponse> responses = responseService.getResponsesByReportId(reportId);
        return ResponseEntity.ok(responses);
    }
}
