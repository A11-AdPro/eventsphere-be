// UserServiceTest.java
package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

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
    @DisplayName("Mendapatkan email pengguna berdasarkan ID yang valid")
    public void testGetUserEmail() {
        // Arrange
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setEmail("user@example.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act
        String email = userService.getUserEmail(userId);

        // Assert
        assertEquals("user@example.com", email);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Melempar EntityNotFoundException ketika pengguna tidak ditemukan")
    public void testGetUserEmail_UserNotFound() {
        // Arrange
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserEmail(userId)
        );

        assertTrue(exception.getMessage().contains("User not found with ID: " + userId));
    }


    @Test
    @DisplayName("Mendapatkan daftar email admin, dari semua pengguna")
    public void testGetAdminEmails() {
        // Arrange
        User admin1 = new User();
        admin1.setEmail("admin1@example.com");
        admin1.setRole(Role.ADMIN);

        User admin2 = new User();
        admin2.setEmail("admin2@example.com");
        admin2.setRole(Role.ADMIN);

        User user = new User();
        user.setEmail("user@example.com");
        user.setRole(Role.ATTENDEE);

        when(userRepository.findAll()).thenReturn(Arrays.asList(admin1, admin2, user));

        // Act
        List<String> adminEmails = userService.getAdminEmails();

        // Assert
        assertNotNull(adminEmails);
        assertEquals(2, adminEmails.size());
        assertTrue(adminEmails.contains("admin1@example.com"));
        assertTrue(adminEmails.contains("admin2@example.com"));
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Mendapatkan daftar email organizer, untuk event tertentu")
    public void testGetOrganizerEmails() {
        // Arrange
        UUID eventId = UUID.randomUUID();

        User organizer = new User();
        organizer.setEmail("organizer@example.com");
        organizer.setRole(Role.ORGANIZER);

        User attendee = new User();
        attendee.setEmail("attendee@example.com");
        attendee.setRole(Role.ATTENDEE);

        when(userRepository.findAll()).thenReturn(Arrays.asList(organizer, attendee));

        // Act
        List<String> organizerEmails = userService.getOrganizerEmails(eventId);

        // Assert
        assertNotNull(organizerEmails);
        assertEquals(1, organizerEmails.size());
        assertEquals("organizer@example.com", organizerEmails.getFirst());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Mendapatkan daftar ID admin, dari semua pengguna")
    public void testGetAdminIds() {
        // Arrange
        User admin1 = new User();
        admin1.setId(1L);
        admin1.setRole(Role.ADMIN);

        User admin2 = new User();
        admin2.setId(2L);
        admin2.setRole(Role.ADMIN);

        User user = new User();
        user.setId(3L);
        user.setRole(Role.ATTENDEE);

        when(userRepository.findAll()).thenReturn(Arrays.asList(admin1, admin2, user));

        // Act
        List<Long> adminIds = userService.getAdminIds();

        // Assert
        assertNotNull(adminIds);
        assertEquals(2, adminIds.size());
        assertTrue(adminIds.contains(1L));
        assertTrue(adminIds.contains(2L));
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Mendapatkan daftar ID organizer, untuk event tertentu")
    public void testGetOrganizerIds() {
        // Arrange
        UUID eventId = UUID.randomUUID();

        User organizer = new User();
        organizer.setId(1L);
        organizer.setRole(Role.ORGANIZER);

        User attendee = new User();
        attendee.setId(2L);
        attendee.setRole(Role.ATTENDEE);

        when(userRepository.findAll()).thenReturn(Arrays.asList(organizer, attendee));

        // Act
        List<Long> organizerIds = userService.getOrganizerIds(eventId);

        // Assert
        assertNotNull(organizerIds);
        assertEquals(1, organizerIds.size());
        assertEquals(1L, organizerIds.getFirst());
        verify(userRepository).findAll();
    }
}