package id.ac.ui.cs.advprog.eventsphere.event.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EventNotFoundExceptionTest {

    @Test
    void testEventNotFoundException() {
        EventNotFoundException ex =
            new EventNotFoundException("Not found");
        assertEquals("Not found", ex.getMessage());
    }
}