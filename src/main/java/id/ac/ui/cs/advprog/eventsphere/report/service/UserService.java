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
}