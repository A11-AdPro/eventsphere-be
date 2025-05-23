package id.ac.ui.cs.advprog.eventsphere.event.exception;

import id.ac.ui.cs.advprog.eventsphere.event.controller.EventController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.request.WebRequest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest(controllers = EventController.class)
// @Import(EventExceptionHandler.class)
@SpringBootTest
@AutoConfigureMockMvc
class EventExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private WebRequest webRequest;

    @Mock
    private EventNotFoundException eventNotFoundException;

    private EventExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new EventExceptionHandler();
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("No Access");
        var response = handler.handleAccessDeniedException(ex, webRequest);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody().message().contains("don't have permission"));
    }

    @Test
    void testHandleEventNotFoundException() {
        EventNotFoundException ex = new EventNotFoundException("Not found");
        var response = handler.handleEventNotFoundException(ex, webRequest);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().message());
    }

    @Test
    void testHandleUnauthorizedAccessException() {
        UnauthorizedAccessException ex = new UnauthorizedAccessException("Unauthorized");
        var response = handler.handleUnauthorizedAccessException(ex, webRequest);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Unauthorized", response.getBody().message());
    }
}