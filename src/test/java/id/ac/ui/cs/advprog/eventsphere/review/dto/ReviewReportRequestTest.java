package id.ac.ui.cs.advprog.eventsphere.review.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReviewReportRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void testReviewReportRequestValid() {
        ReviewReportRequest request = new ReviewReportRequest();
        request.setReason("This review is spam.");

        Set<ConstraintViolation<ReviewReportRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testReviewReportRequestBlankReason() {
        ReviewReportRequest request = new ReviewReportRequest();
        request.setReason("");

        Set<ConstraintViolation<ReviewReportRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Report reason cannot be empty", violations.iterator().next().getMessage());
    }

    @Test
    void testReviewReportRequestReasonTooLong() {
        ReviewReportRequest request = new ReviewReportRequest();
        request.setReason("a".repeat(501));

        Set<ConstraintViolation<ReviewReportRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Report reason cannot exceed 500 characters", violations.iterator().next().getMessage());
    }

    @Test
    void testGettersAndSetters() {
        ReviewReportRequest request = new ReviewReportRequest();
        String reason = "Test reason";
        request.setReason(reason);
        assertEquals(reason, request.getReason());
    }
}

