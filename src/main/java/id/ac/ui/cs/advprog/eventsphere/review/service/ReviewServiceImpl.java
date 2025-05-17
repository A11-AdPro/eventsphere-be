package id.ac.ui.cs.advprog.eventsphere.review.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.AuthService;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewRequest;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewCreatedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final AuthService authService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public Review createReview(ReviewRequest reviewRequest) {
        // Get the currently logged-in user
        User currentUser = authService.getCurrentUser();

        // Create and save the review
        Review review = Review.builder()
                .content(reviewRequest.getContent())
                .rating(reviewRequest.getRating())
                .eventId(reviewRequest.getEventId())
                .user(currentUser)
                .build();

        Review savedReview = reviewRepository.save(review);

        // Publish an event that the review was created
        // This is the key part of the Observer pattern
        eventPublisher.publishEvent(new ReviewCreatedEvent(this, savedReview));

        return savedReview;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByEventId(Long eventId) {
        return reviewRepository.findByEventId(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingForEvent(Long eventId) {
        return reviewRepository.calculateAverageRatingForEvent(eventId);
    }
}
