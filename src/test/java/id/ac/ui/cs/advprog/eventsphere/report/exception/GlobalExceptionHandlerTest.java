package id.ac.ui.cs.advprog.eventsphere.report.exception;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    public void testHandleEntityNotFoundException() {
        // Create exception
        EntityNotFoundException exception = new EntityNotFoundException("Report not found with id: 123");

        // Handle exception
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleEntityNotFoundException(exception);

        // Verify response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Report not found with id: 123", response.getBody().getMessage());
        assertEquals("404", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleIllegalArgumentException() {
        // Create exception
        IllegalArgumentException exception = new IllegalArgumentException("Invalid report status");

        // Handle exception
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception);

        // Verify response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid report status", response.getBody().getMessage());
        assertEquals("400", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleIoException() {
        // Create exception
        IOException exception = new IOException("Error processing file");

        // Handle exception
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIoException(exception);

        // Verify response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error processing file", response.getBody().getMessage());
        assertEquals("500", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleMaxUploadSizeExceededException() {
        // Create exception
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(1000000);

        // Handle exception
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMaxUploadSizeExceededException(exception);

        // Verify response
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("File size exceeds maximum limit"));
        assertEquals("413", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleGenericException() {
        // Create exception
        Exception exception = new Exception("Unexpected error");

        // Handle exception
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

        // Verify response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", response.getBody().getMessage());
        assertEquals("500", response.getBody().getErrorCode());
    }
}