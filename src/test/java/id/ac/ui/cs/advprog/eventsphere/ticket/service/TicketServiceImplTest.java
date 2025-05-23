package id.ac.ui.cs.advprog.eventsphere.ticket.service;

import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketRequest;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketResponse;
import id.ac.ui.cs.advprog.eventsphere.ticket.exception.TicketNotFoundException;
import id.ac.ui.cs.advprog.eventsphere.ticket.model.Ticket;
import id.ac.ui.cs.advprog.eventsphere.ticket.model.TicketCategory;
import id.ac.ui.cs.advprog.eventsphere.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {

    private TicketRepository ticketRepository;
    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        ticketService = new TicketServiceImpl(ticketRepository);
    }

    @Test
    void addTicket_shouldSaveTicket() throws ExecutionException, InterruptedException {
        TicketRequest request = new TicketRequest("VIP", 500.0, 100, TicketCategory.VIP, 1L);
        Ticket ticket = new Ticket(null, "VIP", 500.0, 100, TicketCategory.VIP, 1L);

        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        CompletableFuture<TicketResponse> future = ticketService.addTicket(request);
        TicketResponse savedTicket = future.get(); // ✅ .get() sudah di-handle dengan throws

        assertNotNull(savedTicket);
        assertEquals("VIP", savedTicket.getName());
        assertEquals(TicketCategory.VIP, savedTicket.getCategory());
    }

    @Test

    void updateTicket_shouldUpdateTicket() {
        Long ticketId = 1L;
        Ticket existingTicket = new Ticket(ticketId, "Reguler", 200.0, 50, TicketCategory.REGULAR, 2L);
        TicketRequest updateRequest = new TicketRequest("Reguler", 250.0, 40, TicketCategory.REGULAR, 2L);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(existingTicket));
        when(ticketRepository.save(existingTicket)).thenReturn(existingTicket);

        TicketResponse updated = ticketService.updateTicket(ticketId, updateRequest).join(); // ✅ fix

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

        assertThrows(TicketNotFoundException.class, () -> ticketService.updateTicket(ticketId, request));
    }

    @Test
    void deleteTicket_shouldCallRepository() throws ExecutionException, InterruptedException {
        Long ticketId = 1L;
        Ticket ticket = new Ticket(ticketId, "Reguler", 150.0, 20, TicketCategory.REGULAR, 3L);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        ticketService.deleteTicket(ticketId).get(); // ✅ add get()

        verify(ticketRepository, times(1)).delete(ticket);
    }

    @Test
    void deleteTicket_shouldThrowExceptionWhenNotFound() {
        Long ticketId = 888L;
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // Karena orElseThrow langsung dilempar saat method dipanggil
        assertThrows(TicketNotFoundException.class, () -> {
            ticketService.deleteTicket(ticketId);
        });
    }


}





