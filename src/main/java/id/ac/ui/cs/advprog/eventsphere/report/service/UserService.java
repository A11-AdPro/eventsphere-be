package id.ac.ui.cs.advprog.eventsphere.report.service;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    public String getUserEmail(UUID userId) {
        return "user@example.com";
    }

    public List<String> getAdminEmails() {
        return List.of("admin1@example.com", "admin2@example.com");
    }

    public List<String> getOrganizerEmails(UUID eventId) {
        return List.of("organizer@example.com");
    }

    public List<UUID> getAdminIds() {
        return List.of(
                UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"),
                UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12")
        );
    }

    public List<UUID> getOrganizerIds(UUID eventId) {
        return List.of(
                UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13")
        );
    }
}