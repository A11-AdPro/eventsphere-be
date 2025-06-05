package id.ac.ui.cs.advprog.eventsphere.report.dto.request;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import lombok.Data;

@Data
public class CreateReportRequest {
    private Long userId;
    private String userEmail;
    private Long eventId;
    private String eventTitle;
    private ReportCategory category;
    private String description;
}