package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.event.service.EventService;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EventService eventService;

    @Autowired
    public UserService(UserRepository userRepository, EventService eventService) {
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    public String getUserEmail(Long userId) {
        return userRepository.findById(userId)
                .map(User::getEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
    }

    public List<String> getAdminEmails() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .map(User::getEmail)
                .collect(Collectors.toList());
    }

    public List<String> getOrganizerEmails(Long eventId) {
        try {
            // Get the specific event to find its organizer
            EventResponseDTO event = eventService.getActiveEventById(eventId);
            if (event != null && event.getOrganizerId() != null) {
                return userRepository.findById(event.getOrganizerId())
                        .map(user -> Collections.singletonList(user.getEmail()))
                        .orElse(Collections.emptyList());
            }
        } catch (Exception e) {
            System.out.println("Could not find event organizer for event ID: " + eventId);
        }
        return Collections.emptyList();
    }

    public List<Long> getAdminIds() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .map(User::getId)
                .collect(Collectors.toList());
    }

    public List<Long> getOrganizerIds(Long eventId) {
        try {
            EventResponseDTO event = eventService.getActiveEventById(eventId);
            if (event != null && event.getOrganizerId() != null) {
                return Collections.singletonList(event.getOrganizerId());
            }
        } catch (Exception e) {
            System.out.println("Could not find event organizer for event ID: " + eventId);
        }
        return Collections.emptyList();
    }
}