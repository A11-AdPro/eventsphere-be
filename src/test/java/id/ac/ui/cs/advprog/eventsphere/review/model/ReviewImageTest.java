package id.ac.ui.cs.advprog.eventsphere.review.model;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReviewImageTest {

    private ReviewImage reviewImage;
    private Review review;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test.user@example.com").build();
        review = Review.builder()
                .id(1L)
                .content("Great event!")
                .rating(5)
                .user(user)
                .eventId(101L)
                .build();

        reviewImage = new ReviewImage();
        reviewImage.setId(1L);
        reviewImage.setFilePath("/path/to/image.jpg");
        reviewImage.setFileName("image.jpg");
        reviewImage.setContentType("image/jpeg");
        reviewImage.setReview(review);
        // Call onCreate to set initial timestamp
        reviewImage.onCreate();
    }

    @Test
    void testReviewImageNoArgsConstructor() {
        ReviewImage newReviewImage = new ReviewImage();
        assertNotNull(newReviewImage);
    }

    @Test
    void testReviewImageAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        ReviewImage fullReviewImage = new ReviewImage(1L, "/path/to/image.jpg", "image.jpg", "image/jpeg", review, now);
        assertEquals(1L, fullReviewImage.getId());
        assertEquals("/path/to/image.jpg", fullReviewImage.getFilePath());
        assertEquals("image.jpg", fullReviewImage.getFileName());
        assertEquals("image/jpeg", fullReviewImage.getContentType());
        assertEquals(review, fullReviewImage.getReview());
        assertEquals(now, fullReviewImage.getCreatedAt());
    }

    @Test
    void testReviewImageBuilder() {
        LocalDateTime now = LocalDateTime.now();
        ReviewImage builtReviewImage = ReviewImage.builder()
                .id(2L)
                .filePath("/path/to/another.png")
                .fileName("another.png")
                .contentType("image/png")
                .review(review)
                .createdAt(now)
                .build();

        assertEquals(2L, builtReviewImage.getId());
        assertEquals("/path/to/another.png", builtReviewImage.getFilePath());
        assertEquals("another.png", builtReviewImage.getFileName());
        assertEquals("image/png", builtReviewImage.getContentType());
        assertEquals(review, builtReviewImage.getReview());
        assertEquals(now, builtReviewImage.getCreatedAt());
    }

    @Test
    void testGettersAndSetters() {
        assertEquals(1L, reviewImage.getId());
        assertEquals("/path/to/image.jpg", reviewImage.getFilePath());
        assertEquals("image.jpg", reviewImage.getFileName());
        assertEquals("image/jpeg", reviewImage.getContentType());
        assertEquals(review, reviewImage.getReview());
        assertNotNull(reviewImage.getCreatedAt());

        reviewImage.setFilePath("/new/path.png");
        assertEquals("/new/path.png", reviewImage.getFilePath());

        reviewImage.setFileName("new.png");
        assertEquals("new.png", reviewImage.getFileName());

        reviewImage.setContentType("image/png");
        assertEquals("image/png", reviewImage.getContentType());

        Review newReview = Review.builder().id(2L).build();
        reviewImage.setReview(newReview);
        assertEquals(newReview, reviewImage.getReview());

        LocalDateTime newTime = LocalDateTime.now().plusHours(1);
        reviewImage.setCreatedAt(newTime); // Though createdAt is typically set by @PrePersist, testing setter for completeness
        assertEquals(newTime, reviewImage.getCreatedAt());
    }

    @Test
    void testOnCreateTimestamp() {
        ReviewImage newImage = new ReviewImage();
        assertNull(newImage.getCreatedAt());
        newImage.onCreate();
        assertNotNull(newImage.getCreatedAt());
    }
}

