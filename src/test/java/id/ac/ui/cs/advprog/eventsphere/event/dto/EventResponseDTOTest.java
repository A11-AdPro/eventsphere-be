package id.ac.ui.cs.advprog.eventsphere.event.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventResponseDTOTest {

    private Validator validator;
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

    }

    @Test
    void eventResponseDTO_gettersAndSetters() {
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(1L);
        dto.setTitle("Test Event");
        dto.setDescription("Test Description");
        LocalDateTime eventDate = LocalDateTime.now().plusDays(7);
        dto.setEventDate(eventDate);
        dto.setLocation("Test Location");
        BigDecimal price = new BigDecimal("200.00");
        dto.setPrice(price);

        UserSummaryDTO organizer = new UserSummaryDTO();
        organizer.setId(2L);
        organizer.setUsername("organizer");
        organizer.setName("Event Organizer");
        dto.setOrganizer(organizer);

        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        dto.setActive(true);

        assertEquals(1L, dto.getId());
        assertEquals("Test Event", dto.getTitle());
        assertEquals("Test Description", dto.getDescription());
        assertEquals(eventDate, dto.getEventDate());
        assertEquals("Test Location", dto.getLocation());
        assertEquals(price, dto.getPrice());
        assertEquals(organizer, dto.getOrganizer());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());
        assertTrue(dto.isActive());

        var violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Validation should pass without any violations");
    }
}