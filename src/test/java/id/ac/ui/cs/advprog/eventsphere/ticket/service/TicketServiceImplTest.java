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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {

    private EventRepository eventRepository;
    private TicketRepository ticketRepository;
    private TicketServiceImpl ticketService;
    private User organizer;
    private User admin;
    private User regularUser;
    private Event dummyEvent;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        eventRepository = mock(EventRepository.class);
        ticketService = new TicketServiceImpl(ticketRepository, eventRepository);

        organizer = new User();
        organizer.setRole(Role.ORGANIZER);

        admin = new User();
        admin.setRole(Role.ADMIN);

        regularUser = new User();
        regularUser.setRole(Role.ATTENDEE);

        dummyEvent = new Event();
        dummyEvent.setId(1L);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(dummyEvent));
        
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
    void addTicket_shouldThrowExceptionWhenUserIsNotOrganizer() {
        TicketRequest request = new TicketRequest("VIP", 500.0, 100, TicketCategory.VIP, 1L);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ticketService.addTicket(request, regularUser));
        
        assertEquals("Hanya organizer yang dapat menambahkan tiket.", exception.getMessage());
        
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void addTicket_shouldThrowExceptionWhenUserIsAdmin() {
        TicketRequest request = new TicketRequest("VIP", 500.0, 100, TicketCategory.VIP, 1L);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ticketService.addTicket(request, admin));
        
        assertEquals("Hanya organizer yang dapat menambahkan tiket.", exception.getMessage());
        
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void getAvailableTickets_shouldReturnAllTickets() {
        // Arrange
        Ticket ticket1 = new Ticket(1L, "VIP", 500.0, 100, TicketCategory.VIP, dummyEvent);
        Ticket ticket2 = new Ticket(2L, "Regular", 250.0, 200, TicketCategory.REGULAR, dummyEvent);
        List<Ticket> tickets = Arrays.asList(ticket1, ticket2);

        when(ticketRepository.findAll()).thenReturn(tickets);

        List<TicketResponse> result = ticketService.getAvailableTickets();

        assertNotNull(result);
        assertEquals(2, result.size());
        
        TicketResponse response1 = result.get(0);
        assertEquals("VIP", response1.getName());
        assertEquals(500.0, response1.getPrice());
        assertEquals(TicketCategory.VIP, response1.getCategory());
        
        TicketResponse response2 = result.get(1);
        assertEquals("Regular", response2.getName());
        assertEquals(250.0, response2.getPrice());
        assertEquals(TicketCategory.REGULAR, response2.getCategory());
        
        verify(ticketRepository, times(1)).findAll();
    }

    @Test
    void getAvailableTickets_shouldReturnEmptyListWhenNoTickets() {
        // Arrange
        when(ticketRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<TicketResponse> result = ticketService.getAvailableTickets();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(ticketRepository, times(1)).findAll();
    }

    @Test
    void purchaseTicket_shouldSuccessfullyPurchaseTicket() {
        // Arrange
        Long ticketId = 1L;
        Ticket ticket = new Ticket(ticketId, "VIP", 500.0, 100, TicketCategory.VIP, dummyEvent);
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        // Act
        TicketResponse result = ticketService.purchaseTicket(ticketId);

        // Assert
        assertNotNull(result);
        assertEquals("VIP", result.getName());
        assertEquals(500.0, result.getPrice());
        assertEquals(TicketCategory.VIP, result.getCategory());
        
        verify(ticketRepository, times(1)).findById(ticketId);
        verify(ticketRepository, times(1)).save(ticket);
        // Verify that purchase() method was called (quota should be decreased)
        assertEquals(99, ticket.getQuota()); // Assuming purchase() decreases quota by 1
    }

    @Test
    void purchaseTicket_shouldThrowExceptionWhenTicketNotFound() {
        // Arrange
        Long ticketId = 999L;
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ticketService.purchaseTicket(ticketId));
        
        assertEquals("Ticket tidak ditemukan", exception.getMessage());
        verify(ticketRepository, times(1)).findById(ticketId);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void purchaseTicket_shouldThrowExceptionWhenTicketSoldOut() {
        // Arrange
        Long ticketId = 1L;
        Ticket soldOutTicket = new Ticket(ticketId, "VIP", 500.0, 0, TicketCategory.VIP, dummyEvent);
        // Assuming quota 0 means sold out, or you might need to set it differently based on your isSoldOut() implementation
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(soldOutTicket));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> ticketService.purchaseTicket(ticketId));
        
        assertEquals("Tiket sudah habis terjual.", exception.getMessage());
        verify(ticketRepository, times(1)).findById(ticketId);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void getTicketById_shouldReturnTicket() {
        // Arrange
        Long ticketId = 1L;
        Ticket ticket = new Ticket(ticketId, "VIP", 500.0, 100, TicketCategory.VIP, dummyEvent);
        
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        // Act
        TicketResponse result = ticketService.getTicketById(ticketId);

        // Assert
        assertNotNull(result);
        assertEquals("VIP", result.getName());
        assertEquals(500.0, result.getPrice());
        assertEquals(100, result.getQuota());
        assertEquals(TicketCategory.VIP, result.getCategory());
        
        verify(ticketRepository, times(1)).findById(ticketId);
    }

    @Test
    void getTicketById_shouldThrowExceptionWhenTicketNotFound() {
        // Arrange
        Long ticketId = 999L;
        when(ticketRepository.findById(ticketId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TicketNotFoundException.class, 
            () -> ticketService.getTicketById(ticketId));
        
        verify(ticketRepository, times(1)).findById(ticketId);
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