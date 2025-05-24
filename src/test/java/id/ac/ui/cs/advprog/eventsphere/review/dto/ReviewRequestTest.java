package id.ac.ui.cs.advprog.eventsphere.review.dto;

import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ReviewRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void testReviewRequestValid() {
        ReviewRequest request = new ReviewRequest();
        request.setEventId(1L);
        request.setTicketId(101L);
        request.setContent("This is a great event!");
        request.setRating(5);
        List<MultipartFile> images = new ArrayList<>();
        images.add(mock(MultipartFile.class));
        request.setImages(images);

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testReviewRequestNullEventId() {
        ReviewRequest request = new ReviewRequest();
        request.setTicketId(101L);
        request.setContent("Valid content");
        request.setRating(5);

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Event ID is required", violations.iterator().next().getMessage());
    }

    @Test
    void testReviewRequestNullTicketId() {
        ReviewRequest request = new ReviewRequest();
        request.setEventId(1L);
        request.setContent("Valid content");
        request.setRating(5);

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Ticket ID is required", violations.iterator().next().getMessage());
    }

    @Test
    void testReviewRequestBlankContent() {
        ReviewRequest request = new ReviewRequest();
        request.setEventId(1L);
        request.setTicketId(101L);
        request.setContent("");
        request.setRating(5);

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Review content cannot be empty", violations.iterator().next().getMessage());
    }

    @Test
    void testReviewRequestContentTooLong() {
        ReviewRequest request = new ReviewRequest();
        request.setEventId(1L);
        request.setTicketId(101L);
        request.setContent("a".repeat(501));
        request.setRating(5);

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Review content cannot exceed 500 characters", violations.iterator().next().getMessage());
    }

    @Test
    void testReviewRequestNullRating() {
        ReviewRequest request = new ReviewRequest();
        request.setEventId(1L);
        request.setTicketId(101L);
        request.setContent("Valid content");

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Rating is required", violations.iterator().next().getMessage());
    }

    @Test
    void testReviewRequestRatingTooLow() {
        ReviewRequest request = new ReviewRequest();
        request.setEventId(1L);
        request.setTicketId(101L);
        request.setContent("Valid content");
        request.setRating(0);

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Rating must be at least 1", violations.iterator().next().getMessage());
    }

    @Test
    void testReviewRequestRatingTooHigh() {
        ReviewRequest request = new ReviewRequest();
        request.setEventId(1L);
        request.setTicketId(101L);
        request.setContent("Valid content");
        request.setRating(6);

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Rating cannot be more than 5", violations.iterator().next().getMessage());
    }

    @Test
    void testReviewRequestImagesCanBeNull() {
        ReviewRequest request = new ReviewRequest();
        request.setEventId(1L);
        request.setTicketId(101L);
        request.setContent("Valid content");
        request.setRating(5);
        request.setImages(null); // Images are optional

        Set<ConstraintViolation<ReviewRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testGettersAndSetters() {
        ReviewRequest request = new ReviewRequest();
        Long eventId = 1L;
        Long ticketId = 101L;
        String content = "Test content";
        Integer rating = 4;
        List<MultipartFile> images = new ArrayList<>();
        images.add(mock(MultipartFile.class));

        request.setEventId(eventId);
        request.setTicketId(ticketId);
        request.setContent(content);
        request.setRating(rating);
        request.setImages(images);

        assertEquals(eventId, request.getEventId());
        assertEquals(ticketId, request.getTicketId());
        assertEquals(content, request.getContent());
        assertEquals(rating, request.getRating());
        assertEquals(images, request.getImages());
    }
}

