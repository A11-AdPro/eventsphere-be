package id.ac.ui.cs.advprog.eventsphere.report.dto.request;

import lombok.Data;

@Data
public class CreateReportCommentRequest {
    private Long responderId;
    private String responderRole;
    private String message;
}