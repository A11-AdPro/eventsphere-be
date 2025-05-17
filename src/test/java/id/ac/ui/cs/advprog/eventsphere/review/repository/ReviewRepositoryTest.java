package id.ac.ui.cs.advprog.eventsphere.review.repository;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ActiveProfiles("test")
public class ReviewRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewRepository reviewRepository;

    private User testUser;
    private final Long TEST_EVENT_ID = 1L;

    @BeforeEach
    void setUp() {
        // Create and persist a test user
        testUser = User.builder()
                .email("attendee@example.com")
                .password("password")
                .fullName("Test Attendee")
                .role(Role.ATTENDEE)
                .build();
        entityManager.persist(testUser);
        entityManager.flush();

        // Create and persist reviews
        Review review1 = Review.builder()
                .content("Great event!")
                .rating(5)
                .eventId(TEST_EVENT_ID)
                .user(testUser)
                .build();

        Review review2 = Review.builder()
                .content("Nice experience")
                .rating(4)
                .eventId(TEST_EVENT_ID)
                .user(testUser)
                .build();

        Review review3 = Review.builder()
                .content("Review for another event")
                .rating(3)
                .eventId(2L)
                .user(testUser)
                .build();

        entityManager.persist(review1);
        entityManager.persist(review2);
        entityManager.persist(review3);
        entityManager.flush();
    }

    @Test
    @DisplayName("Should find reviews by event ID")
    void findByEventIdShouldReturnMatchingReviews() {
        // Act
        List<Review> reviews = reviewRepository.findByEventId(TEST_EVENT_ID);

        // Assert
        assertNotNull(reviews);
        assertEquals(2, reviews.size());
        assertEquals("Great event!", reviews.get(0).getContent());
        assertEquals("Nice experience", reviews.get(1).getContent());
    }

    @Test
    @DisplayName("Should calculate average rating for an event")
    void calculateAverageRatingForEventShouldReturnCorrectValue() {
        // Act
        Double averageRating = reviewRepository.calculateAverageRatingForEvent(TEST_EVENT_ID);

        // Assert
        assertNotNull(averageRating);
        assertEquals(4.5, averageRating); // (5 + 4) / 2 = 4.5
    }

    @Test
    @DisplayName("Should return null average rating when no reviews exist")
    void calculateAverageRatingForNonExistingEventShouldReturnNull() {
        // Act
        Double averageRating = reviewRepository.calculateAverageRatingForEvent(999L);

        // Assert
        assertEquals(null, averageRating);
    }
}
