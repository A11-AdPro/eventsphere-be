package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    public List<String> getOrganizerEmails(UUID eventId) {
        // In a real implementation, we would query for organizers of this specific event
        // For now, return all organizers
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ORGANIZER)
                .map(User::getEmail)
                .collect(Collectors.toList());
    }

    public List<Long> getAdminIds() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .map(User::getId)
                .collect(Collectors.toList());
    }

    public List<Long> getOrganizerIds(UUID eventId) {
        // In a real implementation, we would query for organizers of this specific event
        // For now, return all organizers
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ORGANIZER)
                .map(User::getId)
                .collect(Collectors.toList());
    }
}