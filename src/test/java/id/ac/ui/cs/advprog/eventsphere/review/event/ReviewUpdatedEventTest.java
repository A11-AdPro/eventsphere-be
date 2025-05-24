package id.ac.ui.cs.advprog.eventsphere.review.event;

import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ReviewUpdatedEventTest {

    @Test
    void testReviewUpdatedEvent() {
        Object source = new Object();
        Review mockReview = mock(Review.class);

        ReviewUpdatedEvent event = new ReviewUpdatedEvent(source, mockReview);

        assertEquals(source, event.getSource());
        assertEquals(mockReview, event.getReview());
    }
}

