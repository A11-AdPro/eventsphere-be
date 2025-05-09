package id.ac.ui.cs.advprog.eventsphere.report.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
    }

    @Test
    public void testGetUserEmail() {
        UUID userId = UUID.randomUUID();
        String email = userService.getUserEmail(userId);

        assertEquals("user@example.com", email);
    }

    @Test
    public void testGetAdminEmails() {
        List<String> adminEmails = userService.getAdminEmails();

        assertNotNull(adminEmails);
        assertEquals(2, adminEmails.size());
        assertTrue(adminEmails.contains("admin1@example.com"));
        assertTrue(adminEmails.contains("admin2@example.com"));
    }

    @Test
    public void testGetOrganizerEmails() {
        UUID eventId = UUID.randomUUID();
        List<String> organizerEmails = userService.getOrganizerEmails(eventId);

        assertNotNull(organizerEmails);
        assertEquals(1, organizerEmails.size());
        assertEquals("organizer@example.com", organizerEmails.get(0));
    }

    @Test
    public void testGetAdminIds() {
        List<UUID> adminIds = userService.getAdminIds();

        assertNotNull(adminIds);
        assertEquals(2, adminIds.size());
        assertTrue(adminIds.contains(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")));
        assertTrue(adminIds.contains(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12")));
    }

    @Test
    public void testGetOrganizerIds() {
        UUID eventId = UUID.randomUUID();
        List<UUID> organizerIds = userService.getOrganizerIds(eventId);

        assertNotNull(organizerIds);
        assertEquals(1, organizerIds.size());
        assertEquals(UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13"), organizerIds.get(0));
    }
}
