package id.ac.ui.cs.advprog.eventsphere.ticket.service;

import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketRequest;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketResponse;

import java.util.List;

public interface TicketService {
    TicketResponse addTicket(TicketRequest request);
    TicketResponse updateTicket(Long id, TicketRequest request);
    List<TicketResponse> getAvailableTickets();
    TicketResponse purchaseTicket(Long id);
    void deleteTicket(Long id);
    TicketResponse getTicketById(Long id);
}
