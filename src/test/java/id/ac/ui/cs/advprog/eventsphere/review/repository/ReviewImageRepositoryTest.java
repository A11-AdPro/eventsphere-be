package id.ac.ui.cs.advprog.eventsphere.review.repository;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ReviewImageRepositoryTest {

    @Autowired
    private ReviewImageRepository reviewImageRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    private Review review1;
    private ReviewImage image1;
    private ReviewImage image2;

    @BeforeEach
    void setUp() {
        reviewImageRepository.deleteAll(); // Delete in reverse order of dependency
        reviewRepository.deleteAll();
        userRepository.deleteAll();

        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .fullName("Test User") // Combined firstName and lastName
                .role(id.ac.ui.cs.advprog.eventsphere.authentication.model.Role.ATTENDEE)
                .build();
        userRepository.saveAndFlush(user);

        review1 = Review.builder()
                .eventId(1L)
                .user(user)
                .content("Test Review")
                .rating(5)
                .isReported(false) // Explicitly set non-nullable
                .isVisible(true)  // Explicitly set non-nullable
                .images(new ArrayList<>()) // Initialize collections
                .responses(new ArrayList<>())
                .reports(new ArrayList<>())
                .build();
        reviewRepository.saveAndFlush(review1);

        image1 = ReviewImage.builder().review(review1).fileName("image1.jpg").filePath("/path/to/image1.jpg").contentType("image/jpeg").build();
        image2 = ReviewImage.builder().review(review1).fileName("image2.png").filePath("/path/to/image2.png").contentType("image/png").build();
        reviewImageRepository.saveAllAndFlush(List.of(image1, image2));
    }

    @Test
    void findByReviewId_shouldReturnImagesForReview() {
        List<ReviewImage> images = reviewImageRepository.findByReviewId(review1.getId());
        assertThat(images).hasSize(2);
        assertThat(images).extracting(ReviewImage::getFileName).containsExactlyInAnyOrder("image1.jpg", "image2.png");
    }

    @Test
    void countByReviewId_shouldReturnCorrectCount() {
        int count = reviewImageRepository.countByReviewId(review1.getId());
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByReviewId_shouldReturnZeroForReviewWithNoImages() {
        Review review2 = Review.builder()
                .eventId(2L)
                .user(review1.getUser())
                .content("No images here")
                .rating(3)
                .isReported(false) // Explicitly set non-nullable
                .isVisible(true)  // Explicitly set non-nullable
                .images(new ArrayList<>()) // Initialize collections
                .responses(new ArrayList<>())
                .reports(new ArrayList<>())
                .build();
        reviewRepository.save(review2);
        int count = reviewImageRepository.countByReviewId(review2.getId());
        assertThat(count).isEqualTo(0);
    }
}

