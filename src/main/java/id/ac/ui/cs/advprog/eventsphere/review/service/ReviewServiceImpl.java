package id.ac.ui.cs.advprog.eventsphere.review.service;

import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewRepository;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewCreatedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.exception.ReviewException;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.AuthService;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;

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
    private final AuthService authService;
    private static final Path UPLOAD_DIR = Paths.get("uploads/reviews");

    public ReviewServiceImpl(ReviewRepository reviewRepository, ApplicationEventPublisher publisher,
            AuthService authService) {
        this.reviewRepository = reviewRepository;
        this.publisher = publisher;
        this.authService = authService;

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

        User currentUser = authService.getCurrentUser();

        Review review = new Review();
        review.setRating(rating);
        review.setComment(comment);
        review.setUserId(currentUser.getId());
        review.setUsername(currentUser.getUsername());

        reviewRepository.save(review);
        publisher.publishEvent(new ReviewCreatedEvent(this, review));
    }

    @Override
    public void createEventReview(Long eventId, int rating, String comment, List<MultipartFile> images) {
        // Validate input
        validateRating(rating);
        validateComment(comment);
        validateImages(images);

        // Get authenticated user
        User currentUser = authService.getCurrentUser();
        Long currentUserId = currentUser.getId();
        String currentUsername = currentUser.getUsername();

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

        // Check if the current user is authorized to update this review
        User currentUser = authService.getCurrentUser();
        if (!isReviewOwnerOrAdmin(review, currentUser)) {
            throw new ReviewException("You don't have permission to update this review");
        }

        // Check if the review is editable (within 7 days)
        if (!review.isEditable()) {
            throw new ReviewException("Reviews can only be edited within 7 days of creation");
        }

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

        // Check if the current user is authorized to delete this review
        User currentUser = authService.getCurrentUser();
        if (!isReviewOwnerOrAdmin(review, currentUser)) {
            throw new ReviewException("You don't have permission to delete this review");
        }

        reviewRepository.delete(reviewId);
    }

    @Override
    public void respondToReview(Long reviewId, String response) {
        Review review = findReviewById(reviewId);

        // Check if current user is an organizer
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != Role.ORGANIZER && currentUser.getRole() != Role.ADMIN) {
            throw new ReviewException("Only organizers can respond to reviews");
        }

        // In a real application with an event service, we would also check:
        // if the currentUser is actually the organizer of the event that this review
        // belongs to
        // For now we'll assume the role check is sufficient

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
        // Check if current user is admin
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new ReviewException("Only administrators can access reported reviews");
        }

        List<Review> reportedReviews = reviewRepository.findReportedReviews();

        return reportedReviews.stream()
                .map(this::convertReviewToMap)
                .collect(Collectors.toList());
    }

    @Override
    public void restoreReportedReview(Long reviewId) {
        // Check if current user is admin
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new ReviewException("Only administrators can restore reported reviews");
        }

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

    // Helper method to check if the user is the review owner or an admin
    private boolean isReviewOwnerOrAdmin(Review review, User user) {
        return review.getUserId().equals(user.getId()) ||
                user.getRole() == Role.ADMIN;
    }
}
