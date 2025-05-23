package id.ac.ui.cs.advprog.eventsphere.event.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EventConflictExceptionTest {

    @Test
    void testEventConflictException() {
        EventConflictException ex =
            new EventConflictException("Conflict message");
        assertEquals("Conflict message", ex.getMessage());
    }
}