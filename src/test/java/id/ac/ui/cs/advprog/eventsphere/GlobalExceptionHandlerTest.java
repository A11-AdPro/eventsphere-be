package id.ac.ui.cs.advprog.eventsphere;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidExceptionMock;

    @Mock
    private BindingResult bindingResultMock;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleEntityNotFoundException() {
        EntityNotFoundException ex = new EntityNotFoundException("Test Entity Not Found");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleEntityNotFoundException(ex); //

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("404", responseEntity.getBody().getErrorCode()); //
        assertEquals("Test Entity Not Found", responseEntity.getBody().getMessage()); //
    }

    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Test Illegal Argument");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleIllegalArgumentException(ex); //

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("400", responseEntity.getBody().getErrorCode()); //
        assertEquals("Test Illegal Argument", responseEntity.getBody().getMessage()); //
    }

    @Test
    void testHandleAuthenticationException() {
        AuthenticationException ex = new AuthenticationException("Test Auth Failed") {}; // Anonymous inner class for concrete instance
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleAuthenticationException(ex); //

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("401", responseEntity.getBody().getErrorCode()); //
        assertEquals("Authentication failed: Test Auth Failed", responseEntity.getBody().getMessage()); //
    }

    @Test
    void testHandleBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Test Bad Credentials");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleBadCredentialsException(ex); //

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("401", responseEntity.getBody().getErrorCode()); //
        assertEquals("Invalid email or password", responseEntity.getBody().getMessage()); //
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Test Access Denied");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleAccessDeniedException(ex); //

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("403", responseEntity.getBody().getErrorCode()); //
        assertEquals("Access denied: Test Access Denied", responseEntity.getBody().getMessage()); //
    }

    @Test
    void testHandleValidationExceptions() {
        FieldError fieldError = new FieldError("objectName", "fieldName", "defaultMessage");
        when(methodArgumentNotValidExceptionMock.getBindingResult()).thenReturn(bindingResultMock);
        when(bindingResultMock.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<Map<String, Object>> responseEntity = globalExceptionHandler.handleValidationExceptions(methodArgumentNotValidExceptionMock); //

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getBody().get("status")); //
        assertEquals("Validation Failed", responseEntity.getBody().get("error")); //
        assertNotNull(responseEntity.getBody().get("timestamp")); //

        @SuppressWarnings("unchecked")
        Map<String, String> validationErrors = (Map<String, String>) responseEntity.getBody().get("validationErrors"); //
        assertNotNull(validationErrors);
        assertEquals("defaultMessage", validationErrors.get("fieldName")); //
    }
    
    @Test
    void testHandleValidationExceptions_NoErrors() { // Test case where there might be no specific field errors, though BindingResult usually has some error.
        when(methodArgumentNotValidExceptionMock.getBindingResult()).thenReturn(bindingResultMock);
        when(bindingResultMock.getAllErrors()).thenReturn(Collections.emptyList()); // No field errors

        ResponseEntity<Map<String, Object>> responseEntity = globalExceptionHandler.handleValidationExceptions(methodArgumentNotValidExceptionMock); //

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), responseEntity.getBody().get("status")); //
        assertEquals("Validation Failed", responseEntity.getBody().get("error")); //

        @SuppressWarnings("unchecked")
        Map<String, String> validationErrors = (Map<String, String>) responseEntity.getBody().get("validationErrors"); //
        assertNotNull(validationErrors);
        assertTrue(validationErrors.isEmpty()); // Errors map should be empty
    }


    @Test
    void testHandleRuntimeException_NotFoundCase() {
        RuntimeException ex = new RuntimeException("Item not found here");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleRuntimeException(ex); //

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("404", responseEntity.getBody().getErrorCode()); //
        assertEquals("Item not found here", responseEntity.getBody().getMessage()); //
    }
    
    @Test
    void testHandleRuntimeException_NotFoundCase_Capitalized() {
        RuntimeException ex = new RuntimeException("Resource Not found");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleRuntimeException(ex); //

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("404", responseEntity.getBody().getErrorCode()); //
        assertEquals("Resource Not found", responseEntity.getBody().getMessage()); //
    }

    @Test
    void testHandleRuntimeException_ConflictCase_AlreadyExists() {
        RuntimeException ex = new RuntimeException("User already exists");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleRuntimeException(ex); //

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertEquals("409", responseEntity.getBody().getErrorCode()); //
        assertEquals("User already exists", responseEntity.getBody().getMessage()); //
    }
    
    @Test
    void testHandleRuntimeException_ConflictCase_AlreadyInUse() {
        RuntimeException ex = new RuntimeException("Email already in use");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleRuntimeException(ex); //

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertEquals("409", responseEntity.getBody().getErrorCode()); //
        assertEquals("Email already in use", responseEntity.getBody().getMessage()); //
    }

    @Test
    void testHandleRuntimeException_ForbiddenCase_AccessDenied() {
        RuntimeException ex = new RuntimeException("Specific Access denied for this resource");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleRuntimeException(ex); // Variable is responseEntity

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertEquals("403", responseEntity.getBody().getErrorCode());
        assertEquals("Specific Access denied for this resource", responseEntity.getBody().getMessage()); // Use responseEntity
    }

    @Test
    void testHandleRuntimeException_ForbiddenCase_ForbiddenString() {
        RuntimeException ex = new RuntimeException("Operation Forbidden");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleRuntimeException(ex); //

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertEquals("403", responseEntity.getBody().getErrorCode()); //
        assertEquals("Operation Forbidden", responseEntity.getBody().getMessage()); //
    }

    @Test
    void testHandleRuntimeException_DefaultInternalErrorCase() {
        RuntimeException ex = new RuntimeException("Some other runtime error");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleRuntimeException(ex); //

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("500", responseEntity.getBody().getErrorCode()); //
        assertEquals("Internal error: Some other runtime error", responseEntity.getBody().getMessage()); //
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Some generic unexpected error");
        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleGenericException(ex); //

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("500", responseEntity.getBody().getErrorCode()); //
        assertEquals("Unexpected error occurred", responseEntity.getBody().getMessage()); //
    }
}