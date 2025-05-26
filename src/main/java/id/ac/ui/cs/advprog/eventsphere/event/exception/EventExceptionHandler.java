package id.ac.ui.cs.advprog.eventsphere.event.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice("id.ac.ui.cs.advprog.eventsphere.event")
public class EventExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(EventExceptionHandler.class);

    // ========== CUSTOM EXCEPTIONS ==========
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            WebRequest request) {
        
        String errorMessage = "You don't have permission to access this resource. " +
                "Please contact your administrator if you believe this is an error.";
        
        return buildErrorResponse(
            ex,
            HttpStatus.FORBIDDEN,
            "ACCESS_DENIED",
            errorMessage, 
            request
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            Exception ex,
            HttpStatus status,
            String errorCode,
            String customMessage,
            WebRequest request) {
        
        ErrorResponse response = new ErrorResponse(
            status.value(),
            customMessage,  // Gunakan custom message
            LocalDateTime.now(),
            errorCode,
            request.getDescription(false)
        );
        
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            WebRequest request) {
        return buildErrorResponse(
            ex,
            HttpStatus.UNAUTHORIZED,
            "AUTHENTICATION_FAILED",
            request
        );
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEventNotFoundException(
            EventNotFoundException ex, 
            WebRequest request) {
        return buildErrorResponse(
            ex, 
            HttpStatus.NOT_FOUND, 
            "EVENT_NOT_FOUND",
            request
        );
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccessException(
            UnauthorizedAccessException ex, 
            WebRequest request) {
        return buildErrorResponse(
            ex, 
            HttpStatus.FORBIDDEN, 
            "UNAUTHORIZED_ACCESS",
            request
        );
    }

    @ExceptionHandler({IllegalStateException.class, EventModificationException.class})
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolations(
            RuntimeException ex, 
            WebRequest request) {
        return buildErrorResponse(
            ex, 
            HttpStatus.BAD_REQUEST, 
            "BUSINESS_RULE_VIOLATION",
            request
        );
    }

    @ExceptionHandler(EventConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            EventConflictException ex, 
            WebRequest request) {
        return buildErrorResponse(
            ex, 
            HttpStatus.CONFLICT, 
            "EVENT_CONFLICT",
            request
        );
    }

    // ========== VALIDATION EXCEPTION ==========
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, 
            WebRequest request) {
        
        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage,
                (existing, replacement) -> existing
            ));

        ValidationErrorResponse response = new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validation failed",
            LocalDateTime.now(),
            errors,
            "VALIDATION_ERROR",
            request.getDescription(false)
        );

        return ResponseEntity.badRequest().body(response);
    }

    // ========== GLOBAL EXCEPTION CATCHER ==========
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, 
            WebRequest request) {
        
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        return buildErrorResponse(
            new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                LocalDateTime.now(),
                "INTERNAL_SERVER_ERROR",
                request.getDescription(false)
            ),
            HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    // ========== HELPER METHODS ==========
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            Exception ex, 
            HttpStatus status,
            String errorCode,
            WebRequest request) {
        
        ErrorResponse response = new ErrorResponse(
            status.value(),
            ex.getMessage(),
            LocalDateTime.now(),
            errorCode,
            request.getDescription(false)
        );
        
        return ResponseEntity.status(status).body(response);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            ErrorResponse errorResponse, 
            HttpStatus status) {
        return ResponseEntity.status(status).body(errorResponse);
    }

    // ========== ERROR RESPONSE CLASSES ==========
    public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp,
        String errorCode,
        String path
    ) {
        public ErrorResponse(int status, String message, LocalDateTime timestamp) {
            this(status, message, timestamp, null, null);
        }
    }

    public record ValidationErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp,
        Map<String, String> errors,
        String errorCode,
        String path
    ) {
        public ValidationErrorResponse(
            int status, 
            String message, 
            LocalDateTime timestamp, 
            Map<String, String> errors
        ) {
            this(status, message, timestamp, errors, "VALIDATION_ERROR", null);
        }
    }
}