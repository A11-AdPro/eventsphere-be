package id.ac.ui.cs.advprog.eventsphere.review.controller;

import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewReportRequest;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewRequest;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewResponseRequest;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewImage;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewReport;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewResponse;
import id.ac.ui.cs.advprog.eventsphere.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<Review> createReview(@Valid @RequestBody ReviewRequest reviewRequest) {
        Review createdReview = reviewService.createReview(reviewRequest);
        return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
    }

    @PostMapping(value = "/{reviewId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ATTENDEE')")
    public ResponseEntity<List<ReviewImage>> addImagesToReview(
            @PathVariable Long reviewId,
            @RequestParam("images") List<MultipartFile> images) {
        List<ReviewImage> savedImages = reviewService.addImagesToReview(reviewId, images);
        return ResponseEntity.ok(savedImages);
    }

    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("hasAnyRole('ATTENDEE', 'ADMIN')")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        reviewService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('ATTENDEE', 'ADMIN')")
    public ResponseEntity<Review> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest reviewRequest) {
        Review updatedReview = reviewService.updateReview(reviewId, reviewRequest);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('ATTENDEE', 'ADMIN')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Review>> getReviewsByEventId(@PathVariable Long eventId) {
        List<Review> reviews = reviewService.getReviewsByEventId(eventId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/event/{eventId}/paginated")
    public ResponseEntity<Page<Review>> getReviewsByEventIdPaginated(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sortBy) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewService.getReviewsByEventIdSorted(eventId, sortBy, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/event/{eventId}/search")
    public ResponseEntity<Page<Review>> searchReviewsByKeyword(
            @PathVariable Long eventId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews = reviewService.searchReviewsByKeyword(eventId, keyword, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/event/{eventId}/rating")
    public ResponseEntity<Double> getAverageRatingForEvent(@PathVariable Long eventId) {
        Double avgRating = reviewService.getAverageRatingForEvent(eventId);
        return ResponseEntity.ok(avgRating != null ? avgRating : 0.0);
    }

    @PostMapping("/{reviewId}/respond")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ReviewResponse> respondToReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewResponseRequest responseRequest) {
        ReviewResponse response = reviewService.respondToReview(reviewId, responseRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{reviewId}/report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewReport> reportReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewReportRequest reportRequest) {
        ReviewReport report = reviewService.reportReview(reviewId, reportRequest);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reported")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Review>> getReportedReviews() {
        List<Review> reportedReviews = reviewService.getReportedReviews();
        return ResponseEntity.ok(reportedReviews);
    }

    @PatchMapping("/reports/{reportId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveReport(@PathVariable Long reportId) {
        reviewService.approveReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reports/{reportId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectReport(@PathVariable Long reportId) {
        reviewService.rejectReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{reviewId}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> hideReview(@PathVariable Long reviewId) {
        reviewService.hideReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{reviewId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> restoreReview(@PathVariable Long reviewId) {
        reviewService.restoreReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{reviewId}/canEdit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> canEditReview(@PathVariable Long reviewId) {
        boolean canEdit = reviewService.canEditReview(reviewId);
        return ResponseEntity.ok(Map.of("canEdit", canEdit));
    }
}
