package id.ac.ui.cs.advprog.eventsphere.ticket.controller.repository;

import id.ac.ui.cs.advprog.eventsphere.ticket.controller.model.Ticket;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TicketRepository {
    private final Map<Long, Ticket> tickets = new HashMap<>();
    private long nextId = 1;

    public Ticket save(Ticket ticket) {
        if (ticket.getId() == null) {
            ticket.setId(nextId++);
        }
        tickets.put(ticket.getId(), ticket);
        return ticket;
    }

    public Optional<Ticket> findById(Long id) {
        return Optional.ofNullable(tickets.get(id));
    }

    public List<Ticket> findAll() {
        return new ArrayList<>(tickets.values());
    }

    public void deleteById(Long id) {
        tickets.remove(id);
    }

    public void delete(Ticket ticket) {
        tickets.remove(ticket.getId());
    }
}

