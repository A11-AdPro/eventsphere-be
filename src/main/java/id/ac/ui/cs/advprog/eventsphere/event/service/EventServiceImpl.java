package id.ac.ui.cs.advprog.eventsphere.event.service;

import id.ac.ui.cs.advprog.eventsphere.event.dto.EventCreateDTO;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventUpdateDTO;
import id.ac.ui.cs.advprog.eventsphere.event.exception.EventNotFoundException;
import id.ac.ui.cs.advprog.eventsphere.event.exception.UnauthorizedAccessException;
import id.ac.ui.cs.advprog.eventsphere.event.model.Event;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.event.repository.EventRepository;
import id.ac.ui.cs.advprog.eventsphere.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    
    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    
    @Override
    @Transactional
    public EventResponseDTO createEvent(EventCreateDTO eventCreateDTO, User organizer) {
        
        Event event = modelMapper.map(eventCreateDTO, Event.class);
        event.setOrganizer(organizer);
        Event savedEvent = eventRepository.save(event);

        return toResponseDTO(savedEvent);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getAllActiveEvents() {
        return eventRepository.findByIsActiveTrue()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public EventResponseDTO getActiveEventById(Long id) {
        return eventRepository.findByIdAndIsActiveTrue(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new EventNotFoundException("Active event not found with id: " + id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getActiveEventsByOrganizer(User organizer) {
        return eventRepository.findByOrganizerAndIsActiveTrue(organizer)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }
    
    @Override
    @Transactional
    public EventResponseDTO updateEvent(Long id, EventUpdateDTO eventUpdateDTO, User organizer) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        
        validateEventOwnership(event, organizer);
        validateEventNotTooClose(event.getEventDate(), 24);
        
        modelMapper.map(eventUpdateDTO, event);
        Event updatedEvent = eventRepository.save(event);
        
        return toResponseDTO(updatedEvent);
    }

    @Override
    @Transactional
    public String cancelEvent(Long id, User organizer) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        
        validateEventOwnership(event, organizer);
        
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cannot cancel past events");
        }
        
        event.setCancelled(true);
        event.setCancellationTime(LocalDateTime.now());
        eventRepository.save(event);
        return "Event with ID " + id + " has been canceled successfully";
    }

    @Override
    @Transactional
    public String deleteEvent(Long id, User organizer) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        
        validateEventOwnership(event, organizer);

        if (!(event.isCancelled() || event.getEventDate().isBefore(LocalDateTime.now()))) {
            throw new IllegalStateException(
                "Only cancelled or past events can be deleted");
        }
        
        event.setActive(false);
        eventRepository.save(event);
        return "Event with ID " + id + " has been deleted successfully";
    }

    @Async
    @Transactional
    public CompletableFuture<EventResponseDTO> createEventAsync(EventCreateDTO eventCreateDTO, User organizer) {
        try {
            EventResponseDTO result = createEvent(eventCreateDTO, organizer);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<EventResponseDTO>> getAllActiveEventsAsync() {
        try {
            List<EventResponseDTO> result = getAllActiveEvents();
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<EventResponseDTO> getActiveEventByIdAsync(Long id) {
        try {
            EventResponseDTO result = getActiveEventById(id);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<EventResponseDTO>> getActiveEventsByOrganizerAsync(User organizer) {
        try {
            List<EventResponseDTO> result = getActiveEventsByOrganizer(organizer);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Async
    @Transactional
    public CompletableFuture<EventResponseDTO> updateEventAsync(Long id, EventUpdateDTO eventUpdateDTO, User organizer) {
        try {
            EventResponseDTO result = updateEvent(id, eventUpdateDTO, organizer);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    @Transactional
    public CompletableFuture<String> cancelEventAsync(Long id, User organizer) {
        try {
            String result = cancelEvent(id, organizer);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Async
    @Transactional
    public CompletableFuture<String> deleteEventAsync(Long id, User organizer) {
        try {
            String result = deleteEvent(id, organizer);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
    
    // Helper methods
    private EventResponseDTO toResponseDTO(Event event) {
        EventResponseDTO dto = modelMapper.map(event, EventResponseDTO.class);
        dto.setOrganizerId(event.getOrganizer().getId());
        dto.setOrganizerName(event.getOrganizer().getFullName());
        return dto;
    }
    
    private void validateEventOwnership(Event event, User organizer) {
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new UnauthorizedAccessException("User is not the organizer of this event");
        }
    }
    
    private void validateEventNotTooClose(LocalDateTime eventDate, int hoursBefore) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(hoursBefore))) {
            throw new IllegalStateException(
                String.format("Event cannot be modified within %d hours of its start time", hoursBefore)
            );
        }
    }
}