package id.ac.ui.cs.advprog.eventsphere.review.model;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class ReviewResponseTest {

    private ReviewResponse reviewResponse;
    private Review review;
    private User organizer;

    @BeforeEach
    void setUp() {
        organizer = User.builder().id(1L).email("organizer@example.com").build();
        review = Review.builder().id(1L).content("Test Review").build();

        reviewResponse = new ReviewResponse();
        reviewResponse.setId(1L);
        reviewResponse.setContent("Thank you for your feedback!");
        reviewResponse.setOrganizer(organizer);
        reviewResponse.setReview(review);
        // onCreate will be called by @PrePersist, let's call it manually for setup consistency
        reviewResponse.onCreate();
    }

    @Test
    void testReviewResponseNoArgsConstructor() {
        ReviewResponse newResponse = new ReviewResponse();
        assertNotNull(newResponse);
    }

    @Test
    void testReviewResponseAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        ReviewResponse fullResponse = new ReviewResponse(1L, "Content", organizer, review, now, now);
        assertEquals(1L, fullResponse.getId());
        assertEquals("Content", fullResponse.getContent());
        assertEquals(organizer, fullResponse.getOrganizer());
        assertEquals(review, fullResponse.getReview());
        assertEquals(now, fullResponse.getCreatedAt());
        assertEquals(now, fullResponse.getUpdatedAt());
    }

    @Test
    void testReviewResponseBuilder() {
        LocalDateTime now = LocalDateTime.now();
        ReviewResponse builtResponse = ReviewResponse.builder()
                .id(2L)
                .content("Builder Response")
                .organizer(organizer)
                .review(review)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(2L, builtResponse.getId());
        assertEquals("Builder Response", builtResponse.getContent());
        assertEquals(organizer, builtResponse.getOrganizer());
        assertEquals(review, builtResponse.getReview());
        assertEquals(now, builtResponse.getCreatedAt());
        assertEquals(now, builtResponse.getUpdatedAt());
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, reviewResponse.getId());
        assertEquals("Thank you for your feedback!", reviewResponse.getContent());
        assertEquals(organizer, reviewResponse.getOrganizer());
        assertEquals(review, reviewResponse.getReview());
        assertNotNull(reviewResponse.getCreatedAt());
        assertNotNull(reviewResponse.getUpdatedAt());

        reviewResponse.setContent("Updated Response");
        assertEquals("Updated Response", reviewResponse.getContent());

        User newOrganizer = User.builder().id(2L).build();
        reviewResponse.setOrganizer(newOrganizer);
        assertEquals(newOrganizer, reviewResponse.getOrganizer());

        Review newReview = Review.builder().id(2L).build();
        reviewResponse.setReview(newReview);
        assertEquals(newReview, reviewResponse.getReview());

        LocalDateTime newTime = LocalDateTime.now().plusHours(1);
        reviewResponse.setCreatedAt(newTime);
        assertEquals(newTime, reviewResponse.getCreatedAt());
        reviewResponse.setUpdatedAt(newTime);
        assertEquals(newTime, reviewResponse.getUpdatedAt());
    }

    @Test
    void testOnCreateSetsTimestamps() {
        ReviewResponse newResponse = new ReviewResponse();
        assertNull(newResponse.getCreatedAt());
        assertNull(newResponse.getUpdatedAt());

        newResponse.onCreate(); // Simulates @PrePersist

        assertNotNull(newResponse.getCreatedAt());
        assertNotNull(newResponse.getUpdatedAt());
        // Allow for a small difference due to execution time
        assertTrue(Math.abs(ChronoUnit.MILLIS.between(newResponse.getCreatedAt(), newResponse.getUpdatedAt())) < 100);
    }

    @Test
    void testOnUpdateUpdatesTimestamp() {
        LocalDateTime oldUpdatedAt = reviewResponse.getUpdatedAt();
        // Simulate a delay
        try {
            Thread.sleep(10); // Ensure a distinct timestamp
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        reviewResponse.onUpdate(); // Simulates @PreUpdate
        assertNotNull(reviewResponse.getUpdatedAt());
        assertTrue(reviewResponse.getUpdatedAt().isAfter(oldUpdatedAt));
    }
}

