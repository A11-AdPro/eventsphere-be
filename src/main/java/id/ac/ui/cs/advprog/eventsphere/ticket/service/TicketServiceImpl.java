package id.ac.ui.cs.advprog.eventsphere.ticket.service;

import id.ac.ui.cs.advprog.eventsphere.ticket.model.Ticket;
import id.ac.ui.cs.advprog.eventsphere.ticket.repository.TicketRepository;
import id.ac.ui.cs.advprog.eventsphere.ticket.exception.TicketNotFoundException;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.event.exception.EventNotFoundException;
import id.ac.ui.cs.advprog.eventsphere.event.service.EventService;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketRequest;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {
    private final TicketRepository repo;
    private final EventService eventService;

    public TicketServiceImpl(TicketRepository repo, EventService eventService) {
        this.repo = repo;
        this.eventService = eventService;
    }

    @Async
    @Override
    public CompletableFuture<TicketResponse> addTicket(TicketRequest request) {
        try {
            EventResponseDTO event = eventService.getActiveEventById(request.eventId);
        } catch (EventNotFoundException e) {
            throw new RuntimeException("Cannot create ticket for non-existent event");
        }

        Ticket ticket = new Ticket(null, request.name, request.price, request.quota, request.category, request.eventId);
        return CompletableFuture.completedFuture(toResponse(repo.save(ticket)));
    }

    @Async
    @Override
    public CompletableFuture<TicketResponse> updateTicket(Long id, TicketRequest request) {
        Ticket ticket = repo.findById(id).orElseThrow(TicketNotFoundException::new);
        ticket.setName(request.name);
        ticket.setCategory(request.category);
        ticket.setEventId(request.eventId);
        ticket.updateDetails(request.price, request.quota);
        return CompletableFuture.completedFuture(toResponse(repo.save(ticket)));
    }

    @Async
    @Override
    public CompletableFuture<List<TicketResponse>> getAvailableTickets() {
        List<TicketResponse> result = repo.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public TicketResponse purchaseTicket(Long id) {
        Ticket ticket = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        EventResponseDTO event = eventService.getActiveEventById(ticket.getEventId());
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot purchase ticket for past event");
        }
        
        if (ticket.isSoldOut()) {
            throw new RuntimeException("Ticket is sold out");
        }

        ticket.purchase();
        boolean soldOutStatus = ticket.isSoldOut();
        Ticket updatedTicket = repo.save(ticket);

        return new TicketResponse.Builder()
                .id(updatedTicket.getId())
                .name(updatedTicket.getName())
                .price(updatedTicket.getPrice())
                .quota(ticket.getQuota())
                .category(updatedTicket.getCategory())
                .soldOut(soldOutStatus)
                .eventId(updatedTicket.getEventId())
                .build();
    }

    @Async
    @Override
    public CompletableFuture<Void> deleteTicket(Long ticketId) {
        Ticket ticket = repo.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException());
        repo.delete(ticket);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = repo.findById(id)
                .orElseThrow(() -> new TicketNotFoundException());
        return toResponse(ticket);
    }

    private TicketResponse toResponse(Ticket t) {
        return new TicketResponse.Builder()
                .id(t.getId())
                .name(t.getName())
                .price(t.getPrice())
                .quota(t.getQuota())
                .category(t.getCategory())
                .eventId(t.getEventId())
                .soldOut(t.isSoldOut())
                .build();
    }
}









