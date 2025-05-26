package id.ac.ui.cs.advprog.eventsphere.ticket.service;

import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketRequest;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketResponse;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;

import java.util.List;

public interface TicketService {

    // Organizer bisa menambahkan tiket, perlu User untuk otorisasi
    TicketResponse addTicket(TicketRequest request, User organizer);

    // Organizer update tiket, perlu User untuk otorisasi
    TicketResponse updateTicket(Long id, TicketRequest request, User organizer);

    // Attendee bisa melihat tiket yang tersedia
    List<TicketResponse> getAvailableTickets();

    // Attendee bisa beli tiket, sync
    TicketResponse purchaseTicket(Long id);

    // Admin bisa hapus tiket, perlu User untuk otorisasi (admin)
    String deleteTicket(Long id, User admin);

    // Mendapatkan tiket by id, sync
    TicketResponse getTicketById(Long id);
}
