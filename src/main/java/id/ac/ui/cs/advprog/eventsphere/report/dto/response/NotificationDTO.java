
package id.ac.ui.cs.advprog.eventsphere.report.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NotificationDTO {
    private UUID id;
    private UUID recipientId;
    private String senderRole;
    private String title;
    private String message;
    private boolean read;
    private String type;
    private UUID relatedEntityId;
    private LocalDateTime createdAt;
}