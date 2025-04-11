package id.ac.ui.cs.advprog.eventsphere.event.service;

import id.ac.ui.cs.advprog.eventsphere.event.dto.EventCreateDTO;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventUpdateDTO;
import id.ac.ui.cs.advprog.eventsphere.event.dto.UserSummaryDTO;
import id.ac.ui.cs.advprog.eventsphere.event.exception.EventNotFoundException;
import id.ac.ui.cs.advprog.eventsphere.event.exception.UnauthorizedAccessException;
import id.ac.ui.cs.advprog.eventsphere.event.model.Event;
import id.ac.ui.cs.advprog.eventsphere.event.model.User;
import id.ac.ui.cs.advprog.eventsphere.event.model.UserRole;
import id.ac.ui.cs.advprog.eventsphere.event.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    private User organizer;
    private User anotherUser;
    private Event event;
    private EventCreateDTO eventCreateDTO;
    private EventUpdateDTO eventUpdateDTO;
    private EventResponseDTO eventResponseDTO;
    private UserSummaryDTO userSummaryDTO;

    @BeforeEach
    void setUp() {
        // Set up test data
        organizer = User.builder()
                .id(1L)
                .username("organizer")
                .password("password")
                .name("Event Organizer")
                .email("organizer@example.com")
                .role(UserRole.ROLE_ORGANIZER)
                .build();

        anotherUser = User.builder()
                .id(2L)
                .username("user2")
                .password("password")
                .name("Another User")
                .email("another@example.com")
                .role(UserRole.ROLE_ORGANIZER)
                .build();

        LocalDateTime eventDate = LocalDateTime.now().plusDays(10);
        event = Event.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .eventDate(eventDate)
                .location("Test Location")
                .price(new BigDecimal("100.00"))
                .organizer(organizer)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        eventCreateDTO = new EventCreateDTO();
        eventCreateDTO.setTitle("New Event");
        eventCreateDTO.setDescription("New Description");
        eventCreateDTO.setEventDate(eventDate);
        eventCreateDTO.setLocation("New Location");
        eventCreateDTO.setPrice(new BigDecimal("150.00"));

        eventUpdateDTO = new EventUpdateDTO();
        eventUpdateDTO.setTitle("Updated Event");
        eventUpdateDTO.setDescription("Updated Description");
        eventUpdateDTO.setEventDate(eventDate);
        eventUpdateDTO.setLocation("Updated Location");
        eventUpdateDTO.setPrice(new BigDecimal("200.00"));

        userSummaryDTO = new UserSummaryDTO();
        userSummaryDTO.setId(organizer.getId());
        userSummaryDTO.setUsername(organizer.getUsername());
        userSummaryDTO.setName(organizer.getName());

        eventResponseDTO = new EventResponseDTO();
        eventResponseDTO.setId(1L);
        eventResponseDTO.setTitle("Test Event");
        eventResponseDTO.setDescription("Test Description");
        eventResponseDTO.setEventDate(eventDate);
        eventResponseDTO.setLocation("Test Location");
        eventResponseDTO.setPrice(new BigDecimal("100.00"));
        eventResponseDTO.setOrganizer(userSummaryDTO);
        eventResponseDTO.setCreatedAt(event.getCreatedAt());
        eventResponseDTO.setUpdatedAt(event.getUpdatedAt());
    }

    @Test
    void createEvent_ShouldReturnEventResponseDTO() {
        // Arrange
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(modelMapper.map(any(Event.class), eq(EventResponseDTO.class))).thenReturn(eventResponseDTO);

        // Act
        EventResponseDTO result = eventService.createEvent(eventCreateDTO, organizer);

        // Assert
        assertNotNull(result);
        assertEquals(eventResponseDTO.getId(), result.getId());
        assertEquals(eventResponseDTO.getTitle(), result.getTitle());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void getAllEvents_ShouldReturnListOfActiveEvents() {
        // Arrange
        List<Event> events = Arrays.asList(event);
        when(eventRepository.findByIsActiveTrue()).thenReturn(events);
        when(modelMapper.map(any(Event.class), eq(EventResponseDTO.class))).thenReturn(eventResponseDTO);

        // Act
        List<EventResponseDTO> result = eventService.getAllEvents();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(eventResponseDTO.getId(), result.get(0).getId());
        verify(eventRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void getEventById_WithValidId_ShouldReturnEventResponseDTO() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(modelMapper.map(any(Event.class), eq(EventResponseDTO.class))).thenReturn(eventResponseDTO);

        // Act
        EventResponseDTO result = eventService.getEventById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(eventResponseDTO.getId(), result.getId());
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void getEventById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EventNotFoundException.class, () -> {
            eventService.getEventById(999L);
        });
        verify(eventRepository, times(1)).findById(999L);
    }

    @Test
    void getEventsByOrganizer_ShouldReturnListOfOrganizerEvents() {
        // Arrange
        List<Event> events = Arrays.asList(event);
        when(eventRepository.findByOrganizerAndIsActiveTrue(organizer)).thenReturn(events);
        when(modelMapper.map(any(Event.class), eq(EventResponseDTO.class))).thenReturn(eventResponseDTO);

        // Act
        List<EventResponseDTO> result = eventService.getEventsByOrganizer(organizer);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(eventResponseDTO.getId(), result.get(0).getId());
        verify(eventRepository, times(1)).findByOrganizerAndIsActiveTrue(organizer);
    }

    @Test
    void updateEvent_WithValidIdAndOwner_ShouldReturnUpdatedEvent() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(modelMapper.map(any(Event.class), eq(EventResponseDTO.class))).thenReturn(eventResponseDTO);

        // Act
        EventResponseDTO result = eventService.updateEvent(1L, eventUpdateDTO, organizer);

        // Assert
        assertNotNull(result);
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void updateEvent_WithUnauthorizedUser_ShouldThrowException() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> {
            eventService.updateEvent(1L, eventUpdateDTO, anotherUser);
        });
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void updateEvent_WithEventTooClose_ShouldThrowException() {
        // Arrange
        Event soonEvent = Event.builder()
                .id(2L)
                .title("Soon Event")
                .eventDate(LocalDateTime.now().plusHours(12))
                .organizer(organizer)
                .build();
        when(eventRepository.findById(2L)).thenReturn(Optional.of(soonEvent));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            eventService.updateEvent(2L, eventUpdateDTO, organizer);
        });
        verify(eventRepository, times(1)).findById(2L);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void deleteEvent_WithValidIdAndOwner_ShouldSoftDeleteEvent() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        
        // Act
        eventService.deleteEvent(1L, organizer);

        // Assert
        assertFalse(event.isActive());
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void deleteEvent_WithUnauthorizedUser_ShouldThrowException() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () -> {
            eventService.deleteEvent(1L, anotherUser);
        });
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, never()).save(any(Event.class));
    }
}