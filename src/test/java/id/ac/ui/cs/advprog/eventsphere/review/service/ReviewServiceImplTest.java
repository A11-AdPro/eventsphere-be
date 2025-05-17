package id.ac.ui.cs.advprog.eventsphere.review.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.AuthService;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewRequest;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewCreatedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private AuthService authService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Captor
    private ArgumentCaptor<ReviewCreatedEvent> eventCaptor;

    private User testUser;
    private Review testReview;
    private ReviewRequest reviewRequest;
    private final Long TEST_EVENT_ID = 1L;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = User.builder()
                .id(1L)
                .email("attendee@example.com")
                .fullName("Test Attendee")
                .role(Role.ATTENDEE)
                .build();

        // Set up review request
        reviewRequest = new ReviewRequest();
        reviewRequest.setEventId(TEST_EVENT_ID);
        reviewRequest.setContent("Great event!");
        reviewRequest.setRating(5);

        // Set up test review
        testReview = Review.builder()
                .id(1L)
                .content("Great event!")
                .rating(5)
                .eventId(TEST_EVENT_ID)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create a review and publish an event")
    void createReviewShouldSaveAndPublishEvent() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        // Act
        Review result = reviewService.createReview(reviewRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testReview.getId(), result.getId());
        assertEquals(testReview.getContent(), result.getContent());
        assertEquals(testReview.getRating(), result.getRating());
        assertEquals(testReview.getEventId(), result.getEventId());
        assertEquals(testUser, result.getUser());

        // Verify the review was saved
        verify(reviewRepository).save(any(Review.class));

        // Verify an event was published
        verify(eventPublisher).publishEvent(any(ReviewCreatedEvent.class));

        // Capture and check the event details
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        ReviewCreatedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(testReview, capturedEvent.getReview());
    }

    @Test
    @DisplayName("Should return reviews by event ID")
    void getReviewsByEventIdShouldReturnMatchingReviews() {
        // Arrange
        List<Review> expectedReviews = Arrays.asList(testReview);
        when(reviewRepository.findByEventId(TEST_EVENT_ID)).thenReturn(expectedReviews);

        // Act
        List<Review> result = reviewService.getReviewsByEventId(TEST_EVENT_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testReview.getId(), result.get(0).getId());
        assertEquals(testReview.getContent(), result.get(0).getContent());
    }

    @Test
    @DisplayName("Should return average rating for event")
    void getAverageRatingForEventShouldReturnCorrectValue() {
        // Arrange
        Double expectedRating = 4.5;
        when(reviewRepository.calculateAverageRatingForEvent(TEST_EVENT_ID)).thenReturn(expectedRating);

        // Act
        Double result = reviewService.getAverageRatingForEvent(TEST_EVENT_ID);

        // Assert
        assertEquals(expectedRating, result);
        verify(reviewRepository).calculateAverageRatingForEvent(TEST_EVENT_ID);
    }
}
