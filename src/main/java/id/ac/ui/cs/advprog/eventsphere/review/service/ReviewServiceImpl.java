package id.ac.ui.cs.advprog.eventsphere.review.service;

import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewRepository;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewCreatedEvent;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ApplicationEventPublisher publisher;

    public ReviewServiceImpl(ReviewRepository reviewRepository, ApplicationEventPublisher publisher) {
        this.reviewRepository = reviewRepository;
        this.publisher = publisher;
    }

    @Override
    public void createReview(int rating, String comment) {
        Review review = new Review();
        review.setRating(rating);
        review.setComment(comment);
        reviewRepository.save(review);
        publisher.publishEvent(new ReviewCreatedEvent(this, review));
    }
}
