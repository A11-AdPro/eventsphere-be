package id.ac.ui.cs.advprog.eventsphere.event.dto;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EventResponseDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private String location;
    private BigDecimal price;
    private Long organizerId;
    private String organizerName;
    private Role organizerRole ;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    private boolean isCancelled;
    private LocalDateTime cancellationTime;

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }
    
    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }
}