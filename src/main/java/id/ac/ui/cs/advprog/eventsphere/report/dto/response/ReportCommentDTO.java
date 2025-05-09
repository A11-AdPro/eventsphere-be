package id.ac.ui.cs.advprog.eventsphere.report.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReportCommentDTO {
    private UUID id;
    private UUID reportId;
    private UUID responderId;
    private String responderRole;
    private String message;
    private LocalDateTime createdAt;
}