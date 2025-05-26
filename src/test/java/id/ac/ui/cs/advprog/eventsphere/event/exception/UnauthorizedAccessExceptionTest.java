package id.ac.ui.cs.advprog.eventsphere.event.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UnauthorizedAccessExceptionTest {

    @Test
    void testUnauthorizedAccessException() {
        UnauthorizedAccessException ex =
            new UnauthorizedAccessException("Unauthorized");
        assertEquals("Unauthorized", ex.getMessage());
    }
}