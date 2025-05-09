package id.ac.ui.cs.advprog.eventsphere.ticket.exception;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException() {
        super("Ticket not found");
    }
}
