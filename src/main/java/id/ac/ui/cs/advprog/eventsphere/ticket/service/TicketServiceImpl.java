package id.ac.ui.cs.advprog.eventsphere.ticket.service;

import id.ac.ui.cs.advprog.eventsphere.ticket.model.Ticket;
import id.ac.ui.cs.advprog.eventsphere.ticket.repository.TicketRepository;
import id.ac.ui.cs.advprog.eventsphere.ticket.exception.TicketNotFoundException;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketRequest;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketResponse;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.event.repository.EventRepository;
import id.ac.ui.cs.advprog.eventsphere.event.model.Event;
import id.ac.ui.cs.advprog.eventsphere.event.exception.EventNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    private final TicketRepository repo;
    private final EventRepository eventRepository;

    @Override
    public TicketResponse addTicket(TicketRequest request, User organizer) {
        if (!Role.ORGANIZER.equals(organizer.getRole())) {
            throw new RuntimeException("Hanya organizer yang dapat menambahkan tiket.");
        }

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event tidak ditemukan"));

        Ticket ticket = Ticket.builder()
                .name(request.getName())
                .price(request.getPrice())
                .quota(request.getQuota())
                .category(request.getCategory())
                .event(event)
                .sold(0)
                .deleted(false)
                .build();

        return toResponse(repo.save(ticket));
    }

    @Override
    public TicketResponse updateTicket(Long id, TicketRequest request, User organizer) {
        try {
            if (!Role.ORGANIZER.equals(organizer.getRole())) {
                throw new RuntimeException("Hanya organizer yang dapat mengupdate tiket.");
            }

            Ticket ticket = repo.findById(id).orElseThrow(TicketNotFoundException::new);
            ticket.setName(request.getName());
            ticket.setCategory(request.getCategory());
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + request.getEventId()));
            ticket.setEvent(event);
            ticket.updateDetails(request.getPrice(), request.getQuota());

            return toResponse(repo.save(ticket));
        } catch (Exception e) {
            System.err.println("Error updating ticket: " + e.getMessage());
            e.printStackTrace();
            throw e; // rethrow biar tetap error, tapi kita bisa lihat log dulu
        }
    }

    @Override
    public List<TicketResponse> getAvailableTickets() {
        return repo.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TicketResponse purchaseTicket(Long id) {
        Ticket ticket = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket tidak ditemukan"));

        if (ticket.isSoldOut()) {
            throw new RuntimeException("Tiket sudah habis terjual.");
        }

        ticket.purchase();
        return toResponse(repo.save(ticket));
    }

    @Override
    public String deleteTicket(Long id, User admin) {
        if (!Role.ADMIN.equals(admin.getRole())) {
            throw new RuntimeException("Hanya admin yang dapat menghapus tiket.");
        }

        Ticket ticket = repo.findById(id)
                .orElseThrow(TicketNotFoundException::new);
        repo.delete(ticket);

        return "Tiket dengan ID " + id + " berhasil dihapus.";
    }

    @Override
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = repo.findById(id)
                .orElseThrow(TicketNotFoundException::new);
        return toResponse(ticket);
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse.Builder()
                .id(ticket.getId())
                .name(ticket.getName())
                .price(ticket.getPrice())
                .quota(ticket.getQuota())
                .category(ticket.getCategory())
                .eventId(ticket.getEventId())
                .soldOut(ticket.isSoldOut())
                .build();
    }
}





