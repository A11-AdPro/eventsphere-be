package id.ac.ui.cs.advprog.eventsphere.topup.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaymentExceptionHandlerTest {
    
    private PaymentExceptionHandler exceptionHandler;
    private WebRequest webRequest;
    
    @BeforeEach
    public void setUp() {
        exceptionHandler = new PaymentExceptionHandler();
        webRequest = mock(WebRequest.class);
    }
    
    @Test
    @DisplayName("Should handle IllegalArgumentException")
    public void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Test error message");
        
        ResponseEntity<Object> response = exceptionHandler.handleIllegalArgumentException(ex, webRequest);
        
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), body.get("error"));
        assertEquals("Test error message", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }
    
    @Test
    @DisplayName("Should handle RuntimeException with 'not found' message as NOT_FOUND")
    public void testHandleRuntimeExceptionNotFound() {
        RuntimeException ex = new RuntimeException("User not found");
        
        ResponseEntity<Object> response = exceptionHandler.handleRuntimeException(ex, webRequest);
        
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.NOT_FOUND.value(), body.get("status"));
        assertEquals(HttpStatus.NOT_FOUND.getReasonPhrase(), body.get("error"));
        assertEquals("User not found", body.get("message"));
    }
    
    @Test
    @DisplayName("Should handle other RuntimeException as INTERNAL_SERVER_ERROR")
    public void testHandleRuntimeExceptionOther() {
        RuntimeException ex = new RuntimeException("Some other error");
        
        ResponseEntity<Object> response = exceptionHandler.handleRuntimeException(ex, webRequest);
        
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.get("status"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), body.get("error"));
        assertEquals("Internal error: Some other error", body.get("message"));
    }
    
    @Test
    @DisplayName("Should handle generic Exception")
    public void testHandleAllUncaughtException() {
        Exception ex = new Exception("Generic error");
        
        ResponseEntity<Object> response = exceptionHandler.handleAllUncaughtException(ex, webRequest);
        
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.get("status"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), body.get("error"));
        assertEquals("Unexpected error: Generic error", body.get("message"));
    }
}