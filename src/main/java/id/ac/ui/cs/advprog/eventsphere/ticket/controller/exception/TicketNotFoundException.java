package id.ac.ui.cs.advprog.eventsphere.ticket.controller.exception;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException() {
        super("Ticket not found");
    }
}
