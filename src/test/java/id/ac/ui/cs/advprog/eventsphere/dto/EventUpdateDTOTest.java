package id.ac.ui.cs.advprog.eventsphere.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EventUpdateDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void eventUpdateDTO_valid() {
        EventUpdateDTO dto = new EventUpdateDTO();
        dto.setTitle("Updated Workshop");
        dto.setDescription("Updated description");
        dto.setEventDate(LocalDateTime.now().plusDays(5));
        dto.setLocation("New location");
        dto.setPrice(new BigDecimal("150.00"));

        Set<ConstraintViolation<EventUpdateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void eventUpdateDTO_eventDateInPast() {
        EventUpdateDTO dto = new EventUpdateDTO();
        dto.setTitle("Updated Workshop");
        dto.setEventDate(LocalDateTime.now().minusDays(1));
        dto.setPrice(new BigDecimal("150.00"));

        Set<ConstraintViolation<EventUpdateDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Event date must be in the future", violations.iterator().next().getMessage());
    }

    @Test
    void eventUpdateDTO_priceIsNegative() {
        EventUpdateDTO dto = new EventUpdateDTO();
        dto.setTitle("Updated Workshop");
        dto.setEventDate(LocalDateTime.now().plusDays(5));
        dto.setPrice(new BigDecimal("-50.00"));

        Set<ConstraintViolation<EventUpdateDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Price must be positive", violations.iterator().next().getMessage());
    }

    @Test
    void eventUpdateDTO_onlyUpdateTitle() {
        EventUpdateDTO dto = new EventUpdateDTO();
        dto.setTitle("Updated Title Only");

        Set<ConstraintViolation<EventUpdateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }
}