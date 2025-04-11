package id.ac.ui.cs.advprog.eventsphere.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        // Common mock setup
    }

    @Test
    void handleEventNotFoundException() {
        // Arrange
        String errorMessage = "Event not found";
        EventNotFoundException exception = new EventNotFoundException(errorMessage);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> responseEntity = 
            exceptionHandler.handleEventNotFoundException(exception);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND.value(), responseEntity.getBody().getStatus());
        assertEquals(errorMessage, responseEntity.getBody().getMessage());
        assertNotNull(responseEntity.getBody().getTimestamp());
    }

    @Test
    void handleUnauthorizedAccessException() {
        // Arrange
        String errorMessage = "Unauthorized access";
        UnauthorizedAccessException exception = new UnauthorizedAccessException(errorMessage);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> responseEntity = 
            exceptionHandler.handleUnauthorizedAccessException(exception);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN.value(), responseEntity.getBody().getStatus());
        assertEquals(errorMessage, responseEntity.getBody().getMessage());
        assertNotNull(responseEntity.getBody().getTimestamp());
    }

    @Test
    void handleValidationExceptions() {
        // Arrange
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("event", "title", "Title is required"));
        fieldErrors.add(new FieldError("event", "price", "Price must be positive"));
        
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));

        // Act
        ResponseEntity<GlobalExceptionHandler.ValidationErrorResponse> responseEntity = 
            exceptionHandler.handleValidationExceptions(methodArgumentNotValidException);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getBody().getStatus());
        assertEquals("Validation error", responseEntity.getBody().getMessage());
        assertNotNull(responseEntity.getBody().getTimestamp());
        
        Map<String, String> errors = responseEntity.getBody().getErrors();
        assertEquals(2, errors.size());
        assertEquals("Title is required", errors.get("title"));
        assertEquals("Price must be positive", errors.get("price"));
    }

    @Test
    void handleIllegalStateException() {
        // Arrange
        String errorMessage = "Invalid state";
        IllegalStateException exception = new IllegalStateException(errorMessage);

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> responseEntity = 
            exceptionHandler.handleIllegalStateException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getBody().getStatus());
        assertEquals(errorMessage, responseEntity.getBody().getMessage());
        assertNotNull(responseEntity.getBody().getTimestamp());
    }

    @Test
    void handleGlobalException() {
        // Arrange
        Exception exception = new Exception("Some unexpected error");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> responseEntity = 
            exceptionHandler.handleGlobalException(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getBody().getStatus());
        assertEquals("An unexpected error occurred", responseEntity.getBody().getMessage());
        assertNotNull(responseEntity.getBody().getTimestamp());
    }
    
    @Test
    void errorResponse_GettersAndSetters() {
        // Arrange
        int status = 404;
        String message = "Test message";
        LocalDateTime timestamp = LocalDateTime.now();
        
        // Act
        GlobalExceptionHandler.ErrorResponse errorResponse = 
            new GlobalExceptionHandler.ErrorResponse(status, message, timestamp);
            
        // Assert - Initial values
        assertEquals(status, errorResponse.getStatus());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(timestamp, errorResponse.getTimestamp());
        
        // Act - Change values
        int newStatus = 500;
        String newMessage = "New message";
        LocalDateTime newTimestamp = LocalDateTime.now().plusHours(1);
        
        errorResponse.setStatus(newStatus);
        errorResponse.setMessage(newMessage);
        errorResponse.setTimestamp(newTimestamp);
        
        // Assert - Updated values
        assertEquals(newStatus, errorResponse.getStatus());
        assertEquals(newMessage, errorResponse.getMessage());
        assertEquals(newTimestamp, errorResponse.getTimestamp());
    }
    
    @Test
    void validationErrorResponse_GettersAndSetters() {
        // Arrange
        int status = 400;
        String message = "Validation error";
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> errors = Map.of("field1", "error1", "field2", "error2");
        
        // Act
        GlobalExceptionHandler.ValidationErrorResponse validationErrorResponse = 
            new GlobalExceptionHandler.ValidationErrorResponse(status, message, timestamp, errors);
            
        // Assert - Initial values
        assertEquals(status, validationErrorResponse.getStatus());
        assertEquals(message, validationErrorResponse.getMessage());
        assertEquals(timestamp, validationErrorResponse.getTimestamp());
        assertEquals(2, validationErrorResponse.getErrors().size());
        assertEquals("error1", validationErrorResponse.getErrors().get("field1"));
        assertEquals("error2", validationErrorResponse.getErrors().get("field2"));
        
        // Act - Change values
        Map<String, String> newErrors = Map.of("field3", "error3");
        validationErrorResponse.setErrors(newErrors);
        
        // Assert - Updated values
        assertEquals(1, validationErrorResponse.getErrors().size());
        assertEquals("error3", validationErrorResponse.getErrors().get("field3"));
    }
}