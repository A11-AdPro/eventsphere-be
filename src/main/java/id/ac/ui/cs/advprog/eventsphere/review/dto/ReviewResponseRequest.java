package id.ac.ui.cs.advprog.eventsphere.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewResponseRequest {
    @NotBlank(message = "Response content cannot be empty")
    @Size(max = 200, message = "Response cannot exceed 200 characters")
    private String content;
}
