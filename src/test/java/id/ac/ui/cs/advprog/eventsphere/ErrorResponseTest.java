package id.ac.ui.cs.advprog.eventsphere;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void testErrorResponseConstructorAndAccessors() {
        // Arrange
        String errorCode = "E1001";
        String message = "Test error message";

        // Act
        ErrorResponse errorResponse = new ErrorResponse(errorCode, message); //

        // Assert
        assertEquals(errorCode, errorResponse.getErrorCode()); //
        assertEquals(message, errorResponse.getMessage()); //
        assertNotNull(errorResponse.getTimestamp()); //

        // Check if the timestamp is set to roughly "now"
        // Allow a small delta (e.g., a few seconds) for execution time
        assertTrue(ChronoUnit.SECONDS.between(errorResponse.getTimestamp(), LocalDateTime.now()) < 5,
                "Timestamp should be close to the current time.");
    }
}