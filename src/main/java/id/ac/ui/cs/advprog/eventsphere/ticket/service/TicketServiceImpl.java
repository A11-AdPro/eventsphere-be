package id.ac.ui.cs.advprog.eventsphere.ticket.service;

import id.ac.ui.cs.advprog.eventsphere.ticket.model.Ticket;
import id.ac.ui.cs.advprog.eventsphere.ticket.repository.TicketRepository;
import id.ac.ui.cs.advprog.eventsphere.ticket.exception.TicketNotFoundException;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketRequest;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {
    private final TicketRepository repo;

    public TicketServiceImpl(TicketRepository repo) {
        this.repo = repo;
    }

    @Override
    public TicketResponse addTicket(TicketRequest request) {
        Ticket ticket = new Ticket(null, request.name, request.price, request.quota, request.category, request.eventId);
        return toResponse(repo.save(ticket));
    }

    @Override
    public TicketResponse updateTicket(Long id, TicketRequest request) {
        Ticket ticket = repo.findById(id).orElseThrow(TicketNotFoundException::new);
        ticket.setName(request.name);
        ticket.setCategory(request.category);
        ticket.setEventId(request.eventId);
        ticket.updateDetails(request.price, request.quota);
        return toResponse(repo.save(ticket));
    }

    @Override
    public List<TicketResponse> getAvailableTickets() {
        return repo.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public TicketResponse purchaseTicket(Long id) {
        Ticket ticket = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Periksa apakah tiket sudah sold out
        if (ticket.isSoldOut()) {
            throw new RuntimeException("Ticket is sold out");
        }

        // Mengurangi kuota dengan memanggil metode purchase
        ticket.purchase(); // Ini akan menambah sold dan memeriksa apakah sold >= quota

        // Cek apakah sold sudah mencapai quota
        boolean soldOutStatus = ticket.isSoldOut();

        // Simpan perubahan tiket
        Ticket updatedTicket = repo.save(ticket);

        // Kembalikan response dengan detail tiket terbaru
        return new TicketResponse.Builder()
                .id(updatedTicket.getId())
                .name(updatedTicket.getName())
                .price(updatedTicket.getPrice())
                .quota(ticket.getQuota())  // Kuota yang terupdate
                .category(updatedTicket.getCategory())
                .soldOut(soldOutStatus)  // Status soldOut yang terupdate
                .eventId(updatedTicket.getEventId())
                .build();
    }


    @Override
    public void deleteTicket(Long ticketId) {
        Ticket ticket = repo.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException());
        repo.delete(ticket);
    }

    private TicketResponse toResponse(Ticket t) {
        return new TicketResponse.Builder()
                .id(t.getId())
                .name(t.getName())
                .price(t.getPrice())
                .quota(t.getQuota())
                .category(t.getCategory())
                .eventId(t.getEventId()) // ✅ ini wajib agar test berhasil
                .soldOut(t.isSoldOut())
                .build();
    }
}




