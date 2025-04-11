package id.ac.ui.cs.advprog.eventsphere.service;

import id.ac.ui.cs.advprog.eventsphere.dto.EventCreateDTO;
import id.ac.ui.cs.advprog.eventsphere.dto.EventResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.dto.EventUpdateDTO;
import id.ac.ui.cs.advprog.eventsphere.exception.EventNotFoundException;
import id.ac.ui.cs.advprog.eventsphere.exception.UnauthorizedAccessException;
import id.ac.ui.cs.advprog.eventsphere.model.User;
import id.ac.ui.cs.advprog.eventsphere.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EventServiceTest {

    @Autowired
    private EventService eventService;

    private User organizer;
    private User anotherUser;
    private EventCreateDTO eventCreateDTO;
    private EventUpdateDTO eventUpdateDTO;

    @BeforeEach
    void setUp() {
        organizer = createTestOrganizer();
        anotherUser = createTestUser();

        // Set up event create DTO
        eventCreateDTO = new EventCreateDTO();
        eventCreateDTO.setTitle("Test Integration Event");
        eventCreateDTO.setDescription("Testing the full EventService contract");
        eventCreateDTO.setEventDate(LocalDateTime.now().plusDays(10));
        eventCreateDTO.setLocation("Test Integration Location");
        eventCreateDTO.setPrice(new BigDecimal("125.00"));

        // Set up event update DTO
        eventUpdateDTO = new EventUpdateDTO();
        eventUpdateDTO.setTitle("Updated Integration Event");
        eventUpdateDTO.setDescription("Updated description for integration test");
        eventUpdateDTO.setEventDate(LocalDateTime.now().plusDays(15));
        eventUpdateDTO.setLocation("Updated Integration Location");
        eventUpdateDTO.setPrice(new BigDecimal("150.00"));
    }

    @Test
    void testCreateAndGetEventById() {
        // Create a new event
        EventResponseDTO createdEvent = eventService.createEvent(eventCreateDTO, organizer);
        
        // Verify event was created correctly
        assertNotNull(createdEvent);
        assertNotNull(createdEvent.getId());
        assertEquals(eventCreateDTO.getTitle(), createdEvent.getTitle());
        assertEquals(eventCreateDTO.getDescription(), createdEvent.getDescription());
        
        // Verify we can get the event by ID
        EventResponseDTO retrievedEvent = eventService.getEventById(createdEvent.getId());
        assertNotNull(retrievedEvent);
        assertEquals(createdEvent.getId(), retrievedEvent.getId());
        assertEquals(createdEvent.getTitle(), retrievedEvent.getTitle());
    }

    @Test
    void testGetAllEvents() {
        // Create a couple of events
        EventResponseDTO event1 = eventService.createEvent(eventCreateDTO, organizer);
        
        EventCreateDTO anotherEventDTO = new EventCreateDTO();
        anotherEventDTO.setTitle("Second Test Event");
        anotherEventDTO.setDescription("Another event description");
        anotherEventDTO.setEventDate(LocalDateTime.now().plusDays(20));
        anotherEventDTO.setLocation("Another Location");
        anotherEventDTO.setPrice(new BigDecimal("200.00"));
        
        EventResponseDTO event2 = eventService.createEvent(anotherEventDTO, organizer);
        
        // Get all events
        List<EventResponseDTO> allEvents = eventService.getAllEvents();
        
        // Verify we have at least our two events
        assertNotNull(allEvents);
        assertTrue(allEvents.size() >= 2);
        
        // Check that our created events are in the list
        boolean containsEvent1 = false;
        boolean containsEvent2 = false;
        
        for (EventResponseDTO event : allEvents) {
            if (event.getId().equals(event1.getId())) {
                containsEvent1 = true;
            }
            if (event.getId().equals(event2.getId())) {
                containsEvent2 = true;
            }
        }
        
        assertTrue(containsEvent1, "All events should contain event 1");
        assertTrue(containsEvent2, "All events should contain event 2");
    }

    @Test
    void testGetEventsByOrganizer() {
        // Create an event with our test organizer
        EventResponseDTO createdEvent = eventService.createEvent(eventCreateDTO, organizer);
        
        // Get events by this organizer
        List<EventResponseDTO> organizerEvents = eventService.getEventsByOrganizer(organizer);
        
        // Verify we have at least our created event
        assertNotNull(organizerEvents);
        assertFalse(organizerEvents.isEmpty());
        
        // Check our created event is in the list
        boolean containsCreatedEvent = false;
        for (EventResponseDTO event : organizerEvents) {
            if (event.getId().equals(createdEvent.getId())) {
                containsCreatedEvent = true;
                break;
            }
        }
        
        assertTrue(containsCreatedEvent, "Organizer's events should contain the created event");
    }

    @Test
    void testUpdateEvent() {
        // Create an event
        EventResponseDTO createdEvent = eventService.createEvent(eventCreateDTO, organizer);
        
        // Update the event
        EventResponseDTO updatedEvent = eventService.updateEvent(createdEvent.getId(), eventUpdateDTO, organizer);
        
        // Verify the event was updated
        assertNotNull(updatedEvent);
        assertEquals(createdEvent.getId(), updatedEvent.getId());
        assertEquals(eventUpdateDTO.getTitle(), updatedEvent.getTitle());
        assertEquals(eventUpdateDTO.getDescription(), updatedEvent.getDescription());
        assertEquals(eventUpdateDTO.getLocation(), updatedEvent.getLocation());
        assertEquals(eventUpdateDTO.getPrice(), updatedEvent.getPrice());
    }

    @Test
    void testUpdateEvent_UnauthorizedUser() {
        // Create an event with our main organizer
        EventResponseDTO createdEvent = eventService.createEvent(eventCreateDTO, organizer);
        
        // Try to update with another user
        assertThrows(UnauthorizedAccessException.class, () -> {
            eventService.updateEvent(createdEvent.getId(), eventUpdateDTO, anotherUser);
        });
    }

    @Test
    void testDeleteEvent() {
        // Create an event
        EventResponseDTO createdEvent = eventService.createEvent(eventCreateDTO, organizer);
        
        // Delete the event
        eventService.deleteEvent(createdEvent.getId(), organizer);
        
        // Try to get the event - it should throw an exception or return a non-active event
        try {
            // EventResponseDTO deletedEvent = eventService.getEventById(createdEvent.getId());
            // If we get here, the event should no longer be active
            fail("Event should not be retrievable after deletion or should be marked as inactive");
        } catch (EventNotFoundException e) {
            // This is an acceptable outcome - event not found after deletion
            assertTrue(true);
        }
    }

    @Test
    void testDeleteEvent_UnauthorizedUser() {
        // Create an event with our main organizer
        EventResponseDTO createdEvent = eventService.createEvent(eventCreateDTO, organizer);
        
        // Try to delete with another user
        assertThrows(UnauthorizedAccessException.class, () -> {
            eventService.deleteEvent(createdEvent.getId(), anotherUser);
        });
    }

    @Test
    void testGetEventById_NonExistentEvent() {
        // Try to get an event with a non-existent ID
        assertThrows(EventNotFoundException.class, () -> {
            eventService.getEventById(999999L); // Assuming this ID doesn't exist
        });
    }
    
    // Helper methods to create test users - in a real test these would interact with your user repository
    private User createTestOrganizer() {
        return User.builder()
                .id(1L)
                .username("test-organizer")
                .password("password123")
                .name("Test Organizer")
                .email("organizer@test.com")
                .role(UserRole.ROLE_ORGANIZER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
    
    private User createTestUser() {
        return User.builder()
                .id(2L)
                .username("test-user")
                .password("password123")
                .name("Test User")
                .email("user@test.com")
                .role(UserRole.ROLE_USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}