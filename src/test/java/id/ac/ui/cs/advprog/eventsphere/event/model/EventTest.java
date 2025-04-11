package id.ac.ui.cs.advprog.eventsphere.event.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {
    
    private Event event;
    private User organizer;
    private LocalDateTime eventDate;
    
    @BeforeEach
    void setUp() {
        eventDate = LocalDateTime.now().plusDays(7);
        organizer = User.builder()
                .id(1L)
                .username("organizerUser")
                .password("password123")
                .name("Event Organizer")
                .email("organizer@example.com")
                .role(UserRole.ROLE_ORGANIZER)
                .build();
        
        event = Event.builder()
                .id(1L)
                .title("Test Event")
                .description("This is a test event")
                .eventDate(eventDate)
                .location("Test Location")
                .price(new BigDecimal("100.00"))
                .organizer(organizer)
                .build();
    }
    
    @Test
    void testEventConstructor() {
        assertNotNull(event);
        assertEquals(1L, event.getId());
        assertEquals("Test Event", event.getTitle());
        assertEquals("This is a test event", event.getDescription());
        assertEquals(eventDate, event.getEventDate());
        assertEquals("Test Location", event.getLocation());
        assertEquals(new BigDecimal("100.00"), event.getPrice());
        assertEquals(organizer, event.getOrganizer());
    }
    
    @Test
    void testNoArgsConstructor() {
        Event emptyEvent = new Event();
        assertNotNull(emptyEvent);
        assertNull(emptyEvent.getId());
        assertNull(emptyEvent.getTitle());
        assertNull(emptyEvent.getDescription());
        assertNull(emptyEvent.getEventDate());
        assertNull(emptyEvent.getLocation());
        assertNull(emptyEvent.getPrice());
        assertNull(emptyEvent.getOrganizer());
    }
    
    @Test
    void testPrePersist() {
        Event newEvent = new Event();
        newEvent.onCreate();
        
        assertNotNull(newEvent.getCreatedAt());
        assertNotNull(newEvent.getUpdatedAt());
        assertTrue(newEvent.isActive());
    }
    
    @Test
    void testPreUpdate() {
        LocalDateTime initialUpdateTime = LocalDateTime.now().minusDays(1);
        event.setUpdatedAt(initialUpdateTime);
        
        event.onUpdate();
        
        assertNotEquals(initialUpdateTime, event.getUpdatedAt());
        assertTrue(event.getUpdatedAt().isAfter(initialUpdateTime));
    }
    
    @Test
    void testSetters() {
        Event newEvent = new Event();
        LocalDateTime newEventDate = LocalDateTime.now().plusDays(14);
        User newOrganizer = User.builder().id(2L).build();
        
        newEvent.setId(2L);
        newEvent.setTitle("Updated Title");
        newEvent.setDescription("Updated Description");
        newEvent.setEventDate(newEventDate);
        newEvent.setLocation("Updated Location");
        newEvent.setPrice(new BigDecimal("200.00"));
        newEvent.setOrganizer(newOrganizer);
        newEvent.setActive(false);
        
        assertEquals(2L, newEvent.getId());
        assertEquals("Updated Title", newEvent.getTitle());
        assertEquals("Updated Description", newEvent.getDescription());
        assertEquals(newEventDate, newEvent.getEventDate());
        assertEquals("Updated Location", newEvent.getLocation());
        assertEquals(new BigDecimal("200.00"), newEvent.getPrice());
        assertEquals(newOrganizer, newEvent.getOrganizer());
        assertFalse(newEvent.isActive());
    }
}