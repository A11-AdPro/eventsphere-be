package id.ac.ui.cs.advprog.eventsphere.report.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_responses")
@Data
@NoArgsConstructor
public class ReportResponse {

    private static final int MAX_MESSAGE_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(name = "responder_id", nullable = false)
    private Long responderId;

    @Column(name = "responder_email")
    private String responderEmail;

    @Column(name = "responder_role", nullable = false)
    private String responderRole;

    @Column(nullable = false, length = MAX_MESSAGE_LENGTH)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ReportResponse(Long responderId, String responderEmail, String responderRole, String message, Report report) {
        this.responderId = responderId;
        this.responderEmail = responderEmail;
        this.responderRole = responderRole;
        this.setMessage(message); // Use setter for validation
        this.report = report;
    }

    public void setMessage(String message) {
        if (message != null && message.length() > MAX_MESSAGE_LENGTH) {
            throw new IllegalArgumentException("Message exceeds maximum length of " + MAX_MESSAGE_LENGTH + " characters");
        }
        this.message = message;
    }
}