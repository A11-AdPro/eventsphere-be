package id.ac.ui.cs.advprog.eventsphere.review.listener;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewCreatedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewNotificationListenerTest {

    // We can mock the logger to verify interactions if needed, but for now,
    // we'll focus on ensuring the method runs without error as it primarily logs.
    // @Mock
    // private Logger logger; // Assuming SLF4J is used via Lombok @Slf4j

    @InjectMocks
    private ReviewNotificationListener listener;

    @Test
    void handleReviewCreatedEvent_shouldLogNotification() {
        Long eventId = 1L;
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("testuser");

        Review review = Review.builder().eventId(eventId).user(user).build();
        ReviewCreatedEvent event = new ReviewCreatedEvent(this, review);

        // As this listener only logs, we are just testing that it runs without error.
        // In a real scenario with a dedicated NotificationService, we would verify its interactions.
        listener.handleReviewCreatedEvent(event);

        // To verify logging, you would typically use a test appender or mock the logger.
        // For this example, we ensure no exceptions are thrown and the method completes.
        // verify(logger).info(anyString(), eq(eventId), eq("testuser")); // Example if logger was mocked
    }
}

