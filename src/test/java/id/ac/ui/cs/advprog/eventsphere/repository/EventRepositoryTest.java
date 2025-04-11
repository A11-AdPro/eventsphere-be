package id.ac.ui.cs.advprog.eventsphere.repository;

import id.ac.ui.cs.advprog.eventsphere.model.Event;
import id.ac.ui.cs.advprog.eventsphere.model.User;
import id.ac.ui.cs.advprog.eventsphere.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventRepository eventRepository;

    private User organizer;
    private User anotherOrganizer;
    private Event activeEvent;
    private Event inactiveEvent;
    private Event futureEvent;
    private Event pastEvent;

    @BeforeEach
    void setUp() {
        // Create test users
        organizer = User.builder()
                .username("organizer1")
                .password("password")
                .name("First Organizer")
                .email("organizer1@example.com")
                .role(UserRole.ROLE_ORGANIZER)
                .build();
        entityManager.persist(organizer);

        anotherOrganizer = User.builder()
                .username("organizer2")
                .password("password")
                .name("Second Organizer")
                .email("organizer2@example.com")
                .role(UserRole.ROLE_ORGANIZER)
                .build();
        entityManager.persist(anotherOrganizer);

        // Create test events
        LocalDateTime now = LocalDateTime.now();

        activeEvent = Event.builder()
                .title("Active Event")
                .description("This is an active event")
                .eventDate(now.plusDays(10))
                .location("Active Location")
                .price(new BigDecimal("100.00"))
                .organizer(organizer)
                .isActive(true)
                .build();
        entityManager.persist(activeEvent);

        inactiveEvent = Event.builder()
                .title("Inactive Event")
                .description("This is an inactive event")
                .eventDate(now.plusDays(15))
                .location("Inactive Location")
                .price(new BigDecimal("150.00"))
                .organizer(organizer)
                .isActive(false)
                .build();
        entityManager.persist(inactiveEvent);

        futureEvent = Event.builder()
                .title("Future Event")
                .description("This is a future event")
                .eventDate(now.plusDays(30))
                .location("Future Location")
                .price(new BigDecimal("200.00"))
                .organizer(anotherOrganizer)
                .isActive(true)
                .build();
        entityManager.persist(futureEvent);

        pastEvent = Event.builder()
                .title("Past Event")
                .description("This is a past event")
                .eventDate(now.minusDays(5))
                .location("Past Location")
                .price(new BigDecimal("50.00"))
                .organizer(anotherOrganizer)
                .isActive(true)
                .build();
        entityManager.persist(pastEvent);

        entityManager.flush();
    }

    @Test
    void findByOrganizer_ShouldReturnOrganizerEvents() {
        // Act
        List<Event> organizerEvents = eventRepository.findByOrganizer(organizer);

        // Assert
        assertEquals(2, organizerEvents.size());
        assertTrue(organizerEvents.contains(activeEvent));
        assertTrue(organizerEvents.contains(inactiveEvent));
    }

    // @Test
    // void findByIsActiveTrue_ShouldReturnOnlyActiveEvents() {
    //     // Act
    //     List<Event> activeEvents = eventRepository.findByIsActiveTrue();

    //     // Assert
    //     assertEquals(3, activeEvents.size());
    //     assertTrue(activeEvents.contains(activeEvent));
    //     assertTrue(activeEvents.contains(futureEvent));
    //     assertTrue(activeEvents.contains(pastEvent));
    //     assertFalse(activeEvents.contains(inactiveEvent));
    // }

    @Test
    void findByEventDateAfter_ShouldReturnFutureEvents() {
        // Act
        List<Event> futureEvents = eventRepository.findByEventDateAfter(LocalDateTime.now());

        // Assert
        assertTrue(futureEvents.size() >= 3);
        assertTrue(futureEvents.contains(activeEvent));
        assertTrue(futureEvents.contains(inactiveEvent));
        assertTrue(futureEvents.contains(futureEvent));
        assertFalse(futureEvents.contains(pastEvent));
    }

    // @Test
    // void findByOrganizerAndIsActiveTrue_ShouldReturnActiveOrganizerEvents() {
    //     // Act
    //     List<Event> activeOrganizerEvents = eventRepository.findByOrganizerAndIsActiveTrue(organizer);

    //     // Assert
    //     assertEquals(1, activeOrganizerEvents.size());
    //     assertTrue(activeOrganizerEvents.contains(activeEvent));
    //     assertFalse(activeOrganizerEvents.contains(inactiveEvent));
    // }
}