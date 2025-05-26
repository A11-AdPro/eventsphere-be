package id.ac.ui.cs.advprog.eventsphere.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewReportRequest {
    @NotBlank(message = "Report reason cannot be empty")
    @Size(max = 500, message = "Report reason cannot exceed 500 characters")
    private String reason;
}
