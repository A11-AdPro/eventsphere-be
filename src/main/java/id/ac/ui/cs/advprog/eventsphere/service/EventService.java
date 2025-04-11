package id.ac.ui.cs.advprog.eventsphere.service;

import id.ac.ui.cs.advprog.eventsphere.dto.EventCreateDTO;
import id.ac.ui.cs.advprog.eventsphere.dto.EventResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.dto.EventUpdateDTO;
import id.ac.ui.cs.advprog.eventsphere.model.User;

import java.util.List;

public interface EventService {
    EventResponseDTO createEvent(EventCreateDTO eventCreateDTO, User organizer);
    List<EventResponseDTO> getAllEvents();
    EventResponseDTO getEventById(Long id);
    List<EventResponseDTO> getEventsByOrganizer(User organizer);
    EventResponseDTO updateEvent(Long id, EventUpdateDTO eventUpdateDTO, User organizer);
    void deleteEvent(Long id, User organizer);
}