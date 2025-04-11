package id.ac.ui.cs.advprog.eventsphere.review.integration;

import id.ac.ui.cs.advprog.eventsphere.review.controller.ReviewController;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewRepository;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewRepositoryImpl;
import id.ac.ui.cs.advprog.eventsphere.review.service.ReviewService;
import id.ac.ui.cs.advprog.eventsphere.review.service.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ReviewIntegrationTest {

    private ReviewRepository reviewRepository;
    private ApplicationEventPublisher publisher;
    private ReviewService reviewService;
    private ReviewController reviewController;

    @BeforeEach
    void setUp() {
        reviewRepository = spy(new ReviewRepositoryImpl());
        publisher = mock(ApplicationEventPublisher.class);
        reviewService = new ReviewServiceImpl(reviewRepository, publisher);
        reviewController = new ReviewController(reviewService);
    }

    @Test
    void testFullReviewFlow() {
        // Given
        int rating = 5;
        String comment = "Excellent event!";
        doNothing().when(publisher).publishEvent(any());

        // When
        reviewController.createReview(rating, comment);

        // Then
        verify(reviewRepository).save(any());
        verify(publisher).publishEvent(any());
    }
}