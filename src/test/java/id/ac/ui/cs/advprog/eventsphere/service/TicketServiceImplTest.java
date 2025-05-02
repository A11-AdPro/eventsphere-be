package id.ac.ui.cs.advprog.eventsphere.service;

import id.ac.ui.cs.advprog.eventsphere.dto.TicketRequest;
import id.ac.ui.cs.advprog.eventsphere.dto.TicketResponse;
import id.ac.ui.cs.advprog.eventsphere.exception.TicketNotFoundException;
import id.ac.ui.cs.advprog.eventsphere.model.Ticket;
import id.ac.ui.cs.advprog.eventsphere.model.TicketCategory;
import id.ac.ui.cs.advprog.eventsphere.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {

    private TicketRepository ticketRepository;
    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        ticketService = new TicketServiceImpl(ticketRepository); // ✅ Inject mock repository
    }

    @Test
    void addTicket_shouldSaveTicket() {
        TicketRequest request = new TicketRequest("VIP", 500.0, 100, TicketCategory.VIP, 1L);
        Ticket ticket = new Ticket(null, "VIP", 500.0, 100, TicketCategory.VIP, 1L); // ✅ 6 args

        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        TicketResponse savedTicket = ticketService.addTicket(request);

        assertNotNull(savedTicket);
        assertEquals("VIP", savedTicket.getName());
        assertEquals(TicketCategory.VIP, savedTicket.getCategory());
    }


    @Test
    void updateTicket_shouldUpdateTicket() {
        Long ticketId = 1L;
        Ticket existingTicket = new Ticket(ticketId, "Reguler", 200.0, 50, TicketCategory.REGULAR, 2L); // ✅ 6 args
        TicketRequest updateRequest = new TicketRequest("Reguler", 250.0, 40, TicketCategory.REGULAR, 2L);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(existingTicket));
        when(ticketRepository.save(existingTicket)).thenReturn(existingTicket);

        TicketResponse updated = ticketService.updateTicket(ticketId, updateRequest);

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

        TicketRequest request = new TicketRequest( "VIP", 500.0, 100, TicketCategory.VIP, 1L); // ✅ fixed args

        assertThrows(TicketNotFoundException.class, () -> ticketService.updateTicket(ticketId, request));
    }

    @Test
    void deleteTicket_shouldCallRepository() {
        Long ticketId = 1L;
        Ticket ticket = new Ticket(ticketId, "Reguler", 150.0, 20, TicketCategory.REGULAR, 3L); // ✅ 6 args

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        ticketService.deleteTicket(ticketId);

        verify(ticketRepository, times(1)).delete(ticket);
    }


    @Test
    void deleteTicket_shouldThrowExceptionWhenNotFound() {
        Long ticketId = 888L;
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () -> ticketService.deleteTicket(ticketId));
    }
}



