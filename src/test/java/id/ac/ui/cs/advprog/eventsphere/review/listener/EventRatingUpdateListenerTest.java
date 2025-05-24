package id.ac.ui.cs.advprog.eventsphere.review.listener;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewCreatedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventRatingUpdateListenerTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private EventRatingUpdateListener listener;

    @Test
    void handleReviewCreatedEvent_shouldUpdateEventRating() {
        Long eventId = 1L;
        User user = User.builder().id(1L).email("testuser@example.com").build();
        Review review = Review.builder().eventId(eventId).user(user).build();
        ReviewCreatedEvent event = new ReviewCreatedEvent(this, review);

        when(reviewRepository.calculateAverageRatingForEvent(eventId)).thenReturn(4.5);

        listener.handleReviewCreatedEvent(event);

        verify(reviewRepository, times(1)).calculateAverageRatingForEvent(eventId);
        // In a real scenario, you might also verify that an eventService.updateEventRating was called
    }
}

