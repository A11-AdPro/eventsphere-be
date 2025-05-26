package id.ac.ui.cs.advprog.eventsphere.event.controller;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventCreateDTO;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventUpdateDTO;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.event.exception.UnauthorizedAccessException;
import id.ac.ui.cs.advprog.eventsphere.event.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDetails userDetails;
    @InjectMocks
    private EventController eventController;

    private User organizer;
    private EventCreateDTO createDTO;
    private EventUpdateDTO updateDTO;
    private EventResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        organizer = new User();
        organizer.setId(100L);
        organizer.setEmail("organizer@test.com");

        createDTO = new EventCreateDTO();
        createDTO.setTitle("My Event");
        createDTO.setDescription("Desc");
        createDTO.setLocation("Location");
        createDTO.setPrice(BigDecimal.TEN);
        createDTO.setEventDate(LocalDateTime.now().plusDays(5));

        updateDTO = new EventUpdateDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setDescription("Updated Desc");
        updateDTO.setLocation("New Location");
        updateDTO.setPrice(BigDecimal.valueOf(20));
        updateDTO.setEventDate(LocalDateTime.now().plusDays(10));

        responseDTO = new EventResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitle("My Event");
        responseDTO.setOrganizerId(organizer.getId());
    }

    @Test
    void testCreateEvent_Success() {
        mockAuthorization();
        when(userRepository.findByEmail("organizer@test.com"))
                .thenReturn(Optional.of(organizer));
        when(eventService.createEvent(eq(createDTO), eq(organizer)))
                .thenReturn(responseDTO);

        ResponseEntity<EventResponseDTO> response =
                eventController.createEvent(createDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
        verify(eventService).createEvent(eq(createDTO), eq(organizer));
    }

    @Test
    void testGetAllEvents() {
        List<EventResponseDTO> mockEvents = Arrays.asList(responseDTO);
        when(eventService.getAllActiveEvents()).thenReturn(mockEvents);

        ResponseEntity<List<EventResponseDTO>> response =
                eventController.getAllEvents();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(eventService).getAllActiveEvents();
    }

    @Test
    void testGetActiveEventById() {
        when(eventService.getActiveEventById(1L)).thenReturn(responseDTO);

        ResponseEntity<EventResponseDTO> response =
                eventController.getActiveEventById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
        verify(eventService).getActiveEventById(1L);
    }

    @Test
    void testGetOrganizerEvents_Success() {
        mockAuthorization();
        when(userRepository.findByEmail("organizer@test.com"))
                .thenReturn(Optional.of(organizer));

        List<EventResponseDTO> mockEvents = Collections.singletonList(responseDTO);
        when(eventService.getActiveEventsByOrganizer(organizer)).thenReturn(mockEvents);

        ResponseEntity<List<EventResponseDTO>> response =
                eventController.getOrganizerEvents();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(eventService).getActiveEventsByOrganizer(organizer);
    }

    @Test
    void testUpdateEvent_Success() {
        mockAuthorization();
        when(userRepository.findByEmail("organizer@test.com"))
                .thenReturn(Optional.of(organizer));

        EventResponseDTO updatedDTO = new EventResponseDTO();
        updatedDTO.setId(1L);
        updatedDTO.setTitle("Updated Title");
        when(eventService.updateEvent(eq(1L), eq(updateDTO), eq(organizer)))
                .thenReturn(updatedDTO);

        ResponseEntity<EventResponseDTO> response =
                eventController.updateEvent(1L, updateDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedDTO, response.getBody());
        verify(eventService).updateEvent(eq(1L), eq(updateDTO), eq(organizer));
    }

    @Test
    void testCancelEvent_Success() {
        mockAuthorization();
        when(userRepository.findByEmail("organizer@test.com"))
                .thenReturn(Optional.of(organizer));
        when(eventService.cancelEvent(1L, organizer))
                .thenReturn("Event canceled");

        ResponseEntity<Map<String, String>> response =
                eventController.cancelEvent(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Event canceled", response.getBody().get("message"));
        verify(eventService).cancelEvent(1L, organizer);
    }

    @Test
    void testDeleteEvent_Success() {
        mockAuthorization();
        when(userRepository.findByEmail("organizer@test.com"))
                .thenReturn(Optional.of(organizer));
        when(eventService.deleteEvent(1L, organizer))
                .thenReturn("Event deleted");

        ResponseEntity<Map<String, String>> response =
                eventController.deleteEvent(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Event deleted", response.getBody().get("message"));
        verify(eventService).deleteEvent(1L, organizer);
    }

    @Test
    void testCreateEvent_UserNotFound() {
        mockAuthorization();
        when(userRepository.findByEmail("organizer@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(UnauthorizedAccessException.class,
                () -> eventController.createEvent(createDTO));

        verify(eventService, never()).createEvent(any(), any());
    }

    private void mockAuthorization() {
        SecurityContext context = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(context.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("organizer@test.com");
        SecurityContextHolder.setContext(context);
    }
}