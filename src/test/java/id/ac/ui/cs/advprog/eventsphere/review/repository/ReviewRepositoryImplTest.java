package id.ac.ui.cs.advprog.eventsphere.review.repository;

import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReviewRepositoryImplTest {

    private ReviewRepositoryImpl reviewRepository;
    private List<Review> reviews;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        reviewRepository = new ReviewRepositoryImpl();

        // Access the private field for testing
        Field reviewsField = ReviewRepositoryImpl.class.getDeclaredField("reviews");
        reviewsField.setAccessible(true);
        reviews = (List<Review>) reviewsField.get(reviewRepository);
    }

    @Test
    void testSaveReview() {
        // Given
        Review review = new Review();
        review.setRating(3);
        review.setComment("Average experience");

        // Verify list is empty initially
        assertEquals(0, reviews.size());

        // When
        reviewRepository.save(review);

        // Then
        assertEquals(1, reviews.size());
        assertTrue(reviews.contains(review));
    }
}