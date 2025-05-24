package id.ac.ui.cs.advprog.eventsphere.ticket.service;

import id.ac.ui.cs.advprog.eventsphere.event.dto.EventResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.event.service.EventService;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketRequest;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketResponse;
import id.ac.ui.cs.advprog.eventsphere.ticket.exception.TicketNotFoundException;
import id.ac.ui.cs.advprog.eventsphere.ticket.model.Ticket;
import id.ac.ui.cs.advprog.eventsphere.ticket.model.TicketCategory;
import id.ac.ui.cs.advprog.eventsphere.ticket.repository.TicketRepository;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {

    private TicketRepository ticketRepository;
    private EventService eventService;
    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        eventService = mock(EventService.class);
        ticketService = new TicketServiceImpl(ticketRepository, eventService);
    }

    @Test
    void addTicket_shouldSaveTicket() throws ExecutionException, InterruptedException {
        // Arrange
        TicketRequest request = new TicketRequest("VIP", 500.0, 100, TicketCategory.VIP, 1L);
        Ticket ticket = new Ticket(1L, "VIP", 500.0, 100, TicketCategory.VIP, 1L);

        // Mock event service to return a valid event
        EventResponseDTO mockEvent = new EventResponseDTO();
        mockEvent.setId(1L);
        mockEvent.setTitle("Test Event");
        mockEvent.setEventDate(LocalDateTime.now().plusDays(1));
        mockEvent.setActive(true);
        
        when(eventService.getActiveEventById(1L)).thenReturn(mockEvent);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        // Act
        CompletableFuture<TicketResponse> future = ticketService.addTicket(request);
        TicketResponse savedTicket = future.get();

        // Assert
        assertNotNull(savedTicket);
        assertEquals("VIP", savedTicket.getName());
        assertEquals(TicketCategory.VIP, savedTicket.getCategory());
        assertEquals(1L, savedTicket.getEventId());
        
        verify(eventService, times(1)).getActiveEventById(1L);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void addTicket_shouldThrowExceptionForNonExistentEvent() {
        // Arrange
        TicketRequest request = new TicketRequest("VIP", 500.0, 100, TicketCategory.VIP, 999L);
        
        when(eventService.getActiveEventById(999L))
            .thenThrow(new RuntimeException("Event not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            ticketService.addTicket(request);
        });
        
        verify(eventService, times(1)).getActiveEventById(999L);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void updateTicket_shouldUpdateTicket() throws ExecutionException, InterruptedException {
        // Arrange
        Long ticketId = 1L;
        Ticket existingTicket = new Ticket(ticketId, "Regular", 200.0, 50, TicketCategory.REGULAR, 2L);
        TicketRequest updateRequest = new TicketRequest("Regular Updated", 250.0, 40, TicketCategory.REGULAR, 2L);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(existingTicket));
        when(ticketRepository.save(existingTicket)).thenReturn(existingTicket);

        // Act
        CompletableFuture<TicketResponse> future = ticketService.updateTicket(ticketId, updateRequest);
        TicketResponse updated = future.get();

        // Assert
        assertEquals(250.0, updated.getPrice());
        assertEquals(40, updated.getQuota());
        assertEquals("Regular Updated", updated.getName());
        assertEquals(TicketCategory.REGULAR, updated.getCategory());
        assertEquals(2L, updated.getEventId());
        
        verify(ticketRepository, times(1)).findById(ticketId);
        verify(ticketRepository, times(1)).save(existingTicket);
    }

    @Test
    void updateTicket_shouldThrowExceptionWhenNotFound() {
        // Arrange
        Long ticketId = 999L;
        TicketRequest request = new TicketRequest("VIP", 500.0, 100, TicketCategory.VIP, 1L);
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TicketNotFoundException.class, () -> {
            ticketService.updateTicket(ticketId, request);
        });
        
        verify(ticketRepository, times(1)).findById(ticketId);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void getTicketById_shouldReturnTicket() {
        // Arrange
        Long ticketId = 1L;
        Ticket ticket = new Ticket(ticketId, "VIP", 500.0, 100, TicketCategory.VIP, 1L);
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // Act
        TicketResponse result = ticketService.getTicketById(ticketId);

        // Assert
        assertNotNull(result);
        assertEquals(ticketId, result.getId());
        assertEquals("VIP", result.getName());
        assertEquals(500.0, result.getPrice());
        assertEquals(TicketCategory.VIP, result.getCategory());
        
        verify(ticketRepository, times(1)).findById(ticketId);
    }

    @Test
    void getTicketById_shouldThrowExceptionWhenNotFound() {
        // Arrange
        Long ticketId = 999L;
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TicketNotFoundException.class, () -> {
            ticketService.getTicketById(ticketId);
        });
        
        verify(ticketRepository, times(1)).findById(ticketId);
    }

    @Test
    void purchaseTicket_shouldSuccessfullyPurchaseTicket() {
        // Arrange
        Long ticketId = 1L;
        Ticket ticket = new Ticket(ticketId, "Regular", 200.0, 10, TicketCategory.REGULAR, 2L);
        
        EventResponseDTO mockEvent = new EventResponseDTO();
        mockEvent.setId(2L);
        mockEvent.setEventDate(LocalDateTime.now().plusDays(1)); // Future event
        mockEvent.setActive(true);
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(eventService.getActiveEventById(2L)).thenReturn(mockEvent);
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        // Act
        TicketResponse result = ticketService.purchaseTicket(ticketId);

        // Assert
        assertNotNull(result);
        assertEquals(ticketId, result.getId());
        assertEquals("Regular", result.getName());
        
        verify(ticketRepository, times(1)).findById(ticketId);
        verify(eventService, times(1)).getActiveEventById(2L);
        verify(ticketRepository, times(1)).save(ticket);
    }

    @Test
    void purchaseTicket_shouldThrowExceptionForPastEvent() {
        // Arrange
        Long ticketId = 1L;
        Ticket ticket = new Ticket(ticketId, "Regular", 200.0, 10, TicketCategory.REGULAR, 2L);
        
        EventResponseDTO mockEvent = new EventResponseDTO();
        mockEvent.setId(2L);
        mockEvent.setEventDate(LocalDateTime.now().minusDays(1)); // Past event
        mockEvent.setActive(true);
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(eventService.getActiveEventById(2L)).thenReturn(mockEvent);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.purchaseTicket(ticketId);
        });
        
        assertEquals("Cannot purchase ticket for past event", exception.getMessage());
        
        verify(ticketRepository, times(1)).findById(ticketId);
        verify(eventService, times(1)).getActiveEventById(2L);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void purchaseTicket_shouldThrowExceptionWhenSoldOut() {
        // Arrange
        Long ticketId = 1L;
        Ticket ticket = new Ticket(ticketId, "Regular", 200.0, 0, TicketCategory.REGULAR, 2L); // quota = 0
        
        EventResponseDTO mockEvent = new EventResponseDTO();
        mockEvent.setId(2L);
        mockEvent.setEventDate(LocalDateTime.now().plusDays(1));
        mockEvent.setActive(true);
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(eventService.getActiveEventById(2L)).thenReturn(mockEvent);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            ticketService.purchaseTicket(ticketId);
        });
        
        assertEquals("Ticket is sold out", exception.getMessage());
        
        verify(ticketRepository, times(1)).findById(ticketId);
        verify(eventService, times(1)).getActiveEventById(2L);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void deleteTicket_shouldCallRepository() throws ExecutionException, InterruptedException {
        // Arrange
        Long ticketId = 1L;
        Ticket ticket = new Ticket(ticketId, "Regular", 150.0, 20, TicketCategory.REGULAR, 3L);

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // Act
        CompletableFuture<Void> future = ticketService.deleteTicket(ticketId);
        future.get(); // Wait for completion

        // Assert
        verify(ticketRepository, times(1)).findById(ticketId);
        verify(ticketRepository, times(1)).delete(ticket);
    }

    @Test
    void deleteTicket_shouldThrowExceptionWhenNotFound() {
        // Arrange
        Long ticketId = 888L;
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TicketNotFoundException.class, () -> {
            ticketService.deleteTicket(ticketId);
        });
        
        verify(ticketRepository, times(1)).findById(ticketId);
        verify(ticketRepository, never()).delete(any(Ticket.class));
    }
}