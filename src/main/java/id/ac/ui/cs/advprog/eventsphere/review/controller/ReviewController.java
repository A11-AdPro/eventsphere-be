package id.ac.ui.cs.advprog.eventsphere.review.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import id.ac.ui.cs.advprog.eventsphere.review.service.ReviewService;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewDto;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewResponseDto;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewReportDto;
import id.ac.ui.cs.advprog.eventsphere.review.exception.ReviewException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> createReview(@RequestBody ReviewDto reviewDto) {
        reviewService.createReview(reviewDto.getRating(), reviewDto.getComment());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Review submitted successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/events/{eventId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> createEventReview(
            @PathVariable Long eventId,
            @RequestParam int rating,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) List<MultipartFile> images) {

        try {
            reviewService.createEventReview(eventId, rating, comment, images);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Review submitted successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ReviewException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/events/{eventId}/reviews")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> getEventReviews(
            @PathVariable Long eventId,
            @RequestParam(required = false, defaultValue = "newest") String sortBy,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) String keyword) {

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("averageRating", reviewService.getEventAverageRating(eventId));
        response.put("reviews", reviewService.getEventReviews(eventId, sortBy, minRating, keyword));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reviews/{reviewId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, Object>> getReview(@PathVariable Long reviewId) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("review", reviewService.getReviewById(reviewId));

            return ResponseEntity.ok(response);
        } catch (ReviewException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewDto reviewDto) {

        try {
            reviewService.updateReview(reviewId, reviewDto.getRating(), reviewDto.getComment());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Review updated successfully");

            return ResponseEntity.ok(response);
        } catch (ReviewException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/reviews/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> deleteReview(@PathVariable Long reviewId) {
        try {
            reviewService.deleteReview(reviewId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Review deleted successfully");

            return ResponseEntity.ok(response);
        } catch (ReviewException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/reviews/{reviewId}/respond")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> respondToReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewResponseDto responseDto) {

        try {
            reviewService.respondToReview(reviewId, responseDto.getResponse());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Response added successfully");

            return ResponseEntity.ok(response);
        } catch (ReviewException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/reviews/{reviewId}/report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> reportReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewReportDto reportDto) {

        try {
            reviewService.reportReview(reviewId, reportDto.getReason());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Review reported successfully");

            return ResponseEntity.ok(response);
        } catch (ReviewException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/admin/reviews/reported")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getReportedReviews() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("reportedReviews", reviewService.getReportedReviews());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/reviews/{reviewId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> restoreReportedReview(@PathVariable Long reviewId) {
        try {
            reviewService.restoreReportedReview(reviewId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Review restored successfully");

            return ResponseEntity.ok(response);
        } catch (ReviewException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
