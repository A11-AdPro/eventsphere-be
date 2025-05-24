package id.ac.ui.cs.advprog.eventsphere.ticket.service;

import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketRequest;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketResponse;
import java.util.concurrent.CompletableFuture;

import java.util.List;

public interface TicketService {
    CompletableFuture<TicketResponse> addTicket(TicketRequest request);
    CompletableFuture<TicketResponse> updateTicket(Long id, TicketRequest request);
    CompletableFuture<List<TicketResponse>> getAvailableTickets();
    TicketResponse purchaseTicket(Long id); // tetap sync
    CompletableFuture<Void> deleteTicket(Long ticketId);
    TicketResponse getTicketById(Long id);  // tetap sync
}
