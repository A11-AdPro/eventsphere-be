package id.ac.ui.cs.advprog.eventsphere.review.controller;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.AuthService;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewReportRequest;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewRequest;
import id.ac.ui.cs.advprog.eventsphere.review.dto.ReviewResponseRequest;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewImage;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewReport;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewResponse;
import id.ac.ui.cs.advprog.eventsphere.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private AuthService authService; // Required by SecurityConfig, even if not directly used in controller

    @Autowired
    private ObjectMapper objectMapper;

    private Review review;
    private User attendeeUser;

    @BeforeEach
    void setUp() {
        attendeeUser = User.builder().id(1L).role(Role.ATTENDEE).build();
        review = Review.builder()
                .id(1L)
                .content("Great event!")
                .rating(5)
                .eventId(101L)
                .ticketId(202L) // Ensure ticketId is set
                .user(attendeeUser)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(roles = "ATTENDEE")
    void createReview_Success() throws Exception {
        ReviewRequest request = new ReviewRequest();
        request.setEventId(101L);
        request.setTicketId(202L);
        request.setContent("Amazing!");
        request.setRating(5);

        when(reviewService.createReview(any(ReviewRequest.class))).thenReturn(review);

        mockMvc.perform(post("/api/reviews").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Great event!"));
    }

    @Test
    @WithMockUser(roles = "ATTENDEE")
    void addImagesToReview_Success() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile("images", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "test image".getBytes());
        ReviewImage reviewImage = ReviewImage.builder().id(1L).fileName("image.jpg").build();
        when(reviewService.addImagesToReview(eq(1L), anyList())).thenReturn(Collections.singletonList(reviewImage));

        mockMvc.perform(multipart("/api/reviews/1/images").file(imageFile).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("image.jpg"));
    }

    @Test
    @WithMockUser(roles = "ATTENDEE")
    void deleteImage_Success() throws Exception {
        doNothing().when(reviewService).deleteImage(1L);
        mockMvc.perform(delete("/api/reviews/images/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ATTENDEE")
    void updateReview_Success() throws Exception {
        ReviewRequest request = new ReviewRequest();
        request.setEventId(review.getEventId());
        request.setTicketId(review.getTicketId());
        request.setContent("Updated content");
        request.setRating(4);

        // Prepare the review object that the service is expected to return
        Review updatedReview = Review.builder()
                .id(1L)
                .content("Updated content")
                .rating(4)
                .eventId(review.getEventId())
                .ticketId(review.getTicketId()) // Ensure ticketId is also set here
                .user(review.getUser())
                .createdAt(review.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .isVisible(review.getIsVisible())
                .isReported(review.getIsReported())
                .images(review.getImages())
                .responses(review.getResponses())
                .reports(review.getReports())
                .build();

        when(reviewService.updateReview(eq(1L), any(ReviewRequest.class))).thenReturn(updatedReview);

        mockMvc.perform(put("/api/reviews/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated content"));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Admin can also delete
    void deleteReview_Success() throws Exception {
        doNothing().when(reviewService).deleteReview(1L);
        mockMvc.perform(delete("/api/reviews/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser // Any authenticated user can view
    void getReviewsByEventId_Success() throws Exception {
        when(reviewService.getReviewsByEventId(101L)).thenReturn(Collections.singletonList(review));
        mockMvc.perform(get("/api/reviews/event/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Great event!"));
    }

    @Test
    @WithMockUser
    void getReviewsByEventIdPaginated_Success() throws Exception {
        Page<Review> page = new PageImpl<>(Collections.singletonList(review), PageRequest.of(0,10), 1);
        when(reviewService.getReviewsByEventIdSorted(eq(101L), eq("newest"), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/reviews/event/101/paginated").param("sortBy", "newest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("Great event!"));
    }

    @Test
    @WithMockUser
    void searchReviewsByKeyword_Success() throws Exception {
        Page<Review> page = new PageImpl<>(Collections.singletonList(review), PageRequest.of(0,10), 1);
        when(reviewService.searchReviewsByKeyword(eq(101L), eq("Great"), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/reviews/event/101/search").param("keyword", "Great"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").value("Great event!"));
    }

    @Test
    @WithMockUser
    void getAverageRatingForEvent_Success() throws Exception {
        when(reviewService.getAverageRatingForEvent(101L)).thenReturn(4.5);
        mockMvc.perform(get("/api/reviews/event/101/rating"))
                .andExpect(status().isOk())
                .andExpect(content().string("4.5"));
    }

    @Test
    @WithMockUser
    void getAverageRatingForEvent_NullRating() throws Exception {
        when(reviewService.getAverageRatingForEvent(101L)).thenReturn(null);
        mockMvc.perform(get("/api/reviews/event/101/rating"))
                .andExpect(status().isOk())
                .andExpect(content().string("0.0"));
    }


    @Test
    @WithMockUser(roles = "ORGANIZER")
    void respondToReview_Success() throws Exception {
        ReviewResponseRequest request = new ReviewResponseRequest();
        request.setContent("Thanks!");
        ReviewResponse response = ReviewResponse.builder().id(1L).content("Thanks!").build();

        when(reviewService.respondToReview(eq(1L), any(ReviewResponseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/reviews/1/respond").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Thanks!"));
    }

    @Test
    @WithMockUser // Any authenticated user can report
    void reportReview_Success() throws Exception {
        ReviewReportRequest request = new ReviewReportRequest();
        request.setReason("Spam");
        ReviewReport report = ReviewReport.builder().id(1L).reason("Spam").build();

        when(reviewService.reportReview(eq(1L), any(ReviewReportRequest.class))).thenReturn(report);

        mockMvc.perform(post("/api/reviews/1/report").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").value("Spam"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getReportedReviews_Success() throws Exception {
        review.setIsReported(true);
        when(reviewService.getReportedReviews()).thenReturn(Collections.singletonList(review));
        mockMvc.perform(get("/api/reviews/reported"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isReported").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveReport_Success() throws Exception {
        doNothing().when(reviewService).approveReport(1L);
        mockMvc.perform(patch("/api/reviews/reports/1/approve").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectReport_Success() throws Exception {
        doNothing().when(reviewService).rejectReport(1L);
        mockMvc.perform(patch("/api/reviews/reports/1/reject").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void hideReview_Success() throws Exception {
        doNothing().when(reviewService).hideReview(1L);
        mockMvc.perform(patch("/api/reviews/1/hide").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void restoreReview_Success() throws Exception {
        doNothing().when(reviewService).restoreReview(1L);
        mockMvc.perform(patch("/api/reviews/1/restore").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void canEditReview_Success() throws Exception {
        when(reviewService.canEditReview(1L)).thenReturn(true);
        mockMvc.perform(get("/api/reviews/1/canEdit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canEdit").value(true));
    }
}

