package id.ac.ui.cs.advprog.eventsphere.event.repository;
 
import id.ac.ui.cs.advprog.eventsphere.event.model.Event;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizer(User organizer);
    List<Event> findByIsActiveTrue();
    List<Event> findByEventDateAfter(LocalDateTime date);
    List<Event> findByOrganizerAndIsActiveTrue(User organizer);
    Optional<Event> findByIdAndIsActiveTrue(Long id);
    List<Event> findByOrganizerAndEventDateAfter(User organizer, LocalDateTime date);
}