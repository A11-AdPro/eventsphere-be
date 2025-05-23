package id.ac.ui.cs.advprog.eventsphere.event.model;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
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
                .password("password123")
                .fullName("Event Organizer")
                .email("organizer@example.com")
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
        assertEquals(organizer, event.getOrganizer());
    }

    @Test
    void testNoArgsConstructor() {
        Event emptyEvent = new Event();
        assertNotNull(emptyEvent);
        assertNull(emptyEvent.getId());
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
        assertTrue(event.getUpdatedAt().isAfter(initialUpdateTime));
    }

    @Test
    void testSetters() {
        Event newEvent = new Event();
        LocalDateTime newEventDate = LocalDateTime.now().plusDays(14);
        User newOrganizer = User.builder().id(2L).build();
        newEvent.setOrganizer(newOrganizer);
        assertEquals(newOrganizer, newEvent.getOrganizer());
    }

    @Test
    void testIsRelevant() {
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setCancelled(false);
        event.setActive(true);
        assertTrue(event.isRelevant());

        event.setCancelled(true);
        assertFalse(event.isRelevant());
    }
}