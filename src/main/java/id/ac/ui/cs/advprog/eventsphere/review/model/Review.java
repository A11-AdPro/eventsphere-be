package id.ac.ui.cs.advprog.eventsphere.review.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Review {
    private Long id;
    private Long eventId;
    private Long userId;
    private String username;
    private int rating;
    private String comment;
    private List<String> imagePaths = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String organizerResponse;
    private LocalDateTime responseDate;
    private boolean reported;
    private String reportReason;
    private boolean deleted;

    public Review() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isEditable() {
        return LocalDateTime.now().isBefore(createdAt.plusDays(7));
    }
}
