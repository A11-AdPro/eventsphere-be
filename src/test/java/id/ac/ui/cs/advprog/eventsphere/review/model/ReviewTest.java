package id.ac.ui.cs.advprog.eventsphere.review.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReviewTest {

    @Test
    void testGettersAndSetters() {
        // Given
        Review review = new Review();
        int rating = 5;
        String comment = "Amazing experience";

        // When
        review.setRating(rating);
        review.setComment(comment);

        // Then
        assertEquals(rating, review.getRating());
        assertEquals(comment, review.getComment());
    }
}
