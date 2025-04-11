package id.ac.ui.cs.advprog.eventsphere.review.controller;

import id.ac.ui.cs.advprog.eventsphere.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
    }

    @Test
    void testCreateReview() throws Exception {
        // Given
        int rating = 5;
        String comment = "Great event!";

        doNothing().when(reviewService).createReview(rating, comment);

        // When & Then
        mockMvc.perform(post("/api/reviews")
                .param("rating", String.valueOf(rating))
                .param("comment", comment)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        verify(reviewService).createReview(rating, comment);
    }

    @Test
    void testCreateReviewWithEmptyComment() throws Exception {
        // Given
        int rating = 3;
        String comment = "";

        doNothing().when(reviewService).createReview(rating, comment);

        // When & Then
        mockMvc.perform(post("/api/reviews")
                .param("rating", String.valueOf(rating))
                .param("comment", comment)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        verify(reviewService).createReview(rating, comment);
    }
}
