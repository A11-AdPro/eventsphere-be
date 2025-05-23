package id.ac.ui.cs.advprog.eventsphere.event.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EventModificationExceptionTest {

    @Test
    void testEventModificationException() {
        EventModificationException ex =
            new EventModificationException("Modification error");
        assertEquals("Modification error", ex.getMessage());
    }
}