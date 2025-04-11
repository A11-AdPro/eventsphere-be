package id.ac.ui.cs.advprog.eventsphere.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewDto;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ReviewControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateReview() throws Exception {
        // Given
        ReviewDto reviewDto = new ReviewDto();
        reviewDto.setRating(5);
        reviewDto.setComment("Great event!");

        doNothing().when(reviewService).createReview(reviewDto.getRating(), reviewDto.getComment());

        // When & Then
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Review submitted successfully"));

        verify(reviewService).createReview(reviewDto.getRating(), reviewDto.getComment());
    }
}
