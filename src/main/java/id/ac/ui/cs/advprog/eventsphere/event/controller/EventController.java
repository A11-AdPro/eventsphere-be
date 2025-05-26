package id.ac.ui.cs.advprog.eventsphere.event.controller;

import id.ac.ui.cs.advprog.eventsphere.event.dto.EventCreateDTO;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventUpdateDTO;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.event.service.EventService;
import id.ac.ui.cs.advprog.eventsphere.event.exception.UnauthorizedAccessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    
    private final EventService eventService;
    private final UserRepository userRepository;
    private final String USER_NOT_FOUND_MESSAGE = "User not found";
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ORGANIZER')")
    public ResponseEntity<EventResponseDTO> createEvent(
            @Valid @RequestBody EventCreateDTO eventCreateDTO) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = ((UserDetails) auth.getPrincipal()).getUsername();
        User organizer = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedAccessException(USER_NOT_FOUND_MESSAGE));

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(eventService.createEvent(eventCreateDTO, organizer));
    }
    
    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllActiveEvents());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getActiveEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getActiveEventById(id));
    }
    
    @GetMapping("/my-events")
    @PreAuthorize("hasRole('ROLE_ORGANIZER')")
    public ResponseEntity<List<EventResponseDTO>> getOrganizerEvents() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = ((UserDetails) auth.getPrincipal()).getUsername();
        User organizer = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedAccessException(USER_NOT_FOUND_MESSAGE));

        return ResponseEntity.ok(eventService.getActiveEventsByOrganizer(organizer));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ORGANIZER')")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventUpdateDTO eventUpdateDTO) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = ((UserDetails) auth.getPrincipal()).getUsername();
        User organizer = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedAccessException(USER_NOT_FOUND_MESSAGE));
            
        return ResponseEntity.ok(eventService.updateEvent(id, eventUpdateDTO, organizer));
    }

    @DeleteMapping("/cancel/{id}")
    @PreAuthorize("hasRole('ROLE_ORGANIZER')")
    public ResponseEntity<Map<String, String>> cancelEvent(
            @PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = ((UserDetails) auth.getPrincipal()).getUsername();

        User organizer = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedAccessException(USER_NOT_FOUND_MESSAGE));
        
        String message = eventService.cancelEvent(id, organizer);
        return ResponseEntity.ok(
            Map.of(
                "status", "success",
                "message", message,
                "timestamp", LocalDateTime.now().toString()
            )
        );
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ORGANIZER')")
    public ResponseEntity<Map<String, String>> deleteEvent(
            @PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = ((UserDetails) auth.getPrincipal()).getUsername();

        User organizer = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedAccessException(USER_NOT_FOUND_MESSAGE));
            
        String message = eventService.deleteEvent(id, organizer);
        return ResponseEntity.ok(
            Map.of(
                "status", "success",
                "message", message,
                "timestamp", LocalDateTime.now().toString()
            )
        );
    }
}