package id.ac.ui.cs.advprog.eventsphere.review.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReviewResponseRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void testReviewResponseRequestValid() {
        ReviewResponseRequest request = new ReviewResponseRequest();
        request.setContent("Thank you for your review!");

        Set<ConstraintViolation<ReviewResponseRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testReviewResponseRequestBlankContent() {
        ReviewResponseRequest request = new ReviewResponseRequest();
        request.setContent("");

        Set<ConstraintViolation<ReviewResponseRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Response content cannot be empty", violations.iterator().next().getMessage());
    }

    @Test
    void testReviewResponseRequestContentTooLong() {
        ReviewResponseRequest request = new ReviewResponseRequest();
        request.setContent("a".repeat(201));

        Set<ConstraintViolation<ReviewResponseRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Response cannot exceed 200 characters", violations.iterator().next().getMessage());
    }

    @Test
    void testGettersAndSetters() {
        ReviewResponseRequest request = new ReviewResponseRequest();
        String content = "Test content";
        request.setContent(content);
        assertEquals(content, request.getContent());
    }
}

