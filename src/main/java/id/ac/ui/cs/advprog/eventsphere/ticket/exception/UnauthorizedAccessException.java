package id.ac.ui.cs.advprog.eventsphere.ticket.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}