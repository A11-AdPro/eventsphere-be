package id.ac.ui.cs.advprog.eventsphere.ticket.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnauthorizedAccessExceptionTest {

    @Test
    void constructor_shouldSetCustomMessage() {
        String message = "You are not authorized to perform this action";
        UnauthorizedAccessException exception = new UnauthorizedAccessException(message);

        assertEquals(message, exception.getMessage());
    }
}
