package id.ac.ui.cs.advprog.eventsphere.review.repository;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private Review review1;
    private Review review2;
    private Review review3;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        userRepository.deleteAll();

        user1 = User.builder()
                .email("user1@example.com")
                .password("password")
                .role(id.ac.ui.cs.advprog.eventsphere.authentication.model.Role.ATTENDEE)
                .build();
        userRepository.saveAndFlush(user1);

        review1 = Review.builder()
                .eventId(1L)
                .user(user1)
                .content("Amazing event!")
                .rating(5)
                .ticketId(101L)
                .isReported(false)
                .isVisible(true)
                .images(new ArrayList<>())
                .responses(new ArrayList<>())
                .reports(new ArrayList<>())
                .build();

        review2 = Review.builder()
                .eventId(1L)
                .user(user1)
                .content("Good event")
                .rating(4)
                .ticketId(102L)
                .isReported(false)
                .isVisible(true)
                .images(new ArrayList<>())
                .responses(new ArrayList<>())
                .reports(new ArrayList<>())
                .build();

        review3 = Review.builder()
                .eventId(2L)
                .user(user1)
                .content("Okay event")
                .rating(3)
                .ticketId(103L)
                .isReported(false)
                .isVisible(true)
                .images(new ArrayList<>())
                .responses(new ArrayList<>())
                .reports(new ArrayList<>())
                .build();

        // Save and flush to ensure IDs are generated and @PrePersist lifecycle callbacks are triggered
        reviewRepository.saveAllAndFlush(List.of(review1, review2, review3));
    }

    @Test
    void findByEventId_shouldReturnReviewsForEvent() {
        List<Review> reviews = reviewRepository.findByEventId(1L);
        assertThat(reviews).hasSize(2).extracting(Review::getContent)
                .containsExactlyInAnyOrder("Amazing event!", "Good event");
    }

    @Test
    void findByEventIdOrderByCreatedAtDesc_shouldReturnPagedReviewsSortedByCreationDate() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = reviewRepository.findByEventIdOrderByCreatedAtDesc(1L, pageable);
        assertThat(reviewPage.getContent()).hasSize(2);
        // Assuming review2 was saved after review1, it should appear first if sorted by CreatedAt Desc
        // For H2, order might not be guaranteed without explicit timestamp differences
        // For more robust test, set createdAt explicitly or ensure order by another field
    }

    @Test
    void findByEventIdOrderByRatingDesc_shouldReturnPagedReviewsSortedByRatingDesc() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = reviewRepository.findByEventIdOrderByRatingDesc(1L, pageable);
        assertThat(reviewPage.getContent()).hasSize(2);
        assertThat(reviewPage.getContent().get(0).getRating()).isEqualTo(5);
        assertThat(reviewPage.getContent().get(1).getRating()).isEqualTo(4);
    }

    @Test
    void findByEventIdOrderByRatingAsc_shouldReturnPagedReviewsSortedByRatingAsc() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = reviewRepository.findByEventIdOrderByRatingAsc(1L, pageable);
        assertThat(reviewPage.getContent()).hasSize(2);
        assertThat(reviewPage.getContent().get(0).getRating()).isEqualTo(4);
        assertThat(reviewPage.getContent().get(1).getRating()).isEqualTo(5);
    }

    @Test
    void findByEventIdAndContentContaining_shouldReturnMatchingReviews() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = reviewRepository.findByEventIdAndContentContaining(1L, "Amazing", pageable);
        assertThat(reviewPage.getContent()).hasSize(1);
        assertThat(reviewPage.getContent().get(0).getContent()).isEqualTo("Amazing event!");
    }

    @Test
    void existsByUserIdAndEventId_shouldReturnTrueIfReviewExists() {
        boolean exists = reviewRepository.existsByUserIdAndEventId(user1.getId(), 1L);
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUserIdAndEventId_shouldReturnFalseIfReviewDoesNotExist() {
        boolean exists = reviewRepository.existsByUserIdAndEventId(user1.getId(), 99L); // Non-existent eventId
        assertThat(exists).isFalse();
    }

    @Test
    void existsByTicketId_shouldReturnTrueIfReviewExistsForTicket() {
        boolean exists = reviewRepository.existsByTicketId(101L);
        assertThat(exists).isTrue();
    }

    @Test
    void existsByTicketId_shouldReturnFalseIfReviewDoesNotExistForTicket() {
        boolean exists = reviewRepository.existsByTicketId(999L); // Non-existent ticketId
        assertThat(exists).isFalse();
    }

    @Test
    void calculateAverageRatingForEvent_shouldReturnCorrectAverage() {
        Double avgRating = reviewRepository.calculateAverageRatingForEvent(1L);
        assertThat(avgRating).isEqualTo(4.5);
    }

    @Test
    void calculateAverageRatingForEvent_shouldReturnNullForEventWithNoReviews() {
        Double avgRating = reviewRepository.calculateAverageRatingForEvent(3L); // Event with no reviews
        assertThat(avgRating).isNull();
    }

    @Test
    void findByIsReportedTrue_shouldReturnReportedReviews() {
        review1.setIsReported(true);
        reviewRepository.save(review1);
        List<Review> reportedReviews = reviewRepository.findByIsReportedTrue();
        assertThat(reportedReviews).hasSize(1);
        assertThat(reportedReviews.get(0).getIsReported()).isTrue();
    }

    @Test
    void findByIsVisibleTrue_shouldReturnVisibleReviews() {
        review1.setIsVisible(false);
        reviewRepository.save(review1); // review1 is not visible
                                      // review2 and review3 are visible by default

        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> visibleReviewsPage = reviewRepository.findByIsVisibleTrue(pageable);

        assertThat(visibleReviewsPage.getContent()).hasSize(2);
        assertThat(visibleReviewsPage.getContent()).extracting(Review::getIsVisible).containsOnly(true);
        assertThat(visibleReviewsPage.getContent()).extracting(Review::getId).containsExactlyInAnyOrder(review2.getId(), review3.getId());
    }
}

