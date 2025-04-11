package id.ac.ui.cs.advprog.eventsphere.review.event;

import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ReviewCreatedEventTest {

    @Test
    void testConstructorAndGetters() {
        // Given
        Object source = new Object();
        Review review = new Review();
        review.setRating(4);
        review.setComment("Good event");

        // When
        ReviewCreatedEvent event = new ReviewCreatedEvent(source, review);

        // Then
        assertSame(source, event.getSource());
        assertSame(review, event.getReview());
    }
}
