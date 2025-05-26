package id.ac.ui.cs.advprog.eventsphere.event.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito; // Import Mockito
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException; // Import
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult; // Import
import org.springframework.validation.FieldError; // Import
import org.springframework.web.bind.MethodArgumentNotValidException; // Import
import org.springframework.web.context.request.WebRequest;


import java.time.LocalDateTime;
import java.util.Collections; // Import
import java.util.List; // Import
import java.util.Map; // Import

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock; // For mocking without @Mock field if needed
import static org.mockito.Mockito.when; // For when

@SpringBootTest
@AutoConfigureMockMvc // Though MockMvc isn't directly used for these unit tests of the handler
class EventExceptionHandlerTest {

    // @Autowired
    // private MockMvc mockMvc; // Not strictly needed for these direct handler method tests

    @Mock
    private WebRequest webRequest;

    // Mocks for various exceptions
    @Mock
    private EventNotFoundException eventNotFoundExceptionMock; // Keep if used, or remove if specific instances are created

    @Mock
    private AuthenticationException authenticationExceptionMock;

    @Mock
    private IllegalStateException illegalStateExceptionMock;

    @Mock
    private EventModificationException eventModificationExceptionMock;

    @Mock
    private EventConflictException eventConflictExceptionMock;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidExceptionMock;

    @Mock
    private Exception genericExceptionMock;

