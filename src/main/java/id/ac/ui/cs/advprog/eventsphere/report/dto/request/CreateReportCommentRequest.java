package id.ac.ui.cs.advprog.eventsphere.report.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateReportCommentRequest {
    private UUID responderId;
    private String responderRole;
    private String message;
}