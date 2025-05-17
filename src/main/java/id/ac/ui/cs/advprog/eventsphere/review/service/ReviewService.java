package id.ac.ui.cs.advprog.eventsphere.review.service;

import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewRequest;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;

import java.util.List;

public interface ReviewService {
    Review createReview(ReviewRequest reviewRequest);
    List<Review> getReviewsByEventId(Long eventId);
    Double getAverageRatingForEvent(Long eventId);
}
