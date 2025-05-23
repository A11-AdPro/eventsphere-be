package id.ac.ui.cs.advprog.eventsphere.report.exception;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ReportExceptionHandlerTest {

    private final ReportExceptionHandler exceptionHandler = new ReportExceptionHandler();

    @Test
    @DisplayName("Menangani EntityNotFoundException dengan status 404 Not Found")
    public void testHandleEntityNotFoundException() {
        // Arrange
        EntityNotFoundException exception = new EntityNotFoundException("Report not found with id: 123");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleEntityNotFoundException(exception);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Report not found with id: 123", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals("404", response.getBody().getErrorCode());
    }

    @Test
    @DisplayName("Menangani IllegalArgumentException dengan status 400 Bad Request")
    public void testHandleIllegalArgumentException() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid report status");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid report status", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals("400", response.getBody().getErrorCode());
    }

    @Test
    @DisplayName("Menangani Exception generik dengan status 500 Internal Server Error")
    public void testHandleGenericException() {
        // Arrange
        Exception exception = new Exception("Unexpected error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals("500", response.getBody().getErrorCode());
    }
}