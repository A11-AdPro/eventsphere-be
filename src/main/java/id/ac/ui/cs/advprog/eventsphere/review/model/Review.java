package id.ac.ui.cs.advprog.eventsphere.review.model;

import com.fasterxml.jackson.annotation.JsonManagedReference; // Added import
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private Integer rating;  // Rating from 1-5

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Long eventId;

    @Column
    private Long ticketId; // Reference to the ticket used for the event

    @Column(nullable = false)
    private Boolean isReported = false;

    @Column(nullable = false)
    private Boolean isVisible = true;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER) // Ensured EAGER fetch
    @JsonManagedReference // Added annotation
    private List<ReviewImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewResponse> responses = new ArrayList<>();

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewReport> reports = new ArrayList<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Method to check if review can be edited (within 7 days)
    public boolean canEdit() {
        return this.createdAt.plusDays(7).isAfter(LocalDateTime.now());
    }

    // Method to add an image, enforcing the 3-image limit
    public boolean addImage(ReviewImage image) {
        if (images.size() >= 3) {
            return false;
        }
        images.add(image);
        return true;
    }

    // Method to add a response
    public void addResponse(ReviewResponse response) {
        responses.add(response);
    }

    // Method to report the review
    public void report(ReviewReport report) {
        reports.add(report);
        this.isReported = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt; // Ensures updatedAt is identical to createdAt on initial creation
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
