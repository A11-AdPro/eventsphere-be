package id.ac.ui.cs.advprog.eventsphere.review.event;

import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ReviewCreatedEventTest {

    @Test
    void testReviewCreatedEvent() {
        Object source = new Object();
        Review mockReview = mock(Review.class);

        ReviewCreatedEvent event = new ReviewCreatedEvent(source, mockReview);

        assertEquals(source, event.getSource());
        assertEquals(mockReview, event.getReview());
    }
}

