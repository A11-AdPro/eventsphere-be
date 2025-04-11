package id.ac.ui.cs.advprog.eventsphere.repository;
 
import id.ac.ui.cs.advprog.eventsphere.model.Event;
import id.ac.ui.cs.advprog.eventsphere.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizer(User organizer);
    List<Event> findByIsActiveTrue();
    List<Event> findByEventDateAfter(LocalDateTime date);
    List<Event> findByOrganizerAndIsActiveTrue(User organizer);
}