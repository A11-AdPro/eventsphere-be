package id.ac.ui.cs.advprog.eventsphere.report.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(name = "sender_role", nullable = false)
    private String senderRole;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(nullable = false)
    private String type;

    @Column(name = "related_entity_id")
    private UUID relatedEntityId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification(Long recipientId, String recipientEmail, String senderRole, String title, String message, String type, UUID relatedEntityId) {
        this.recipientId = recipientId;
        this.recipientEmail = recipientEmail;
        this.senderRole = senderRole;
        this.title = title;
        this.message = message;
        this.type = type;
        this.relatedEntityId = relatedEntityId;
    }

    public void markAsRead() {
        this.read = true;
    }
}