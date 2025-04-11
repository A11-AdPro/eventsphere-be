package id.ac.ui.cs.advprog.eventsphere.event.controller;

import id.ac.ui.cs.advprog.eventsphere.event.dto.EventCreateDTO;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventUpdateDTO;
import id.ac.ui.cs.advprog.eventsphere.event.dto.UserSummaryDTO;
import id.ac.ui.cs.advprog.eventsphere.event.model.User;
import id.ac.ui.cs.advprog.eventsphere.event.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.event.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventController eventController;

    private User testUser;
    private EventCreateDTO createDTO;
    private EventUpdateDTO updateDTO;
    private EventResponseDTO responseDTO;
    private List<EventResponseDTO> eventList;
    private UserSummaryDTO userSummaryDTO;

    @BeforeEach
    void setUp() {
        // Setup test User
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test Organizer");
        
        // Setup UserSummaryDTO
        userSummaryDTO = new UserSummaryDTO();
        userSummaryDTO.setId(1L);
        userSummaryDTO.setName("Test Organizer");
        userSummaryDTO.setUsername("testorg");

        // Setup test DTOs
        LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
        
        createDTO = new EventCreateDTO();
        createDTO.setTitle("Test Event");
        createDTO.setDescription("Test Description");
        createDTO.setEventDate(futureDate);
        createDTO.setLocation("Test Location");
        createDTO.setPrice(new BigDecimal("100.00"));

        updateDTO = new EventUpdateDTO();
        updateDTO.setTitle("Updated Event");
        updateDTO.setDescription("Updated Description");
        updateDTO.setEventDate(futureDate.plusDays(1));
        updateDTO.setLocation("Updated Location");
        updateDTO.setPrice(new BigDecimal("150.00"));

        responseDTO = new EventResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitle("Test Event");
        responseDTO.setDescription("Test Description");
        responseDTO.setEventDate(futureDate);
        responseDTO.setLocation("Test Location");
        responseDTO.setPrice(new BigDecimal("100.00"));
        responseDTO.setOrganizer(userSummaryDTO);
        responseDTO.setCreatedAt(LocalDateTime.now().minusDays(1));
        responseDTO.setUpdatedAt(LocalDateTime.now());
        responseDTO.setActive(true);

        eventList = Arrays.asList(responseDTO);

        // Setup mock behavior for userRepository
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    }

    @Test
    void testCreateEvent() {
        // Setup mock behavior
        when(eventService.createEvent(any(EventCreateDTO.class), any(User.class)))
                .thenReturn(responseDTO);

        // Call the controller method
        ResponseEntity<EventResponseDTO> response = eventController.createEvent(createDTO);

        // Verify the result
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDTO, response.getBody());
        
        // Verify the service was called
        verify(eventService).createEvent(eq(createDTO), eq(testUser));
    }

    // @Test
    // void testGetAllEvents() {
    //     // Setup mock behavior
    //     when(eventService.getAllEvents()).thenReturn(eventList);

    //     // Call the controller method
    //     ResponseEntity<List<EventResponseDTO>> response = eventController.getAllEvents();

    //     // Verify the result
    //     assertEquals(HttpStatus.OK, response.getStatusCode());
    //     assertEquals(eventList, response.getBody());
        
    //     // Verify the service was called
    //     verify(eventService).getAllEvents();
    // }

    // @Test
    // void testGetEventById() {
    //     // Setup mock behavior
    //     when(eventService.getEventById(1L)).thenReturn(responseDTO);

    //     // Call the controller method
    //     ResponseEntity<EventResponseDTO> response = eventController.getEventById(1L);

    //     // Verify the result
    //     assertEquals(HttpStatus.OK, response.getStatusCode());
    //     assertEquals(responseDTO, response.getBody());
        
    //     // Verify the service was called
    //     verify(eventService).getEventById(1L);
    // }

    @Test
    void testGetOrganizerEvents() {
        // Setup mock behavior
        when(eventService.getEventsByOrganizer(testUser)).thenReturn(eventList);

        // Call the controller method
        ResponseEntity<List<EventResponseDTO>> response = eventController.getOrganizerEvents();

        // Verify the result
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(eventList, response.getBody());
        
        // Verify the service was called
        verify(eventService).getEventsByOrganizer(testUser);
    }

    @Test
    void testUpdateEvent() {
        // Setup mock behavior
        EventResponseDTO updatedResponse = new EventResponseDTO();
        updatedResponse.setId(1L);
        updatedResponse.setTitle("Updated Event");
        updatedResponse.setDescription("Updated Description");
        updatedResponse.setEventDate(LocalDateTime.now().plusDays(8));
        updatedResponse.setLocation("Updated Location");
        updatedResponse.setPrice(new BigDecimal("150.00"));
        updatedResponse.setOrganizer(userSummaryDTO);
        
        when(eventService.updateEvent(eq(1L), any(EventUpdateDTO.class), any(User.class)))
                .thenReturn(updatedResponse);

        // Call the controller method
        ResponseEntity<EventResponseDTO> response = eventController.updateEvent(1L, updateDTO);

        // Verify the result
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedResponse, response.getBody());
        assertEquals("Updated Event", response.getBody().getTitle());
        
        // Verify the service was called
        verify(eventService).updateEvent(eq(1L), eq(updateDTO), eq(testUser));
    }

    @Test
    void testDeleteEvent() {
        // Setup mock behavior
        doNothing().when(eventService).deleteEvent(anyLong(), any(User.class));

        // Call the controller method
        ResponseEntity<Void> response = eventController.deleteEvent(1L);

        // Verify the result
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        
        // Verify the service was called
        verify(eventService).deleteEvent(1L, testUser);
    }

    @Test
    void testGetTestOrganizer_ThrowsException_WhenUserNotFound() {
        // Setup mock to return empty Optional
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Test the exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventController.createEvent(createDTO);
        });
        
        assertEquals("Test organizer not found", exception.getMessage());
    }
    
    // @Test
    // void testGetTestOrganizer() {
    //     // Setup is already done in BeforeEach
        
    //     // Call the method indirectly through one of the controller methods
    //     eventController.getAllEvents();
        
    //     // Verify the repository method was called
    //     verify(userRepository).findById(1L);
    // }
}