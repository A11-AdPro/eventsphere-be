package id.ac.ui.cs.advprog.eventsphere.ticket.service;

import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketRequest;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketResponse;
import id.ac.ui.cs.advprog.eventsphere.ticket.exception.TicketNotFoundException;
import id.ac.ui.cs.advprog.eventsphere.ticket.model.Ticket;
import id.ac.ui.cs.advprog.eventsphere.ticket.model.TicketCategory;
import id.ac.ui.cs.advprog.eventsphere.ticket.repository.TicketRepository;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.event.model.Event;
import id.ac.ui.cs.advprog.eventsphere.event.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {

    private EventRepository eventRepository;
    private TicketRepository ticketRepository;
    private TicketServiceImpl ticketService;
    private User organizer;
    private User admin;
    private Event dummyEvent;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        eventRepository = mock(EventRepository.class); // Tambahan
        ticketService = new TicketServiceImpl(ticketRepository, eventRepository); // Tambahan argumen

        organizer = new User();
        organizer.setRole(Role.ORGANIZER);

        admin = new User();
        admin.setRole(Role.ADMIN);

        dummyEvent = new Event();
        dummyEvent.setId(1L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(dummyEvent));// agar constructor Ticket tak error
        // tambahan supaya eventId 2L juga valid
        Event dummyEvent2 = new Event();
        dummyEvent2.setId(2L);
        when(eventRepository.findById(2L)).thenReturn(Optional.of(dummyEvent2));
    }


    @Test
    void addTicket_shouldSaveTicket() {
        TicketRequest request = new TicketRequest("VIP", 500.0, 100, TicketCategory.VIP, 1L);
        Ticket ticket = new Ticket(null, "VIP", 500.0, 100, TicketCategory.VIP, dummyEvent);

        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketResponse savedTicket = ticketService.addTicket(request, organizer);

        assertNotNull(savedTicket);
        assertEquals("VIP", savedTicket.getName());
        assertEquals(TicketCategory.VIP, savedTicket.getCategory());
    }

    @Test
    void updateTicket_shouldUpdateTicket() {
        Long ticketId = 1L;
        Ticket existingTicket = new Ticket(ticketId, "Reguler", 200.0, 50, TicketCategory.REGULAR, dummyEvent);
        TicketRequest updateRequest = new TicketRequest("Reguler", 250.0, 40, TicketCategory.REGULAR, 2L);

        // Mock event for updated event id 2L
        Event dummyEvent2 = new Event();
        dummyEvent2.setId(2L);
        when(eventRepository.findById(2L)).thenReturn(Optional.of(dummyEvent2));

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(existingTicket));
        when(ticketRepository.save(existingTicket)).thenReturn(existingTicket);

        TicketResponse updated = ticketService.updateTicket(ticketId, updateRequest, organizer);

        assertEquals(250.0, updated.getPrice());
        assertEquals(40, updated.getQuota());
        assertEquals("Reguler", updated.getName());
        assertEquals(TicketCategory.REGULAR, updated.getCategory());
        assertEquals(2L, updated.getEventId());
    }


    @Test
    void updateTicket_shouldThrowExceptionWhenNotFound() {
        Long ticketId = 999L;
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        TicketRequest request = new TicketRequest("VIP", 500.0, 100, TicketCategory.VIP, 1L);

        assertThrows(TicketNotFoundException.class, () -> ticketService.updateTicket(ticketId, request, organizer));
    }

    @Test
    void deleteTicket_shouldCallRepository() {
        Long ticketId = 1L;
        Ticket ticket = new Ticket(ticketId, "Reguler", 150.0, 20, TicketCategory.REGULAR, dummyEvent);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        String result = ticketService.deleteTicket(ticketId, admin);

        verify(ticketRepository, times(1)).delete(ticket);
        assertEquals("Tiket dengan ID 1 berhasil dihapus.", result);
    }

    @Test
    void deleteTicket_shouldThrowExceptionWhenNotFound() {
        Long ticketId = 888L;
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () -> ticketService.deleteTicket(ticketId, admin));
    }
}






