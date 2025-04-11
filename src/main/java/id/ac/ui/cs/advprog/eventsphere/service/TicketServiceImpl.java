package id.ac.ui.cs.advprog.eventsphere.service;

import id.ac.ui.cs.advprog.eventsphere.dto.*;
import id.ac.ui.cs.advprog.eventsphere.model.Ticket;
import id.ac.ui.cs.advprog.eventsphere.repository.TicketRepository;
import id.ac.ui.cs.advprog.eventsphere.exception.TicketNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

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
        ticket.updateDetails(request.price, request.quota);
        return toResponse(repo.save(ticket));
    }

    @Override
    public List<TicketResponse> getAvailableTickets() {
        return repo.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public TicketResponse purchaseTicket(Long id) {
        Ticket ticket = repo.findById(id).orElseThrow(TicketNotFoundException::new);
        ticket.purchase();
        return toResponse(repo.save(ticket));
    }

    @Override
    public void deleteTicket(Long id) {
        repo.deleteById(id);
    }

    // ✅ Refactored to use Builder Pattern
    private TicketResponse toResponse(Ticket t) {
        return new TicketResponse.Builder()
                .id(t.getId())
                .name(t.getName())
                .price(t.getPrice())
                .quota(t.getQuota())
                .category(t.getCategory())
                .soldOut(t.isSoldOut())
                .build();
    }
}

