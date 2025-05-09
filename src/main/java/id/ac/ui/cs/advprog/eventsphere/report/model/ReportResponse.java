package id.ac.ui.cs.advprog.eventsphere.report.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ReportResponse {
    private static final int MAX_CONTENT_LENGTH = 500;

    private UUID id;
    private UUID reportId;
    private UUID responderId;
    private UserRole responderRole;
    private String content;
    private LocalDateTime createdAt;

    public ReportResponse(UUID id, UUID reportId, UUID responderId, UserRole responderRole, String content, LocalDateTime createdAt) {
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Response content cannot be empty");
        }

        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("Response content cannot exceed " + MAX_CONTENT_LENGTH + " characters");
        }

        this.id = id;
        this.reportId = reportId;
        this.responderId = responderId;
        this.responderRole = responderRole;
        this.content = content;
        this.createdAt = createdAt;
    }
}
