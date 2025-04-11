package id.ac.ui.cs.advprog.eventsphere.event.dto;

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
    private UserSummaryDTO organizer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
}