package id.ac.ui.cs.advprog.eventsphere.review.service;

import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ReviewService {
    void createReview(int rating, String comment);

    void createEventReview(Long eventId, int rating, String comment, List<MultipartFile> images);

    List<Map<String, Object>> getEventReviews(Long eventId, String sortBy, Integer minRating, String keyword);

    double getEventAverageRating(Long eventId);

    Map<String, Object> getReviewById(Long reviewId);

    void updateReview(Long reviewId, int rating, String comment);

    void deleteReview(Long reviewId);

    void respondToReview(Long reviewId, String response);

    void reportReview(Long reviewId, String reason);

    List<Map<String, Object>> getReportedReviews();

    void restoreReportedReview(Long reviewId);
}
