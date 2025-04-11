package id.ac.ui.cs.advprog.eventsphere.controller;

import id.ac.ui.cs.advprog.eventsphere.dto.EventCreateDTO;
import id.ac.ui.cs.advprog.eventsphere.dto.EventResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.dto.EventUpdateDTO;
import id.ac.ui.cs.advprog.eventsphere.model.User;
import id.ac.ui.cs.advprog.eventsphere.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    
    private final EventService eventService;
    private final UserRepository userRepository;
    
    // For demo purposes - in production, use proper authentication
    private User getTestOrganizer() {
        return userRepository.findById(1L)
            .orElseThrow(() -> new RuntimeException("Test organizer not found"));
    }
    
    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody EventCreateDTO eventCreateDTO) {
        // For testing purposes - in production, get the current user from security context
        User organizer = getTestOrganizer();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(eventCreateDTO, organizer));
    }
    
    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }
    
    @GetMapping("/organizer")
    public ResponseEntity<List<EventResponseDTO>> getOrganizerEvents() {
        // For testing purposes
        User organizer = getTestOrganizer();
        return ResponseEntity.ok(eventService.getEventsByOrganizer(organizer));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateDTO eventUpdateDTO) {
        // For testing purposes
        User organizer = getTestOrganizer();
        return ResponseEntity.ok(eventService.updateEvent(id, eventUpdateDTO, organizer));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        // For testing purposes
        User organizer = getTestOrganizer();
        eventService.deleteEvent(id, organizer);
        return ResponseEntity.noContent().build();
    }
}