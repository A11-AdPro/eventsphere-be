package id.ac.ui.cs.advprog.eventsphere.review.model;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReviewTest {

    private Review review;
    private User user;
    private ReviewImage reviewImage1;
    private ReviewImage reviewImage2;
    private ReviewImage reviewImage3;
    private ReviewImage reviewImage4;
    private ReviewResponse reviewResponse;
    private ReviewReport reviewReport;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test.user@example.com").build();
        review = new Review();
        review.setId(1L);
        review.setContent("Great event!");
        review.setRating(5);
        review.setUser(user);
        review.setEventId(101L);
        review.setTicketId(202L);
        review.setIsReported(false);
        review.setIsVisible(true);
        review.setImages(new ArrayList<>());
        review.setResponses(new ArrayList<>());
        review.setReports(new ArrayList<>());

        // Call onCreate to set initial timestamps
        review.onCreate();

        reviewImage1 = ReviewImage.builder().id(1L).fileName("image1.jpg").review(review).build();
        reviewImage2 = ReviewImage.builder().id(2L).fileName("image2.jpg").review(review).build();
        reviewImage3 = ReviewImage.builder().id(3L).fileName("image3.jpg").review(review).build();
        reviewImage4 = ReviewImage.builder().id(4L).fileName("image4.jpg").review(review).build(); // For testing limit

        reviewResponse = ReviewResponse.builder().id(1L).content("Thanks!").review(review).build();
        reviewReport = ReviewReport.builder().id(1L).reason("Spam").review(review).build();
    }

    @Test
    void testReviewNoArgsConstructor() {
        Review newReview = new Review();
        assertNotNull(newReview);
    }

    @Test
    void testReviewAllArgsConstructor() {
        List<ReviewImage> images = new ArrayList<>();
        List<ReviewResponse> responses = new ArrayList<>();
        List<ReviewReport> reports = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        Review fullReview = new Review(1L, "Content", 5, user, 101L, 202L, false, true, images, responses, reports, now, now);
        assertEquals(1L, fullReview.getId());
        assertEquals("Content", fullReview.getContent());
        assertEquals(5, fullReview.getRating());
        assertEquals(user, fullReview.getUser());
        assertEquals(101L, fullReview.getEventId());
        assertEquals(202L, fullReview.getTicketId());
        assertFalse(fullReview.getIsReported());
        assertTrue(fullReview.getIsVisible());
        assertEquals(images, fullReview.getImages());
        assertEquals(responses, fullReview.getResponses());
        assertEquals(reports, fullReview.getReports());
        assertEquals(now, fullReview.getCreatedAt());
        assertEquals(now, fullReview.getUpdatedAt());
    }

    @Test
    void testReviewBuilder() {
        LocalDateTime now = LocalDateTime.now();
        Review builtReview = Review.builder()
            .id(1L)
            .content("Builder Content")
            .rating(4)
            .user(user)
            .eventId(102L)
            .ticketId(203L)
            .isReported(true)
            .isVisible(false)
            .images(new ArrayList<>())
            .responses(new ArrayList<>())
            .reports(new ArrayList<>())
            .createdAt(now)
            .updatedAt(now)
            .build();

        assertEquals(1L, builtReview.getId());
        assertEquals("Builder Content", builtReview.getContent());
        assertEquals(4, builtReview.getRating());
        assertEquals(user, builtReview.getUser());
        assertEquals(102L, builtReview.getEventId());
        assertEquals(203L, builtReview.getTicketId());
        assertTrue(builtReview.getIsReported());
        assertFalse(builtReview.getIsVisible());
        assertNotNull(builtReview.getImages());
        assertNotNull(builtReview.getResponses());
        assertNotNull(builtReview.getReports());
        assertEquals(now, builtReview.getCreatedAt());
        assertEquals(now, builtReview.getUpdatedAt());
    }


    @Test
    void testGettersAndSetters() {
        assertEquals(1L, review.getId());
        assertEquals("Great event!", review.getContent());
        assertEquals(5, review.getRating());
        assertEquals(user, review.getUser());
        assertEquals(101L, review.getEventId());
        assertEquals(202L, review.getTicketId());
        assertFalse(review.getIsReported());
        assertTrue(review.getIsVisible());
        assertNotNull(review.getImages());
        assertNotNull(review.getResponses());
        assertNotNull(review.getReports());
        assertNotNull(review.getCreatedAt());
        assertNotNull(review.getUpdatedAt());

        review.setContent("Updated content");
        assertEquals("Updated content", review.getContent());

        review.setRating(4);
        assertEquals(4, review.getRating());

        User newUser = User.builder().id(2L).build();
        review.setUser(newUser);
        assertEquals(newUser, review.getUser());

        review.setEventId(105L);
        assertEquals(105L, review.getEventId());

        review.setTicketId(205L);
        assertEquals(205L, review.getTicketId());

        review.setIsReported(true);
        assertTrue(review.getIsReported());

        review.setIsVisible(false);
        assertFalse(review.getIsVisible());

        List<ReviewImage> newImages = List.of(reviewImage1);
        review.setImages(newImages);
        assertEquals(newImages, review.getImages());

        List<ReviewResponse> newResponses = List.of(reviewResponse);
        review.setResponses(newResponses);
        assertEquals(newResponses, review.getResponses());

        List<ReviewReport> newReports = List.of(reviewReport);
        review.setReports(newReports);
        assertEquals(newReports, review.getReports());

        LocalDateTime newTime = LocalDateTime.now().plusHours(1);
        review.setCreatedAt(newTime);
        assertEquals(newTime, review.getCreatedAt());
        review.setUpdatedAt(newTime);
        assertEquals(newTime, review.getUpdatedAt());
    }

    @Test
    void testOnCreateAndOnUpdateTimestamps() {
        Review newReview = new Review();
        assertNull(newReview.getCreatedAt());
        assertNull(newReview.getUpdatedAt());

        newReview.onCreate(); // Simulates @PrePersist
        assertNotNull(newReview.getCreatedAt());
        assertNotNull(newReview.getUpdatedAt());
        // Allow for a small difference due to execution time if not set simultaneously in onCreate
        assertTrue(Math.abs(ChronoUnit.MILLIS.between(newReview.getCreatedAt(), newReview.getUpdatedAt())) < 100);

        LocalDateTime oldUpdatedAt = newReview.getUpdatedAt();
        // Simulate a delay
        try {
            Thread.sleep(10); // A small delay to ensure updatedAt changes
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        newReview.onUpdate(); // Simulates @PreUpdate
        assertNotNull(newReview.getUpdatedAt());
        assertTrue(newReview.getUpdatedAt().isAfter(oldUpdatedAt));
    }

    @Test
    void testCanEditWithinSevenDays() {
        review.setCreatedAt(LocalDateTime.now().minusDays(6));
        assertTrue(review.canEdit());
    }

    @Test
    void testCanEditExactlySevenDays() {
        // Set created at to be exactly 7 days ago, to the nanosecond, canEdit should be true (or edge case)
        // Depending on how isAfter is handled, this might be true or false.
        // Let's assume it's true if it's exactly 7 days or less.
        review.setCreatedAt(LocalDateTime.now().minusDays(7));
        assertFalse(review.canEdit()); // if it's exactly 7 days, plusDays(7) will be now, now.isAfter(now) is false.
                                      // so this should be false.
                                      // The logic is `this.createdAt.plusDays(7).isAfter(LocalDateTime.now())`
                                      // If createdAt = now - 7 days, then createdAt.plusDays(7) = now.
                                      // now.isAfter(now) is false. So, it cannot be edited.
    }

    @Test
    void testCanEditAfterSevenDays() {
        review.setCreatedAt(LocalDateTime.now().minusDays(8));
        assertFalse(review.canEdit());
    }

    @Test
    void testCanEditFutureDate() {
        review.setCreatedAt(LocalDateTime.now().plusDays(1)); // Created in the future
        assertTrue(review.canEdit());
    }


    @Test
    void testAddImageSuccessfully() {
        assertTrue(review.addImage(reviewImage1));
        assertEquals(1, review.getImages().size());
        assertTrue(review.getImages().contains(reviewImage1));

        assertTrue(review.addImage(reviewImage2));
        assertEquals(2, review.getImages().size());

        assertTrue(review.addImage(reviewImage3));
        assertEquals(3, review.getImages().size());
    }

    @Test
    void testAddImageExceedsLimit() {
        review.addImage(reviewImage1);
        review.addImage(reviewImage2);
        review.addImage(reviewImage3);

        assertFalse(review.addImage(reviewImage4)); // Try to add 4th image
        assertEquals(3, review.getImages().size()); // Should still be 3
        assertFalse(review.getImages().contains(reviewImage4));
    }

    @Test
    void testAddImageWhenAlreadyFull() {
        List<ReviewImage> initialImages = new ArrayList<>();
        initialImages.add(reviewImage1);
        initialImages.add(reviewImage2);
        initialImages.add(reviewImage3);
        review.setImages(initialImages); // Pre-fill images

        assertFalse(review.addImage(reviewImage4));
        assertEquals(3, review.getImages().size());
    }


    @Test
    void testAddResponse() {
        review.addResponse(reviewResponse);
        assertEquals(1, review.getResponses().size());
        assertTrue(review.getResponses().contains(reviewResponse));
    }

    @Test
    void testReportReview() {
        assertFalse(review.getIsReported());
        review.report(reviewReport);
        assertEquals(1, review.getReports().size());
        assertTrue(review.getReports().contains(reviewReport));
        assertTrue(review.getIsReported());
    }
}

