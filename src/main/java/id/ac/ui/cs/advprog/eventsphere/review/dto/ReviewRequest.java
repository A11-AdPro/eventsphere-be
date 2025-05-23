package id.ac.ui.cs.advprog.eventsphere.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ReviewRequest {
    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotNull(message = "Ticket ID is required")
    private Long ticketId;

    @NotBlank(message = "Review content cannot be empty")
    @Size(max = 500, message = "Review content cannot exceed 500 characters")
    private String content;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be more than 5")
    private Integer rating;

    private List<MultipartFile> images;
}
