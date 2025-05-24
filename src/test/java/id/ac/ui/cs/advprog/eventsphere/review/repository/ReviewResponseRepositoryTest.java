package id.ac.ui.cs.advprog.eventsphere.review.repository;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewResponse;
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
class ReviewResponseRepositoryTest {

    @Autowired
    private ReviewResponseRepository reviewResponseRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    private User organizerUser;
    private Review review1;
    private ReviewResponse response1;
    private ReviewResponse response2;

    @BeforeEach
    void setUp() {
        reviewResponseRepository.deleteAll();
        reviewRepository.deleteAll();
        userRepository.deleteAll();

        organizerUser = User.builder()
                .email("organizer@example.com")
                .password("password")
                .fullName("Organizer User") // Combined firstName and lastName
                .role(id.ac.ui.cs.advprog.eventsphere.authentication.model.Role.ORGANIZER)
                .build();
        userRepository.saveAndFlush(organizerUser);

        User reviewAuthor = User.builder()
                .email("author@example.com")
                .password("password")
                .fullName("Author User") // Combined firstName and lastName
                .role(id.ac.ui.cs.advprog.eventsphere.authentication.model.Role.ATTENDEE)
                .build();
        userRepository.saveAndFlush(reviewAuthor);

        review1 = Review.builder()
                .eventId(1L)
                .user(reviewAuthor)
                .content("A review")
                .rating(4)
                .isReported(false) // Explicitly set non-nullable
                .isVisible(true)  // Explicitly set non-nullable
                .images(new ArrayList<>()) // Initialize collections
                .responses(new ArrayList<>())
                .reports(new ArrayList<>())
                .build();
        reviewRepository.saveAndFlush(review1);

        response1 = ReviewResponse.builder().review(review1).organizer(organizerUser).content("Thank you!").build();
        response2 = ReviewResponse.builder().review(review1).organizer(organizerUser).content("We appreciate your feedback.").build();
        reviewResponseRepository.saveAllAndFlush(List.of(response1, response2));
    }

    @Test
    void findByReviewId_shouldReturnResponsesForReview() {
        List<ReviewResponse> responses = reviewResponseRepository.findByReviewId(review1.getId());
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ReviewResponse::getContent)
                .containsExactlyInAnyOrder("Thank you!", "We appreciate your feedback.");
    }

    @Test
    void findByReviewId_shouldReturnEmptyListForReviewWithNoResponses() {
        Review review2 = Review.builder()
                .eventId(2L)
                .user(review1.getUser())
                .content("Another review")
                .rating(5)
                .isReported(false) // Explicitly set non-nullable
                .isVisible(true)  // Explicitly set non-nullable
                .images(new ArrayList<>()) // Initialize collections
                .responses(new ArrayList<>())
                .reports(new ArrayList<>())
                .build();
        reviewRepository.save(review2);
        List<ReviewResponse> responses = reviewResponseRepository.findByReviewId(review2.getId());
        assertThat(responses).isEmpty();
    }
}

