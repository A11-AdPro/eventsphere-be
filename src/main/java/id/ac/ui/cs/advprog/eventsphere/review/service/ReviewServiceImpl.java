package id.ac.ui.cs.advprog.eventsphere.review.service;

import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewRepository;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewCreatedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.exception.ReviewException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ApplicationEventPublisher publisher;
    private static final Path UPLOAD_DIR = Paths.get("uploads/reviews");

    public ReviewServiceImpl(ReviewRepository reviewRepository, ApplicationEventPublisher publisher) {
        this.reviewRepository = reviewRepository;
        this.publisher = publisher;

        // Create upload directory if it doesn't exist
        try {
            if (!Files.exists(UPLOAD_DIR)) {
                Files.createDirectories(UPLOAD_DIR);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Override
    public void createReview(int rating, String comment) {
        validateRating(rating);
        validateComment(comment);

        Review review = new Review();
        review.setRating(rating);
        review.setComment(comment);

        // In a real application, we'd set the user ID from the authenticated user
        review.setUserId(1L);
        review.setUsername("default_user");

        reviewRepository.save(review);
        publisher.publishEvent(new ReviewCreatedEvent(this, review));
    }

    @Override
    public void createEventReview(Long eventId, int rating, String comment, List<MultipartFile> images) {
        // Validate input
        validateRating(rating);
        validateComment(comment);
        validateImages(images);

        // In a real application, we'd get the current authenticated user
        Long currentUserId = 1L; // Mock user ID
        String currentUsername = "testuser"; // Mock username

        // Check if user already reviewed this event
        if (reviewRepository.existsByEventIdAndUserId(eventId, currentUserId)) {
            throw new ReviewException("You have already reviewed this event");
        }

        // Create and populate the review
        Review review = new Review();
        review.setEventId(eventId);
        review.setUserId(currentUserId);
        review.setUsername(currentUsername);
        review.setRating(rating);
        review.setComment(comment);

        // Process and save images if provided
        if (images != null && !images.isEmpty()) {
            List<String> savedImagePaths = saveImages(images, eventId, currentUserId);
            review.setImagePaths(savedImagePaths);
        }

        // Save the review
        reviewRepository.save(review);

        // Publish event for listeners
        publisher.publishEvent(new ReviewCreatedEvent(this, review));
    }

    @Override
    public List<Map<String, Object>> getEventReviews(Long eventId, String sortBy, Integer minRating, String keyword) {
        List<Review> eventReviews;

        // Apply filters if provided
        if (minRating != null) {
            eventReviews = reviewRepository.findByEventIdAndRatingGreaterThanEqual(eventId, minRating);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            eventReviews = reviewRepository.findByEventIdAndCommentContaining(eventId, keyword);
        } else {
            eventReviews = reviewRepository.findByEventId(eventId);
        }

        // Apply sorting
        switch (sortBy.toLowerCase()) {
            case "highest":
                eventReviews.sort(Comparator.comparing(Review::getRating).reversed());
                break;
            case "lowest":
                eventReviews.sort(Comparator.comparing(Review::getRating));
                break;
            case "newest":
            default:
                eventReviews.sort(Comparator.comparing(Review::getCreatedAt).reversed());
                break;
        }

        // Convert to DTO format
        return eventReviews.stream()
                .map(this::convertReviewToMap)
                .collect(Collectors.toList());
    }

    @Override
    public double getEventAverageRating(Long eventId) {
        List<Review> eventReviews = reviewRepository.findByEventId(eventId);
        if (eventReviews.isEmpty()) {
            return 0.0;
        }

        double sum = eventReviews.stream()
                .mapToInt(Review::getRating)
                .sum();

        return Math.round((sum / eventReviews.size()) * 10.0) / 10.0;
    }

    @Override
    public Map<String, Object> getReviewById(Long reviewId) {
        Review review = findReviewById(reviewId);
        return convertReviewToMap(review);
    }

    @Override
    public void updateReview(Long reviewId, int rating, String comment) {
        Review review = findReviewById(reviewId);

        // Check if the review is editable (within 7 days)
        if (!review.isEditable()) {
            throw new ReviewException("Reviews can only be edited within 7 days of creation");
        }

        // In a real application, we'd check if the current user is the owner of the
        // review
        // For now, we'll simulate this check has passed

        validateRating(rating);
        validateComment(comment);

        review.setRating(rating);
        review.setComment(comment);
        review.setUpdatedAt(LocalDateTime.now());

        reviewRepository.update(review);
    }

    @Override
    public void deleteReview(Long reviewId) {
        Review review = findReviewById(reviewId);

        // In a real application, we'd check if the current user has permission to
        // delete:
        // - The review creator can always delete their own review
        // - Admins can delete any review
        // For now, we'll simulate this check has passed

        reviewRepository.delete(reviewId);
    }

    @Override
    public void respondToReview(Long reviewId, String response) {
        Review review = findReviewById(reviewId);

        // In a real application, we'd check if the current user is an organizer of this
        // event
        // For now, we'll simulate this check has passed

        validateOrganizerResponse(response);

        review.setOrganizerResponse(response);
        review.setResponseDate(LocalDateTime.now());

        reviewRepository.update(review);
    }

    @Override
    public void reportReview(Long reviewId, String reason) {
        Review review = findReviewById(reviewId);

        // Any authenticated user can report a review
        // We could also add rate limiting to prevent abuse

        review.setReported(true);
        review.setReportReason(reason);

        reviewRepository.update(review);

        // In a real application, we might also notify admins
    }

    @Override
    public List<Map<String, Object>> getReportedReviews() {
        // In a real application, we'd check if the current user is an admin
        // For now, we'll simulate this check has passed

        List<Review> reportedReviews = reviewRepository.findReportedReviews();

        return reportedReviews.stream()
                .map(this::convertReviewToMap)
                .collect(Collectors.toList());
    }

    @Override
    public void restoreReportedReview(Long reviewId) {
        // In a real application, we'd check if the current user is an admin
        // For now, we'll simulate this check has passed

        Review review = findReviewById(reviewId);

        if (!review.isReported()) {
            throw new ReviewException("This review has not been reported");
        }

        review.setReported(false);
        review.setReportReason(null);

        reviewRepository.update(review);
    }

    // Helper methods
    private Review findReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewException("Review not found with ID: " + reviewId));
    }

    private Map<String, Object> convertReviewToMap(Review review) {
        Map<String, Object> reviewMap = new HashMap<>();
        reviewMap.put("id", review.getId());
        reviewMap.put("eventId", review.getEventId());
        reviewMap.put("userId", review.getUserId());
        reviewMap.put("username", review.getUsername());
        reviewMap.put("rating", review.getRating());
        reviewMap.put("comment", review.getComment());
        reviewMap.put("imagePaths", review.getImagePaths());
        reviewMap.put("createdAt", review.getCreatedAt());
        reviewMap.put("updatedAt", review.getUpdatedAt());

        if (review.getOrganizerResponse() != null) {
            reviewMap.put("organizerResponse", review.getOrganizerResponse());
            reviewMap.put("responseDate", review.getResponseDate());
        }

        return reviewMap;
    }

    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new ReviewException("Rating must be between 1 and 5");
        }
    }

    private void validateComment(String comment) {
        if (comment != null && comment.length() > 500) {
            throw new ReviewException("Comment cannot exceed 500 characters");
        }
    }

    private void validateOrganizerResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new ReviewException("Response cannot be empty");
        }

        if (response.length() > 200) {
            throw new ReviewException("Response cannot exceed 200 characters");
        }
    }

    private void validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        if (images.size() > 3) {
            throw new ReviewException("Maximum 3 images are allowed");
        }

        for (MultipartFile image : images) {
            String contentType = image.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                throw new ReviewException("Only JPEG and PNG images are allowed");
            }

            if (image.getSize() > 5 * 1024 * 1024) { // 5MB limit
                throw new ReviewException("Image size cannot exceed 5MB");
            }
        }
    }

    private List<String> saveImages(List<MultipartFile> images, Long eventId, Long userId) {
        List<String> savedPaths = new ArrayList<>();

        try {
            for (MultipartFile image : images) {
                String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                Path targetPath = UPLOAD_DIR.resolve(fileName);

                Files.copy(image.getInputStream(), targetPath);
                savedPaths.add(targetPath.toString());
            }
            return savedPaths;
        } catch (IOException e) {
            throw new ReviewException("Failed to save image files", e);
        }
    }
}
