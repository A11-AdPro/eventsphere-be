package id.ac.ui.cs.advprog.eventsphere.ticket.repository;

import id.ac.ui.cs.advprog.eventsphere.ticket.model.Ticket;
import id.ac.ui.cs.advprog.eventsphere.ticket.model.TicketCategory;
import id.ac.ui.cs.advprog.eventsphere.event.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TicketRepositoryTest {

    private TicketRepository ticketRepository;
    private Event dummyEvent;

    @BeforeEach
    void setUp() {
        ticketRepository = new TicketRepository();
        dummyEvent = Event.builder().id(1L).title("Test Event").build();
    }

    @Test
    void testSaveNewTicket_AssignsId() {
        Ticket ticket = Ticket.builder()
                .name("VIP Ticket")
                .price(500.0)
                .quota(100)
                .category(TicketCategory.VIP)
                .event(dummyEvent)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        assertNotNull(savedTicket.getId());
        assertEquals(ticket, savedTicket);
    }

    @Test
    void testFindById_Found() {
        Ticket ticket = Ticket.builder()
                .name("Regular Ticket")
                .price(100.0)
                .quota(200)
                .category(TicketCategory.REGULAR)
                .event(dummyEvent)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        Optional<Ticket> foundTicket = ticketRepository.findById(savedTicket.getId());
        assertTrue(foundTicket.isPresent());
        assertEquals(savedTicket, foundTicket.get());
    }

    @Test
    void testFindById_NotFound() {
        Optional<Ticket> foundTicket = ticketRepository.findById(999L);
        assertTrue(foundTicket.isEmpty());
    }

    @Test
    void testFindAll_ReturnsAllTickets() {
        Ticket ticket1 = ticketRepository.save(Ticket.builder()
                .name("Ticket 1")
                .price(150.0)
                .quota(50)
                .category(TicketCategory.REGULAR)
                .event(dummyEvent)
                .build());

        Ticket ticket2 = ticketRepository.save(Ticket.builder()
                .name("Ticket 2")
                .price(250.0)
                .quota(30)
                .category(TicketCategory.VIP)
                .event(dummyEvent)
                .build());

        List<Ticket> allTickets = ticketRepository.findAll();
        assertEquals(2, allTickets.size());
        assertTrue(allTickets.contains(ticket1));
        assertTrue(allTickets.contains(ticket2));
    }

    @Test
    void testDeleteById_RemovesTicket() {
        Ticket ticket = ticketRepository.save(Ticket.builder()
                .name("Ticket To Delete")
                .price(300.0)
                .quota(10)
                .category(TicketCategory.REGULAR)
                .event(dummyEvent)
                .build());

        ticketRepository.deleteById(ticket.getId());
        assertTrue(ticketRepository.findById(ticket.getId()).isEmpty());
    }

    @Test
    void testDelete_RemovesTicket() {
        Ticket ticket = ticketRepository.save(Ticket.builder()
                .name("Another Ticket To Delete")
                .price(400.0)
                .quota(20)
                .category(TicketCategory.VIP)
                .event(dummyEvent)
                .build());

        ticketRepository.delete(ticket);
        assertTrue(ticketRepository.findById(ticket.getId()).isEmpty());
    }
}
