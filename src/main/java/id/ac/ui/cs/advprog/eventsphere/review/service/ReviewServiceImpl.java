package id.ac.ui.cs.advprog.eventsphere.review.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.AuthService;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewReportRequest;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewRequest;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewResponseRequest;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewCreatedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewDeletedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewReportedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewUpdatedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewImage;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewReport;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewResponse;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewImageRepository;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewReportRepository;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewRepository;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository imageRepository;
    private final ReviewResponseRepository responseRepository;
    private final ReviewReportRepository reportRepository;
    private final AuthService authService;
    private final ApplicationEventPublisher eventPublisher;

    private final Path fileStoragePath = Paths.get("uploads/reviews").toAbsolutePath().normalize();

    @Override
    @Transactional
    public Review createReview(ReviewRequest reviewRequest) {
        User currentUser = authService.getCurrentUser();

        if (currentUser.getRole() != Role.ATTENDEE) {
            throw new AccessDeniedException("Only attendees can create reviews");
        }

        if (!hasUserAttendedEvent(currentUser.getId(), reviewRequest.getEventId(), reviewRequest.getTicketId())) {
            throw new IllegalArgumentException("You must have attended the event to leave a review");
        }

        if (hasUserAlreadyReviewed(currentUser.getId(), reviewRequest.getEventId())) {
            throw new IllegalArgumentException("You have already submitted a review for this event");
        }

        Review review = Review.builder()
                .content(reviewRequest.getContent())
                .rating(reviewRequest.getRating())
                .eventId(reviewRequest.getEventId())
                .ticketId(reviewRequest.getTicketId())
                .user(currentUser)
                .isReported(false)
                .isVisible(true)
                .build();

        Review savedReview = reviewRepository.save(review);

        if (reviewRequest.getImages() != null && !reviewRequest.getImages().isEmpty()) {
            addImagesToReview(savedReview.getId(), reviewRequest.getImages());
        }

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

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByEventIdPaginated(Long eventId, Pageable pageable) {
        return reviewRepository.findByEventIdOrderByCreatedAtDesc(eventId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> getReviewsByEventIdSorted(Long eventId, String sortBy, Pageable pageable) {
        switch (sortBy.toLowerCase()) {
            case "newest":
                return reviewRepository.findByEventIdOrderByCreatedAtDesc(eventId, pageable);
            case "highest":
                return reviewRepository.findByEventIdOrderByRatingDesc(eventId, pageable);
            case "lowest":
                return reviewRepository.findByEventIdOrderByRatingAsc(eventId, pageable);
            default:
                return reviewRepository.findByEventIdOrderByCreatedAtDesc(eventId, pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Review> searchReviewsByKeyword(Long eventId, String keyword, Pageable pageable) {
        return reviewRepository.findByEventIdAndContentContaining(eventId, keyword, pageable);
    }

    @Override
    @Transactional
    public Review updateReview(Long reviewId, ReviewRequest reviewRequest) {
        User currentUser = authService.getCurrentUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!Objects.equals(review.getUser().getId(), currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You don't have permission to update this review");
        }

        if (currentUser.getRole() != Role.ADMIN && !review.canEdit()) {
            throw new IllegalArgumentException("Reviews can only be edited within 7 days of posting");
        }

        review.setContent(reviewRequest.getContent());
        review.setRating(reviewRequest.getRating());

        Review updatedReview = reviewRepository.save(review);

        if (reviewRequest.getImages() != null && !reviewRequest.getImages().isEmpty()) {
            addImagesToReview(updatedReview.getId(), reviewRequest.getImages());
        }

        eventPublisher.publishEvent(new ReviewUpdatedEvent(this, updatedReview));

        return updatedReview;
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        User currentUser = authService.getCurrentUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!Objects.equals(review.getUser().getId(), currentUser.getId()) &&
                currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You don't have permission to delete this review");
        }

        eventPublisher.publishEvent(new ReviewDeletedEvent(this, review));

        reviewRepository.delete(review);
    }

    @Override
    public boolean hasUserAttendedEvent(Long userId, Long eventId, Long ticketId) {
        return ticketId != null;
    }

    @Override
    public boolean hasUserAlreadyReviewed(Long userId, Long eventId) {
        return reviewRepository.existsByUserIdAndEventId(userId, eventId);
    }

    @Override
    public boolean canEditReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        return review.canEdit();
    }

    @Override
    @Transactional
    public List<ReviewImage> addImagesToReview(Long reviewId, List<MultipartFile> images) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        int currentImageCount = imageRepository.countByReviewId(reviewId);
        if (currentImageCount + images.size() > 3) {
            throw new IllegalArgumentException("Maximum of 3 images allowed per review");
        }

        List<ReviewImage> savedImages = new ArrayList<>();

        try {
            Files.createDirectories(fileStoragePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }

        for (MultipartFile image : images) {
            String contentType = image.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
                throw new IllegalArgumentException("Only JPEG and PNG image formats are allowed");
            }

            try {
                String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
                Path targetLocation = fileStoragePath.resolve(filename);

                Files.copy(image.getInputStream(), targetLocation);

                ReviewImage reviewImage = ReviewImage.builder()
                        .fileName(filename)
                        .filePath(targetLocation.toString())
                        .contentType(contentType)
                        .review(review)
                        .build();

                savedImages.add(imageRepository.save(reviewImage));
            } catch (IOException e) {
                throw new RuntimeException("Could not store image " + image.getOriginalFilename(), e);
            }
        }

        return savedImages;
    }

    @Override
    @Transactional
    public void deleteImage(Long imageId) {
        User currentUser = authService.getCurrentUser();
        ReviewImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        Review review = image.getReview();

        if (!Objects.equals(review.getUser().getId(), currentUser.getId()) &&
                currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("You don't have permission to delete this image");
        }

        try {
            Path imagePath = Paths.get(image.getFilePath());
            Files.deleteIfExists(imagePath);
        } catch (IOException e) {
            System.err.println("Error deleting image file: " + e.getMessage());
        }

        imageRepository.delete(image);
    }

    @Override
    @Transactional
    public ReviewResponse respondToReview(Long reviewId, ReviewResponseRequest responseRequest) {
        User currentUser = authService.getCurrentUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (currentUser.getRole() != Role.ORGANIZER) {
            throw new AccessDeniedException("Only organizers can respond to reviews");
        }

        ReviewResponse response = ReviewResponse.builder()
                .content(responseRequest.getContent())
                .organizer(currentUser)
                .review(review)
                .build();

        return responseRepository.save(response);
    }

    @Override
    @Transactional
    public ReviewReport reportReview(Long reviewId, ReviewReportRequest reportRequest) {
        User currentUser = authService.getCurrentUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (reportRepository.existsByReviewIdAndReporterId(reviewId, currentUser.getId())) {
            throw new IllegalArgumentException("You have already reported this review");
        }

        ReviewReport report = ReviewReport.builder()
                .reason(reportRequest.getReason())
                .status(ReviewReport.ReportStatus.PENDING)
                .review(review)
                .reporter(currentUser)
                .build();

        ReviewReport savedReport = reportRepository.save(report);

        review.setIsReported(true);
        reviewRepository.save(review);

        eventPublisher.publishEvent(new ReviewReportedEvent(this, savedReport));

        return savedReport;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReportedReviews() {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can access reported reviews");
        }

        return reviewRepository.findByIsReportedTrue();
    }

    @Override
    @Transactional
    public void approveReport(Long reportId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can approve reports");
        }

        ReviewReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.setStatus(ReviewReport.ReportStatus.APPROVED);
        report.setAdmin(currentUser);
        reportRepository.save(report);

        Review review = report.getReview();
        review.setIsVisible(false);
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void rejectReport(Long reportId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can reject reports");
        }

        ReviewReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.setStatus(ReviewReport.ReportStatus.REJECTED);
        report.setAdmin(currentUser);
        reportRepository.save(report);

        List<ReviewReport> pendingReports = reportRepository.findByReviewId(report.getReview().getId()).stream()
                .filter(r -> r.getStatus() == ReviewReport.ReportStatus.PENDING)
                .toList();

        if (pendingReports.isEmpty()) {
            Review review = report.getReview();
            review.setIsReported(false);
            reviewRepository.save(review);
        }
    }

    @Override
    @Transactional
    public void hideReview(Long reviewId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can hide reviews");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.setIsVisible(false);
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void restoreReview(Long reviewId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can restore reviews");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.setIsVisible(true);
        reviewRepository.save(review);
    }
}
