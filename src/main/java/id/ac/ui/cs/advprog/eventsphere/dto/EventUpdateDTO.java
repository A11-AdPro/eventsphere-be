package id.ac.ui.cs.advprog.eventsphere.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EventUpdateDTO {
    private String title;
    private String description;
    
    @Future(message = "Event date must be in the future")
    private LocalDateTime eventDate;
    
    private String location;
    
    @Positive(message = "Price must be positive")
    private BigDecimal price;
}