package id.ac.ui.cs.advprog.eventsphere.review.listener;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewCreatedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventRatingUpdateListenerTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private EventRatingUpdateListener eventRatingUpdateListener;

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
    @DisplayName("Should calculate average rating when review created event is received")
    void handleReviewCreatedEventShouldCalculateAverageRating() {
        // Arrange
        when(reviewRepository.calculateAverageRatingForEvent(TEST_EVENT_ID)).thenReturn(4.5);

        // Act
        eventRatingUpdateListener.handleReviewCreatedEvent(event);

        // Assert
        verify(reviewRepository).calculateAverageRatingForEvent(TEST_EVENT_ID);
    }
}
