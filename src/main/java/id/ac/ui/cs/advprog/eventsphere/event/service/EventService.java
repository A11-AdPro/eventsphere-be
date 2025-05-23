package id.ac.ui.cs.advprog.eventsphere.event.service;

import id.ac.ui.cs.advprog.eventsphere.event.dto.EventCreateDTO;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventUpdateDTO;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;

import java.util.List;

public interface EventService {
    EventResponseDTO createEvent(EventCreateDTO eventCreateDTO, User organizer);
    List<EventResponseDTO> getAllActiveEvents();
    EventResponseDTO getActiveEventById(Long id);
    List<EventResponseDTO> getActiveEventsByOrganizer(User organizer);
    EventResponseDTO updateEvent(Long id, EventUpdateDTO eventUpdateDTO, User organizer);
    String cancelEvent(Long id, User organizer);
    String deleteEvent(Long id, User organizer);

}