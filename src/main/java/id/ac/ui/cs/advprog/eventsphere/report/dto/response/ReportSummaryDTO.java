package id.ac.ui.cs.advprog.eventsphere.report.dto.response;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReportSummaryDTO {
    private UUID id;
    private Long userId;
    private String userEmail;
    private Long eventId;
    private String eventTitle;
    private ReportCategory category;
    private ReportStatus status;
    private String shortDescription;
    private LocalDateTime createdAt;
    private int commentCount;
}