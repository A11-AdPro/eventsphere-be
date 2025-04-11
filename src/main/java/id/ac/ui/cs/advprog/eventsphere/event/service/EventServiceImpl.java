package id.ac.ui.cs.advprog.eventsphere.event.service;

import id.ac.ui.cs.advprog.eventsphere.event.dto.EventCreateDTO;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventUpdateDTO;
import id.ac.ui.cs.advprog.eventsphere.event.dto.UserSummaryDTO;
import id.ac.ui.cs.advprog.eventsphere.event.exception.EventNotFoundException;
import id.ac.ui.cs.advprog.eventsphere.event.exception.UnauthorizedAccessException;
import id.ac.ui.cs.advprog.eventsphere.event.model.Event;
import id.ac.ui.cs.advprog.eventsphere.event.model.User;
import id.ac.ui.cs.advprog.eventsphere.event.repository.EventRepository;
// import id.ac.ui.cs.advprog.eventsphere.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    
    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    
    @Override
    @Transactional
    public EventResponseDTO createEvent(EventCreateDTO eventCreateDTO, User organizer) {
        Event event = Event.builder()
                .title(eventCreateDTO.getTitle())
                .description(eventCreateDTO.getDescription())
                .eventDate(eventCreateDTO.getEventDate())
                .location(eventCreateDTO.getLocation())
                .price(eventCreateDTO.getPrice())
                .organizer(organizer)
                .build();
        
        Event savedEvent = eventRepository.save(event);
        return mapToEventResponseDTO(savedEvent);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getAllEvents() {
        return eventRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToEventResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        return mapToEventResponseDTO(event);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDTO> getEventsByOrganizer(User organizer) {
        return eventRepository.findByOrganizerAndIsActiveTrue(organizer)
                .stream()
                .map(this::mapToEventResponseDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public EventResponseDTO updateEvent(Long id, EventUpdateDTO eventUpdateDTO, User organizer) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to update this event");
        }
        
        // Check if event date is not too close to update
        LocalDateTime cutoffDate = LocalDateTime.now().plusDays(2);
        if (event.getEventDate().isBefore(cutoffDate)) {
            throw new IllegalStateException("Event cannot be updated within 48 hours of its scheduled time");
        }
        
        if (eventUpdateDTO.getTitle() != null) {
            event.setTitle(eventUpdateDTO.getTitle());
        }
        if (eventUpdateDTO.getDescription() != null) {
            event.setDescription(eventUpdateDTO.getDescription());
        }
        if (eventUpdateDTO.getEventDate() != null) {
            event.setEventDate(eventUpdateDTO.getEventDate());
        }
        if (eventUpdateDTO.getLocation() != null) {
            event.setLocation(eventUpdateDTO.getLocation());
        }
        if (eventUpdateDTO.getPrice() != null) {
            event.setPrice(eventUpdateDTO.getPrice());
        }
        
        Event updatedEvent = eventRepository.save(event);
        return mapToEventResponseDTO(updatedEvent);
    }
    
    @Override
    @Transactional
    public void deleteEvent(Long id, User organizer) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
        
        if (!event.getOrganizer().getId().equals(organizer.getId())) {
            throw new UnauthorizedAccessException("You are not authorized to delete this event");
        }
        
        // Soft delete by setting isActive to false
        event.setActive(false);
        eventRepository.save(event);
    }
    
    private EventResponseDTO mapToEventResponseDTO(Event event) {
        EventResponseDTO dto = modelMapper.map(event, EventResponseDTO.class);
        
        UserSummaryDTO organizerDTO = new UserSummaryDTO();
        organizerDTO.setId(event.getOrganizer().getId());
        organizerDTO.setUsername(event.getOrganizer().getUsername());
        organizerDTO.setName(event.getOrganizer().getName());
        
        dto.setOrganizer(organizerDTO);
        return dto;
    }
}