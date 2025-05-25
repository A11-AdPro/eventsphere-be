
package id.ac.ui.cs.advprog.eventsphere.ticket.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketNotFoundExceptionTest {

    @Test
    void constructor_shouldSetDefaultMessage() {
        TicketNotFoundException exception = new TicketNotFoundException();

        assertEquals("Ticket not found", exception.getMessage());
    }
}

