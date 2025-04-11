package id.ac.ui.cs.advprog.eventsphere.event.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EventNotFoundExceptionTest {

    @Test
    void constructor_ShouldSetMessage() {
        // Arrange
        String errorMessage = "Event with id 1 not found";
        
        // Act
        EventNotFoundException exception = new EventNotFoundException(errorMessage);
        
        // Assert
        assertEquals(errorMessage, exception.getMessage());
    }
    
    @Test
    void exception_ShouldBeRuntimeException() {
        // Act
        EventNotFoundException exception = new EventNotFoundException("test");
        
        // Assert
        assertTrue(exception instanceof RuntimeException);
    }
}