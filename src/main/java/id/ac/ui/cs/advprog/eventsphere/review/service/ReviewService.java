package id.ac.ui.cs.advprog.eventsphere.review.service;

import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewReportRequest;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewRequest;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewResponseRequest;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewImage;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewReport;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {
    // Original methods
    Review createReview(ReviewRequest reviewRequest);
    List<Review> getReviewsByEventId(Long eventId);
    Double getAverageRatingForEvent(Long eventId);

    // New methods for enhanced functionality

    // Read with filtering and pagination
    Page<Review> getReviewsByEventIdPaginated(Long eventId, Pageable pageable);
    Page<Review> getReviewsByEventIdSorted(Long eventId, String sortBy, Pageable pageable);
    Page<Review> searchReviewsByKeyword(Long eventId, String keyword, Pageable pageable);

    // Update and delete
    Review updateReview(Long reviewId, ReviewRequest reviewRequest);
    void deleteReview(Long reviewId);

    // Validation methods
    boolean hasUserAttendedEvent(Long userId, Long eventId, Long ticketId);
    boolean hasUserAlreadyReviewed(Long userId, Long eventId);
    boolean canEditReview(Long reviewId);

    // Image management
    List<ReviewImage> addImagesToReview(Long reviewId, List<MultipartFile> images);
    void deleteImage(Long imageId);

    // Organizer response methods
    ReviewResponse respondToReview(Long reviewId, ReviewResponseRequest responseRequest);

    // Reporting and moderation
    ReviewReport reportReview(Long reviewId, ReviewReportRequest reportRequest);
    List<Review> getReportedReviews();
    void approveReport(Long reportId);
    void rejectReport(Long reportId);

    // Admin methods
    void hideReview(Long reviewId);
    void restoreReview(Long reviewId);
}
