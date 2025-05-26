package id.ac.ui.cs.advprog.eventsphere.event.model;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EventTest {

    private Event event;
    private User organizer;
    private LocalDateTime futureDate;
    private LocalDateTime pastDate;
    private LocalDateTime currentDate;

    @BeforeEach
    void setUp() {
        organizer = new User();
        organizer.setId(1L);
        
        futureDate = LocalDateTime.now().plusDays(1);
        pastDate = LocalDateTime.now().minusDays(1);
        currentDate = LocalDateTime.now();
        
        event = Event.builder()
                .title("Test Event")
                .description("Test Description")
                .eventDate(futureDate)
                .location("Test Location")
                .price(BigDecimal.valueOf(100))
                .organizer(organizer)
                .build();
    }

    @Test
    void testIsRelevant_WhenFutureDateAndActiveAndNotCancelled_ShouldReturnTrue() {
        event.setEventDate(futureDate);
        event.setActive(true);
        event.setCancelled(false);
        
        assertTrue(event.isRelevant());
    }

    @Test
    void testIsRelevant_WhenPastDate_ShouldReturnFalse() {
        event.setEventDate(pastDate);
        event.setActive(true);
        event.setCancelled(false);
        
        assertFalse(event.isRelevant());
    }

    @Test
    void testIsRelevant_WhenCurrentDate_ShouldReturnFalse() {
        event.setEventDate(currentDate);
        event.setActive(true);
        event.setCancelled(false);
        
        assertFalse(event.isRelevant());
    }

    @Test
    void testIsRelevant_WhenCancelled_ShouldReturnFalse() {
        event.setCancelled(true);
        
        assertFalse(event.isRelevant());
    }

    @Test
    void testIsRelevant_WhenNotActive_ShouldReturnFalse() {
        event.setActive(false);
        
        assertFalse(event.isRelevant());
    }

    @Test
    void testOnCreate_SetsCorrectDefaultValues() {
        Event newEvent = Event.builder()
                .title("New Event")
                .eventDate(futureDate)
                .location("Location")
                .price(BigDecimal.TEN)
                .organizer(organizer)
                .build();
        
        assertNull(newEvent.getCreatedAt());
        assertNull(newEvent.getUpdatedAt());
        
        newEvent.onCreate();
        
        assertNotNull(newEvent.getCreatedAt());
        assertNotNull(newEvent.getUpdatedAt());
        assertTrue(newEvent.isActive());
        assertEquals(newEvent.getCreatedAt(), newEvent.getUpdatedAt());
    }
}