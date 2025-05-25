package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.event.service.EventService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserRepository userRepository;
    private EventService eventService;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userRepository = mock(UserRepository.class);
        eventService = mock(EventService.class);
        userService = new UserService(userRepository, eventService);
    }

    @Test
    @DisplayName("Mendapatkan email pengguna berdasarkan ID yang valid")
    public void testGetUserEmail() {
        // Arrange
        User user = new User();
        user.setEmail("user@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        String result = userService.getUserEmail(1L);

        // Assert
        assertEquals("user@example.com", result);
    }

    @Test
    @DisplayName("Melempar exception ketika pengguna tidak ditemukan berdasarkan ID")
    public void testGetUserEmail_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.getUserEmail(1L)
        );
        assertEquals("User not found with ID: 1", exception.getMessage());
    }

    @Test
    @DisplayName("Mendapatkan daftar email admin")
    public void testGetAdminEmails() {
        // Arrange
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setRole(Role.ADMIN);

        User attendee = new User();
        attendee.setEmail("user@example.com");
        attendee.setRole(Role.ATTENDEE);

        when(userRepository.findAll()).thenReturn(Arrays.asList(admin, attendee));

        // Act
        List<String> result = userService.getAdminEmails();

        // Assert
        assertEquals(1, result.size());
        assertEquals("admin@example.com", result.getFirst());
    }

    @Test
    @DisplayName("Mengembalikan daftar kosong ketika tidak ada admin")
    public void testGetAdminEmails_NoAdmins() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<String> result = userService.getAdminEmails();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Mendapatkan email organizer untuk event tertentu berhasil")
    public void testGetOrganizerEmails_Success() {
        // Arrange
        EventResponseDTO event = new EventResponseDTO();
        event.setOrganizerId(5L);

        User organizer = new User();
        organizer.setEmail("organizer@example.com");

        when(eventService.getActiveEventById(1L)).thenReturn(event);
        when(userRepository.findById(5L)).thenReturn(Optional.of(organizer));

        // Act
        List<String> result = userService.getOrganizerEmails(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals("organizer@example.com", result.getFirst());
    }

    @Test
    @DisplayName("Mengembalikan daftar kosong ketika event null")
    public void testGetOrganizerEmails_EventNull() {
        // Arrange
        when(eventService.getActiveEventById(1L)).thenReturn(null);

        // Act
        List<String> result = userService.getOrganizerEmails(1L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Mengembalikan daftar kosong ketika organizer ID null")
    public void testGetOrganizerEmails_OrganizerIdNull() {
        // Arrange
        EventResponseDTO event = new EventResponseDTO();
        event.setOrganizerId(null);

        when(eventService.getActiveEventById(1L)).thenReturn(event);

        // Act
        List<String> result = userService.getOrganizerEmails(1L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Mengembalikan daftar kosong ketika organizer tidak ditemukan")
    public void testGetOrganizerEmails_OrganizerNotFound() {
        // Arrange
        EventResponseDTO event = new EventResponseDTO();
        event.setOrganizerId(5L);

        when(eventService.getActiveEventById(1L)).thenReturn(event);
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        // Act
        List<String> result = userService.getOrganizerEmails(1L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Mengembalikan daftar kosong ketika terjadi exception saat mencari event")
    public void testGetOrganizerEmails_Exception() {
        // Arrange - Test catch (Exception e) block
        when(eventService.getActiveEventById(1L)).thenThrow(new RuntimeException("Service error"));

        // Act
        List<String> result = userService.getOrganizerEmails(1L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Mendapatkan daftar ID admin")
    public void testGetAdminIds() {
        // Arrange
        User admin = new User();
        admin.setId(1L);
        admin.setRole(Role.ADMIN);

        User attendee = new User();
        attendee.setId(2L);
        attendee.setRole(Role.ATTENDEE);

        when(userRepository.findAll()).thenReturn(Arrays.asList(admin, attendee));

        // Act
        List<Long> result = userService.getAdminIds();

        // Assert
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst());
    }

    @Test
    @DisplayName("Mengembalikan daftar kosong ketika tidak ada admin untuk ID")
    public void testGetAdminIds_NoAdmins() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Long> result = userService.getAdminIds();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Mendapatkan ID organizer untuk event tertentu berhasil")
    public void testGetOrganizerIds_Success() {
        // Arrange
        EventResponseDTO event = new EventResponseDTO();
        event.setOrganizerId(5L);

        when(eventService.getActiveEventById(1L)).thenReturn(event);

        // Act
        List<Long> result = userService.getOrganizerIds(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(5L, result.getFirst());
    }

    @Test
    @DisplayName("Mengembalikan daftar kosong ketika event null untuk ID organizer")
    public void testGetOrganizerIds_EventNull() {
        // Arrange
        when(eventService.getActiveEventById(1L)).thenReturn(null);

        // Act
        List<Long> result = userService.getOrganizerIds(1L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Mengembalikan daftar kosong ketika organizer ID null untuk pencarian ID")
    public void testGetOrganizerIds_OrganizerIdNull() {
        // Arrange
        EventResponseDTO event = new EventResponseDTO();
        event.setOrganizerId(null);

        when(eventService.getActiveEventById(1L)).thenReturn(event);

        // Act
        List<Long> result = userService.getOrganizerIds(1L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Mengembalikan daftar kosong ketika terjadi exception saat mencari ID organizer")
    public void testGetOrganizerIds_Exception() {
        // Arrange
        when(eventService.getActiveEventById(1L)).thenThrow(new RuntimeException("Service error"));

        // Act
        List<Long> result = userService.getOrganizerIds(1L);

        // Assert
        assertTrue(result.isEmpty());
    }
}