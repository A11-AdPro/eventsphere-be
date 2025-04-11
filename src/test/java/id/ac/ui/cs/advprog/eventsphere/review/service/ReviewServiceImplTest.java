package id.ac.ui.cs.advprog.eventsphere.review.service;

import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewCreatedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    private ReviewServiceImpl reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewServiceImpl(reviewRepository, publisher);
    }

    @Test
    void testCreateReview() {
        // Given
        int rating = 4;
        String comment = "Good service";

        doNothing().when(reviewRepository).save(any(Review.class));
        doNothing().when(publisher).publishEvent(any(ReviewCreatedEvent.class));

        // When
        reviewService.createReview(rating, comment);

        // Then
        // Verify review was saved with correct properties
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        Review capturedReview = reviewCaptor.getValue();
        assertEquals(rating, capturedReview.getRating());
        assertEquals(comment, capturedReview.getComment());

        // Verify event was published with correct review
        ArgumentCaptor<ReviewCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ReviewCreatedEvent.class);
        verify(publisher).publishEvent(eventCaptor.capture());
        ReviewCreatedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(capturedReview, capturedEvent.getReview());
    }

    @Test
    void testConstructor() {
        // Given
        ReviewRepository mockRepo = mock(ReviewRepository.class);
        ApplicationEventPublisher mockPublisher = mock(ApplicationEventPublisher.class);

        // When
        ReviewServiceImpl service = new ReviewServiceImpl(mockRepo, mockPublisher);

        // Then
        // Just testing that constructor doesn't throw exceptions
        // and works correctly
    }
}
