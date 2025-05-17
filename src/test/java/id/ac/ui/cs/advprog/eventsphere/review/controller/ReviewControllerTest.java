package id.ac.ui.cs.advprog.eventsphere.review.controller;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewRequest;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import id.ac.ui.cs.advprog.eventsphere.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    private User testUser;
    private Review testReview;
    private ReviewRequest reviewRequest;
    private final Long TEST_EVENT_ID = 1L;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("attendee@example.com")
                .fullName("Test Attendee")
                .role(Role.ATTENDEE)
                .build();

        reviewRequest = new ReviewRequest();
        reviewRequest.setEventId(TEST_EVENT_ID);
        reviewRequest.setContent("Great event!");
        reviewRequest.setRating(5);

        testReview = Review.builder()
                .id(1L)
                .content("Great event!")
                .rating(5)
                .eventId(TEST_EVENT_ID)
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser
    @DisplayName("Should create review when authenticated")
    void createReviewShouldReturnCreatedStatusWhenAuthenticated() throws Exception {
        // Arrange
        when(reviewService.createReview(any(ReviewRequest.class))).thenReturn(testReview);

        // Act & Assert
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testReview.getId()))
                .andExpect(jsonPath("$.content").value(testReview.getContent()))
                .andExpect(jsonPath("$.rating").value(testReview.getRating()))
                .andExpect(jsonPath("$.eventId").value(testReview.getEventId()));
    }

    @Test
    @DisplayName("Should return unauthorized status when creating review without authentication")
    void createReviewShouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
        // Arrange
        when(reviewService.createReview(any(ReviewRequest.class))).thenReturn(testReview);

        // Act & Assert
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return bad request when review request is invalid")
    void createReviewShouldReturnBadRequestWhenInvalid() throws Exception {
        // Arrange
        ReviewRequest invalidRequest = new ReviewRequest();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get reviews by event ID")
    void getReviewsByEventIdShouldReturnListOfReviews() throws Exception {
        // Arrange
        List<Review> reviews = Arrays.asList(testReview);
        when(reviewService.getReviewsByEventId(TEST_EVENT_ID)).thenReturn(reviews);

        // Act & Assert
        mockMvc.perform(get("/api/reviews/event/{eventId}", TEST_EVENT_ID))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testReview.getId()))
                .andExpect(jsonPath("$[0].content").value(testReview.getContent()))
                .andExpect(jsonPath("$[0].rating").value(testReview.getRating()));
    }

    @Test
    @DisplayName("Should get average rating for event")
    void getAverageRatingForEventShouldReturnRating() throws Exception {
        // Arrange
        Double averageRating = 4.5;
        when(reviewService.getAverageRatingForEvent(TEST_EVENT_ID)).thenReturn(averageRating);

        // Act & Assert
        mockMvc.perform(get("/api/reviews/event/{eventId}/rating", TEST_EVENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(averageRating));
    }

    @Test
    @DisplayName("Should return zero when no ratings for event")
    void getAverageRatingForEventShouldReturnZeroWhenNull() throws Exception {
        // Arrange
        when(reviewService.getAverageRatingForEvent(TEST_EVENT_ID)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/reviews/event/{eventId}/rating", TEST_EVENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0.0));
    }
}
