package id.ac.ui.cs.advprog.eventsphere.event.repository;

import id.ac.ui.cs.advprog.eventsphere.event.model.Event;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;

    private Event sampleEvent;
    private User organizer;

    @BeforeEach
    void setup() {
        organizer = new User();
        organizer.setEmail("test@organizer.com");
        organizer.setPassword("somepass");
        organizer.setFullName("Test User");
        userRepository.save(organizer);

        sampleEvent = new Event();
        sampleEvent.setTitle("Sample Event");
        sampleEvent.setDescription("Description");
        sampleEvent.setEventDate(LocalDateTime.now().plusDays(5));
        sampleEvent.setLocation("Location");
        sampleEvent.setPrice(BigDecimal.TEN);
        sampleEvent.setCreatedAt(LocalDateTime.now());
        sampleEvent.setUpdatedAt(LocalDateTime.now());
        sampleEvent.setOrganizer(organizer);

        eventRepository.save(sampleEvent);
    }

    @Test
    void testFindByIsActiveTrue() {
        List<Event> activeEvents = eventRepository.findByIsActiveTrue();
        assertFalse(activeEvents.isEmpty());
        assertTrue(activeEvents.stream().allMatch(Event::isActive));
    }

    @Test
    void testFindByIdAndIsActiveTrue() {
        var found = eventRepository.findByIdAndIsActiveTrue(sampleEvent.getId());
        assertTrue(found.isPresent());
        assertTrue(found.get().isActive());
    }

    @Test
    void testFindByOrganizerAndIsActiveTrue() {
        var events = eventRepository.findByOrganizerAndIsActiveTrue(organizer);
        assertEquals(1, events.size());
        assertTrue(events.get(0).isActive());
    }

    @Test
    void testDeactivateEvent() {
        sampleEvent.setActive(false);
        eventRepository.save(sampleEvent);

        var activeEvents = eventRepository.findByIsActiveTrue();
        assertTrue(activeEvents.isEmpty());
    }
}