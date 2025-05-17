package id.ac.ui.cs.advprog.eventsphere.review.listener;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewCreatedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class ReviewNotificationListenerTest {

    @InjectMocks
    private ReviewNotificationListener reviewNotificationListener;

    private User testUser;
    private Review testReview;
    private ReviewCreatedEvent event;
    private final Long TEST_EVENT_ID = 1L;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("attendee@example.com")
                .fullName("Test Attendee")
                .role(Role.ATTENDEE)
                .build();

        testReview = Review.builder()
                .id(1L)
                .content("Great event!")
                .rating(5)
                .eventId(TEST_EVENT_ID)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        event = new ReviewCreatedEvent(this, testReview);
    }

    @Test
    @DisplayName("Should process notification when review created event is received")
    void handleReviewCreatedEventShouldProcessNotification() {
        // Act - This would normally throw an exception if there's an issue
        reviewNotificationListener.handleReviewCreatedEvent(event);

        // Assert - Nothing to verify explicitly since this is just logging for now
        // In a real implementation with a notification service, we would verify the service was called
    }
}
