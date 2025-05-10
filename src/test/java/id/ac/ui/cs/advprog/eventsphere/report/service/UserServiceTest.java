package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    public void testGetUserEmail() {
        // Create test data
        Long userId = 1L;

        // Mock user repository behavior
        User mockUser = new User();
        mockUser.setEmail("user@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Call service method
        String email = userService.getUserEmail(userId);

        // Verify result
        assertEquals("user@example.com", email);

        // Verify repository interaction
        verify(userRepository).findById(userId);
    }

    @Test
    public void testGetUserEmail_UserNotFound() {
        // Create test data
        Long userId = 99L;

        // Mock repository behavior
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Call service method
        String email = userService.getUserEmail(userId);

        // Verify default email is returned
        assertEquals("user@example.com", email);
    }

    @Test
    public void testGetAdminEmails() {
        // Mock users
        User admin1 = new User();
        admin1.setEmail("admin1@example.com");
        admin1.setRole(Role.ADMIN);

        User admin2 = new User();
        admin2.setEmail("admin2@example.com");
        admin2.setRole(Role.ADMIN);

        User user = new User();
        user.setEmail("user@example.com");
        user.setRole(Role.ATTENDEE);

        // Mock repository behavior
        when(userRepository.findAll()).thenReturn(Arrays.asList(admin1, admin2, user));

        // Call service method
        List<String> adminEmails = userService.getAdminEmails();

        // Verify results
        assertNotNull(adminEmails);
        assertEquals(2, adminEmails.size());
        assertTrue(adminEmails.contains("admin1@example.com"));
        assertTrue(adminEmails.contains("admin2@example.com"));

        // Verify repository interaction
        verify(userRepository).findAll();
    }

    @Test
    public void testGetOrganizerEmails() {
        // Create test data
        UUID eventId = UUID.randomUUID();

        // Mock users
        User organizer = new User();
        organizer.setEmail("organizer@example.com");
        organizer.setRole(Role.ORGANIZER);

        User attendee = new User();
        attendee.setEmail("attendee@example.com");
        attendee.setRole(Role.ATTENDEE);

        // Mock repository behavior
        when(userRepository.findAll()).thenReturn(Arrays.asList(organizer, attendee));

        // Call service method
        List<String> organizerEmails = userService.getOrganizerEmails(eventId);

        // Verify results
        assertNotNull(organizerEmails);
        assertEquals(1, organizerEmails.size());
        assertEquals("organizer@example.com", organizerEmails.get(0));

        // Verify repository interaction
        verify(userRepository).findAll();
    }

    @Test
    public void testGetAdminIds() {
        // Mock users
        User admin1 = new User();
        admin1.setId(1L);
        admin1.setRole(Role.ADMIN);

        User admin2 = new User();
        admin2.setId(2L);
        admin2.setRole(Role.ADMIN);

        User user = new User();
        user.setId(3L);
        user.setRole(Role.ATTENDEE);

        // Mock repository behavior
        when(userRepository.findAll()).thenReturn(Arrays.asList(admin1, admin2, user));

        // Call service method
        List<Long> adminIds = userService.getAdminIds();

        // Verify results
        assertNotNull(adminIds);
        assertEquals(2, adminIds.size());
        assertTrue(adminIds.contains(1L));
        assertTrue(adminIds.contains(2L));

        // Verify repository interaction
        verify(userRepository).findAll();
    }

    @Test
    public void testGetOrganizerIds() {
        // Create test data
        UUID eventId = UUID.randomUUID();

        // Mock users
        User organizer = new User();
        organizer.setId(1L);
        organizer.setRole(Role.ORGANIZER);

        User attendee = new User();
        attendee.setId(2L);
        attendee.setRole(Role.ATTENDEE);

        // Mock repository behavior
        when(userRepository.findAll()).thenReturn(Arrays.asList(organizer, attendee));

        // Call service method
        List<Long> organizerIds = userService.getOrganizerIds(eventId);

        // Verify results
        assertNotNull(organizerIds);
        assertEquals(1, organizerIds.size());
        assertEquals(1L, organizerIds.get(0));

        // Verify repository interaction
        verify(userRepository).findAll();
    }
}