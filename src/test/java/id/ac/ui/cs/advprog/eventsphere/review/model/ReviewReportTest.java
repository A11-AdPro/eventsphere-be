package id.ac.ui.cs.advprog.eventsphere.review.model;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class ReviewReportTest {

    private ReviewReport reviewReport;
    private Review review;
    private User reporter;
    private User admin;

    @BeforeEach
    void setUp() {
        reporter = User.builder().id(1L).email("reporter@example.com").build();
        admin = User.builder().id(2L).email("admin@example.com").build();
        review = Review.builder().id(1L).content("Test Review").build();

        reviewReport = new ReviewReport();
        reviewReport.setId(1L);
        reviewReport.setReason("Spam content");
        reviewReport.setStatus(ReviewReport.ReportStatus.PENDING);
        reviewReport.setReview(review);
        reviewReport.setReporter(reporter);
        // onCreate will be called by @PrePersist, let's call it manually for setup consistency
        reviewReport.onCreate();
    }

    @Test
    void testReviewReportNoArgsConstructor() {
        ReviewReport newReport = new ReviewReport();
        assertNotNull(newReport);
        assertNull(newReport.getStatus()); // Status is set in onCreate if null
    }

    @Test
    void testReviewReportAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        ReviewReport fullReport = new ReviewReport(1L, "Reason", ReviewReport.ReportStatus.APPROVED, review, reporter, admin, "Notes", now, now);
        assertEquals(1L, fullReport.getId());
        assertEquals("Reason", fullReport.getReason());
        assertEquals(ReviewReport.ReportStatus.APPROVED, fullReport.getStatus());
        assertEquals(review, fullReport.getReview());
        assertEquals(reporter, fullReport.getReporter());
        assertEquals(admin, fullReport.getAdmin());
        assertEquals("Notes", fullReport.getAdminNotes());
        assertEquals(now, fullReport.getCreatedAt());
        assertEquals(now, fullReport.getUpdatedAt());
    }

    @Test
    void testReviewReportBuilder() {
        LocalDateTime now = LocalDateTime.now();
        ReviewReport builtReport = ReviewReport.builder()
                .id(2L)
                .reason("Inappropriate")
                .status(ReviewReport.ReportStatus.REJECTED)
                .review(review)
                .reporter(reporter)
                .admin(admin)
                .adminNotes("Handled.")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(2L, builtReport.getId());
        assertEquals("Inappropriate", builtReport.getReason());
        assertEquals(ReviewReport.ReportStatus.REJECTED, builtReport.getStatus());
        assertEquals(review, builtReport.getReview());
        assertEquals(reporter, builtReport.getReporter());
        assertEquals(admin, builtReport.getAdmin());
        assertEquals("Handled.", builtReport.getAdminNotes());
        assertEquals(now, builtReport.getCreatedAt());
        assertEquals(now, builtReport.getUpdatedAt());
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, reviewReport.getId());
        assertEquals("Spam content", reviewReport.getReason());
        assertEquals(ReviewReport.ReportStatus.PENDING, reviewReport.getStatus());
        assertEquals(review, reviewReport.getReview());
        assertEquals(reporter, reviewReport.getReporter());
        assertNull(reviewReport.getAdmin());
        assertNull(reviewReport.getAdminNotes());
        assertNotNull(reviewReport.getCreatedAt());
        assertNotNull(reviewReport.getUpdatedAt());

        reviewReport.setReason("Updated Reason");
        assertEquals("Updated Reason", reviewReport.getReason());

        reviewReport.setStatus(ReviewReport.ReportStatus.APPROVED);
        assertEquals(ReviewReport.ReportStatus.APPROVED, reviewReport.getStatus());

        Review newReview = Review.builder().id(2L).build();
        reviewReport.setReview(newReview);
        assertEquals(newReview, reviewReport.getReview());

        User newReporter = User.builder().id(3L).build();
        reviewReport.setReporter(newReporter);
        assertEquals(newReporter, reviewReport.getReporter());

        reviewReport.setAdmin(admin);
        assertEquals(admin, reviewReport.getAdmin());

        reviewReport.setAdminNotes("Reviewed by admin.");
        assertEquals("Reviewed by admin.", reviewReport.getAdminNotes());

        LocalDateTime newTime = LocalDateTime.now().plusHours(1);
        reviewReport.setCreatedAt(newTime);
        assertEquals(newTime, reviewReport.getCreatedAt());
        reviewReport.setUpdatedAt(newTime);
        assertEquals(newTime, reviewReport.getUpdatedAt());
    }

    @Test
    void testOnCreateSetsDefaultStatusAndTimestamps() {
        ReviewReport newReport = new ReviewReport();
        assertNull(newReport.getStatus());
        assertNull(newReport.getCreatedAt());
        assertNull(newReport.getUpdatedAt());

        newReport.onCreate(); // Simulates @PrePersist

        assertEquals(ReviewReport.ReportStatus.PENDING, newReport.getStatus());
        assertNotNull(newReport.getCreatedAt());
        assertNotNull(newReport.getUpdatedAt());
        // Allow for a small difference due to execution time
        assertTrue(Math.abs(ChronoUnit.MILLIS.between(newReport.getCreatedAt(), newReport.getUpdatedAt())) < 100);
    }

    @Test
    void testOnCreateWithExistingStatus() {
        ReviewReport newReport = new ReviewReport();
        newReport.setStatus(ReviewReport.ReportStatus.APPROVED); // Set status before onCreate
        newReport.onCreate();
        assertEquals(ReviewReport.ReportStatus.APPROVED, newReport.getStatus()); // Should not override
    }

    @Test
    void testOnUpdateUpdatesTimestamp() {
        LocalDateTime oldUpdatedAt = reviewReport.getUpdatedAt();
        // Simulate a delay
        try {
            Thread.sleep(10); // Ensure a distinct timestamp
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        reviewReport.onUpdate(); // Simulates @PreUpdate
        assertNotNull(reviewReport.getUpdatedAt());
        assertTrue(reviewReport.getUpdatedAt().isAfter(oldUpdatedAt));
    }

    @Test
    void testEnumValues() {
        assertEquals("PENDING", ReviewReport.ReportStatus.PENDING.name());
        assertEquals("APPROVED", ReviewReport.ReportStatus.APPROVED.name());
        assertEquals("REJECTED", ReviewReport.ReportStatus.REJECTED.name());

        assertEquals(ReviewReport.ReportStatus.PENDING, ReviewReport.ReportStatus.valueOf("PENDING"));
        assertEquals(ReviewReport.ReportStatus.APPROVED, ReviewReport.ReportStatus.valueOf("APPROVED"));
        assertEquals(ReviewReport.ReportStatus.REJECTED, ReviewReport.ReportStatus.valueOf("REJECTED"));
    }
}