    private EventExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new EventExceptionHandler();
        // Stubbing webRequest.getDescription(false) as it's used by the handlers
        when(webRequest.getDescription(false)).thenReturn("test/path");
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("No Access");
        var response = handler.handleAccessDeniedException(ex, webRequest);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("You don't have permission to access this resource."));
        assertEquals("ACCESS_DENIED", response.getBody().errorCode());
        assertEquals("test/path", response.getBody().path());
    }

    @Test
    void testHandleAuthenticationException() {
        when(authenticationExceptionMock.getMessage()).thenReturn("Auth failed");
        ResponseEntity<EventExceptionHandler.ErrorResponse> response = handler.handleAuthenticationException(authenticationExceptionMock, webRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Auth failed", response.getBody().message());
        assertEquals("AUTHENTICATION_FAILED", response.getBody().errorCode());
        assertEquals("test/path", response.getBody().path());
    }


    @Test
    void testHandleEventNotFoundException() {
        EventNotFoundException ex = new EventNotFoundException("Not found");
        var response = handler.handleEventNotFoundException(ex, webRequest);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Not found", response.getBody().message());
        assertEquals("EVENT_NOT_FOUND", response.getBody().errorCode());
    }

    @Test
    void testHandleUnauthorizedAccessException() {
        UnauthorizedAccessException ex = new UnauthorizedAccessException("Unauthorized");
        var response = handler.handleUnauthorizedAccessException(ex, webRequest);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unauthorized", response.getBody().message());
        assertEquals("UNAUTHORIZED_ACCESS", response.getBody().errorCode());
    }

    @Test
    void testHandleBusinessRuleViolations_IllegalStateException() {
        when(illegalStateExceptionMock.getMessage()).thenReturn("Illegal state");
        ResponseEntity<EventExceptionHandler.ErrorResponse> response = handler.handleBusinessRuleViolations(illegalStateExceptionMock, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Illegal state", response.getBody().message());
        assertEquals("BUSINESS_RULE_VIOLATION", response.getBody().errorCode());
    }

    @Test
    void testHandleBusinessRuleViolations_EventModificationException() {
        when(eventModificationExceptionMock.getMessage()).thenReturn("Cannot modify event");
        ResponseEntity<EventExceptionHandler.ErrorResponse> response = handler.handleBusinessRuleViolations(eventModificationExceptionMock, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Cannot modify event", response.getBody().message());
        assertEquals("BUSINESS_RULE_VIOLATION", response.getBody().errorCode());
    }

    @Test
    void testHandleConflictException() {
        when(eventConflictExceptionMock.getMessage()).thenReturn("Event conflict");
        ResponseEntity<EventExceptionHandler.ErrorResponse> response = handler.handleConflictException(eventConflictExceptionMock, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Event conflict", response.getBody().message());
        assertEquals("EVENT_CONFLICT", response.getBody().errorCode());
    }

    @Test
    void testHandleValidationException() {
        BindingResult bindingResultMock = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "defaultMessage");
        // To test the merge function with duplicates, you could add another FieldError with the same "fieldName"
        // FieldError fieldErrorDuplicate = new FieldError("objectName", "fieldName", "anotherMessageForSameField");
        // when(bindingResultMock.getFieldErrors()).thenReturn(List.of(fieldError, fieldErrorDuplicate));
        when(bindingResultMock.getFieldErrors()).thenReturn(List.of(fieldError));
        when(methodArgumentNotValidExceptionMock.getBindingResult()).thenReturn(bindingResultMock);

        ResponseEntity<EventExceptionHandler.ValidationErrorResponse> response = handler.handleValidationException(methodArgumentNotValidExceptionMock, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().message());
        assertEquals("VALIDATION_ERROR", response.getBody().errorCode());
        assertEquals("test/path", response.getBody().path());
        assertNotNull(response.getBody().errors());
        assertEquals("defaultMessage", response.getBody().errors().get("fieldName"));
        // If testing duplicates: the merge function `(existing, replacement) -> existing` means the first one wins.
    }
    
    @Test
    void testHandleValidationException_WithDuplicateFieldErrors() {
        BindingResult bindingResultMock = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("objectName", "fieldName", "message1");
        FieldError fieldError2 = new FieldError("objectName", "fieldName", "message2"); // Same field name
        FieldError fieldErrorOther = new FieldError("objectName", "otherField", "message3");

        when(bindingResultMock.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2, fieldErrorOther));
        when(methodArgumentNotValidExceptionMock.getBindingResult()).thenReturn(bindingResultMock);

        ResponseEntity<EventExceptionHandler.ValidationErrorResponse> response = handler.handleValidationException(methodArgumentNotValidExceptionMock, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().message());
        // Check that the merge function `(existing, replacement) -> existing` worked
        assertEquals("message1", response.getBody().errors().get("fieldName")); 
        assertEquals("message3", response.getBody().errors().get("otherField"));
        assertEquals(2, response.getBody().errors().size());
    }


    @Test
    void testHandleGlobalException() {
        when(genericExceptionMock.getMessage()).thenReturn("Unexpected error");
        // The logger call inside handleGlobalException will be executed.
        // You could use a testing appender if you needed to verify logger output,
        // but for coverage, just executing it is enough.
        ResponseEntity<EventExceptionHandler.ErrorResponse> response = handler.handleGlobalException(genericExceptionMock, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().message());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().errorCode());
        assertEquals("test/path", response.getBody().path());
    }

    // Tests for Record Constructors to ensure their specific logic is covered
    @Test
    void testErrorResponseThreeArgumentConstructor() {
        // This test calls the 3-argument constructor of ErrorResponse
        EventExceptionHandler.ErrorResponse response = new EventExceptionHandler.ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Test Message",
                LocalDateTime.now()
        );

        assertEquals(HttpStatus.NOT_FOUND.value(), response.status());
        assertEquals("Test Message", response.message());
        assertNotNull(response.timestamp());
        assertNull(response.errorCode()); // As per the constructor logic
        assertNull(response.path());      // As per the constructor logic
    }

    @Test
    void testValidationErrorResponseFourArgumentConstructor() {
        // This test calls the 4-argument constructor of ValidationErrorResponse
        Map<String, String> errors = Collections.singletonMap("field", "error");
        EventExceptionHandler.ValidationErrorResponse response = new EventExceptionHandler.ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Test",
                LocalDateTime.now(),
                errors
        );

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.status());
        assertEquals("Validation Test", response.message());
        assertNotNull(response.timestamp());
        assertEquals(errors, response.errors());
        assertEquals("VALIDATION_ERROR", response.errorCode()); // As per the constructor logic
        assertNull(response.path());                          // As per the constructor logic
    }
}