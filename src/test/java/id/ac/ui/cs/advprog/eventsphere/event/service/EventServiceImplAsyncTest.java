package id.ac.ui.cs.advprog.eventsphere.event.service;

import id.ac.ui.cs.advprog.eventsphere.event.dto.*;
import id.ac.ui.cs.advprog.eventsphere.event.exception.*;
import id.ac.ui.cs.advprog.eventsphere.event.model.Event;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.event.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.AsyncResult;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplAsyncTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ModelMapper modelMapper;
    
    @InjectMocks
    private EventServiceImpl eventService;

    @Captor
    private ArgumentCaptor<Event> eventArgumentCaptor;

    private Event testEvent;
    private User organizer;
    private EventCreateDTO createDTO; 
    private EventUpdateDTO updateDTO; 
    private Event eventStateAfterSave; 
    private EventResponseDTO expectedResponseDTO;
    
    private Long eventId = 100L;

    @BeforeEach
    void setUp() {
        organizer = new User();
        organizer.setId(1L);

        testEvent = new Event();
        testEvent.setId(eventId);
        testEvent.setTitle("Original Title");
        testEvent.setDescription("Original Desc");
        testEvent.setEventDate(LocalDateTime.now().plusDays(5));
        testEvent.setLocation("Original Location");
        testEvent.setPrice(BigDecimal.TEN);
        testEvent.setActive(true);
        testEvent.setCancelled(false);
        testEvent.setOrganizer(organizer);

        createDTO = new EventCreateDTO();
        createDTO.setTitle("New Event");
        createDTO.setDescription("New Desc");
        createDTO.setLocation("New Location");
        createDTO.setPrice(BigDecimal.valueOf(20));
        createDTO.setEventDate(LocalDateTime.now().plusDays(7));

        updateDTO = new EventUpdateDTO();
        updateDTO.setTitle("Updated Title");
        updateDTO.setDescription("Updated Desc");
        updateDTO.setLocation("Updated Location");
        updateDTO.setPrice(BigDecimal.valueOf(50));
        updateDTO.setEventDate(LocalDateTime.now().plusDays(10));

        eventStateAfterSave = new Event();
        eventStateAfterSave.setId(eventId);
        eventStateAfterSave.setTitle(updateDTO.getTitle());
        eventStateAfterSave.setDescription(updateDTO.getDescription());
        eventStateAfterSave.setLocation(updateDTO.getLocation());
        eventStateAfterSave.setPrice(updateDTO.getPrice());
        eventStateAfterSave.setEventDate(updateDTO.getEventDate());
        eventStateAfterSave.setOrganizer(organizer);
        eventStateAfterSave.setActive(true);
        eventStateAfterSave.setCancelled(false);

        expectedResponseDTO = new EventResponseDTO();
        expectedResponseDTO.setId(eventId);
        expectedResponseDTO.setTitle(updateDTO.getTitle());
        expectedResponseDTO.setDescription(updateDTO.getDescription());
        expectedResponseDTO.setLocation(updateDTO.getLocation());
        expectedResponseDTO.setPrice(updateDTO.getPrice());
        expectedResponseDTO.setEventDate(updateDTO.getEventDate());
    }

    @Test
    void createEventAsync_Success() throws Exception {
        Event mapped = new Event();
        mapped.setTitle("New Event");
        mapped.setEventDate(createDTO.getEventDate());

        when(modelMapper.map(createDTO, Event.class)).thenReturn(mapped);
        when(eventRepository.save(any())).thenAnswer(inv -> {
            Event e = inv.getArgument(0);
            e.setId(999L);
            return e;
        });

        EventResponseDTO responseDto = new EventResponseDTO();
        responseDto.setId(999L);
        responseDto.setTitle("New Event");
        when(modelMapper.map(any(Event.class), eq(EventResponseDTO.class))).thenReturn(responseDto);
    
        CompletableFuture<EventResponseDTO> future = eventService.createEventAsync(createDTO, organizer);
        EventResponseDTO result = future.get(1, TimeUnit.SECONDS);

        assertNotNull(result);
        assertEquals(999L, result.getId());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEventAsync_Error() {
        when(modelMapper.map(createDTO, Event.class)).thenThrow(new RuntimeException("Mapping error"));

        CompletableFuture<EventResponseDTO> future = eventService.createEventAsync(createDTO, organizer);

        assertThrows(ExecutionException.class, () -> {
            future.get(1, TimeUnit.SECONDS);
        });
    }

    @Test
    void getAllActiveEventsAsync_Success() throws Exception {
        
        when(eventRepository.findByIsActiveTrue()).thenReturn(List.of(testEvent));
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(testEvent.getId());
        when(modelMapper.map(testEvent, EventResponseDTO.class)).thenReturn(dto);

        CompletableFuture<List<EventResponseDTO>> future = eventService.getAllActiveEventsAsync();
        List<EventResponseDTO> result = future.get(1, TimeUnit.SECONDS);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
    }

    @Test
    void getActiveEventByIdAsync_Success() throws Exception {
        
        when(eventRepository.findByIdAndIsActiveTrue(100L))
                .thenReturn(Optional.of(testEvent));
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(100L);
        when(modelMapper.map(testEvent, EventResponseDTO.class)).thenReturn(dto);

        CompletableFuture<EventResponseDTO> future = eventService.getActiveEventByIdAsync(100L);
        EventResponseDTO result = future.get(1, TimeUnit.SECONDS);

        assertEquals(100L, result.getId());
    }

    @Test
    void getActiveEventByIdAsync_NotFound() {
        when(eventRepository.findByIdAndIsActiveTrue(200L))
                .thenReturn(Optional.empty());

        CompletableFuture<EventResponseDTO> future = eventService.getActiveEventByIdAsync(200L);

        assertThrows(ExecutionException.class, () -> {
            future.get(1, TimeUnit.SECONDS);
        });
    }

    @Test
    void getActiveEventsByOrganizerAsync_Success() throws Exception {
        
        when(eventRepository.findByOrganizerAndIsActiveTrue(organizer))
                .thenReturn(List.of(testEvent));
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(testEvent.getId());
        when(modelMapper.map(testEvent, EventResponseDTO.class)).thenReturn(dto);

        CompletableFuture<List<EventResponseDTO>> future = eventService.getActiveEventsByOrganizerAsync(organizer);
        List<EventResponseDTO> result = future.get(1, TimeUnit.SECONDS);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getId());
    }

    @Test
    void updateEventAsync_Success() throws Exception {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));

        doAnswer(invocation -> {
            EventUpdateDTO dtoArg = invocation.getArgument(0);
            Event eventArg = invocation.getArgument(1);
            eventArg.setTitle(dtoArg.getTitle());
            eventArg.setDescription(dtoArg.getDescription());
            eventArg.setLocation(dtoArg.getLocation());
            eventArg.setPrice(dtoArg.getPrice());
            eventArg.setEventDate(dtoArg.getEventDate());
            return null;
        }).when(modelMapper).map(eq(updateDTO), eq(testEvent));

        when(eventRepository.save(any(Event.class))).thenReturn(eventStateAfterSave);

        when(modelMapper.map(eq(eventStateAfterSave), eq(EventResponseDTO.class))).thenReturn(expectedResponseDTO);
        
        CompletableFuture<EventResponseDTO> future = eventService.updateEventAsync(eventId, updateDTO, organizer);
        EventResponseDTO result = future.get(1, TimeUnit.SECONDS);

        assertNotNull(result, "Response DTO tidak boleh null.");
        assertEquals(expectedResponseDTO.getId(), result.getId(), "ID Event pada response tidak cocok.");
        assertEquals(expectedResponseDTO.getTitle(), result.getTitle(), "Judul Event pada response tidak cocok.");
        assertEquals(expectedResponseDTO.getDescription(), result.getDescription(), "Deskripsi Event pada response tidak cocok.");
        assertSame(expectedResponseDTO, result, "Instance DTO yang dikembalikan seharusnya sama dengan yang dari stub.");

        verify(eventRepository, times(1)).findById(eq(eventId));
        verify(modelMapper, times(1)).map(eq(updateDTO), eq(testEvent));
        verify(eventRepository, times(1)).save(eventArgumentCaptor.capture());

        Event capturedEventForSave = eventArgumentCaptor.getValue();
        assertNotNull(capturedEventForSave, "Event yang di-pass ke save tidak boleh null.");
        assertEquals(updateDTO.getTitle(), capturedEventForSave.getTitle(), "Judul event yang di-save tidak terupdate.");
        assertEquals(updateDTO.getDescription(), capturedEventForSave.getDescription(), "Deskripsi event yang di-save tidak terupdate.");
        assertSame(testEvent, capturedEventForSave, "Instance event yang di-save seharusnya adalah instance yang sama yang diambil dari findById dan dimodifikasi.");


        verify(modelMapper, times(1)).map(eq(eventStateAfterSave), eq(EventResponseDTO.class));
    }

    @Test
    void updateEventAsync_NotFound() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        CompletableFuture<EventResponseDTO> future = eventService.updateEventAsync(eventId, updateDTO, organizer);

        assertThrows(ExecutionException.class, () -> {
            future.get(1, TimeUnit.SECONDS);
        });
    }

    @Test
    void cancelEventAsync_Success() throws Exception {
        
        when(eventRepository.findById(100L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(testEvent)).thenReturn(testEvent);

        CompletableFuture<String> future = eventService.cancelEventAsync(100L, organizer);
        String result = future.get(1, TimeUnit.SECONDS);

        assertTrue(testEvent.isCancelled());
        assertEquals("Event with ID 100 has been canceled successfully", result);
    }

    @Test
    void deleteEventAsync_Success() throws Exception {
        
        testEvent.setCancelled(true);
        when(eventRepository.findById(100L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(testEvent)).thenReturn(testEvent);

        CompletableFuture<String> future = eventService.deleteEventAsync(100L, organizer);
        String result = future.get(1, TimeUnit.SECONDS);

        assertFalse(testEvent.isActive());
        assertEquals("Event with ID 100 has been deleted successfully", result);
    }

    @Test
    void deleteEventAsync_NotCancelledOrPast() {
        when(eventRepository.findById(100L)).thenReturn(Optional.of(testEvent));

        CompletableFuture<String> future = eventService.deleteEventAsync(100L, organizer);

        assertThrows(ExecutionException.class, () -> {
            future.get(1, TimeUnit.SECONDS);
        });
    }

    @Test
    void testOwnershipViolationAsync() {
        User another = new User();
        another.setId(999L);
        when(eventRepository.findById(100L)).thenReturn(Optional.of(testEvent));

        CompletableFuture<String> future = eventService.cancelEventAsync(100L, another);

        assertThrows(ExecutionException.class, () -> {
            future.get(1, TimeUnit.SECONDS);
        });
    }
}