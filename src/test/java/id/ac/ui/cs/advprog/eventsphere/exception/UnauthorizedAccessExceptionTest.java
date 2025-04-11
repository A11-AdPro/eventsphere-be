package id.ac.ui.cs.advprog.eventsphere.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UnauthorizedAccessExceptionTest {

    @Test
    void constructor_ShouldSetMessage() {
        // Arrange
        String errorMessage = "User does not have permission to access this resource";
        
        // Act
        UnauthorizedAccessException exception = new UnauthorizedAccessException(errorMessage);
        
        // Assert
        assertEquals(errorMessage, exception.getMessage());
    }
    
    @Test
    void exception_ShouldBeRuntimeException() {
        // Act
        UnauthorizedAccessException exception = new UnauthorizedAccessException("test");
        
        // Assert
        assertTrue(exception instanceof RuntimeException);
    }
}