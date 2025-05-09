package id.ac.ui.cs.advprog.eventsphere.report.dto.request;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateReportRequest {
    private UUID userId;
    private ReportCategory category;
    private String description;
}