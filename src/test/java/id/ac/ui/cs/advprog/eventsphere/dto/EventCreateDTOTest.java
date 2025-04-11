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

class EventCreateDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void eventCreateDTO_valid() {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setTitle("Spring Boot Workshop");
        dto.setDescription("Learn Spring Boot in depth");
        dto.setEventDate(LocalDateTime.now().plusDays(1));
        dto.setLocation("Online");
        dto.setPrice(new BigDecimal("100.00"));

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void eventCreateDTO_titleIsNull() {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setTitle(null);
        dto.setDescription("Learn Spring Boot in depth");
        dto.setEventDate(LocalDateTime.now().plusDays(1));
        dto.setLocation("Online");
        dto.setPrice(new BigDecimal("100.00"));

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Title is required", violations.iterator().next().getMessage());
    }

    @Test
    void eventCreateDTO_titleIsEmpty() {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setTitle("");
        dto.setDescription("Learn Spring Boot in depth");
        dto.setEventDate(LocalDateTime.now().plusDays(1));
        dto.setLocation("Online");
        dto.setPrice(new BigDecimal("100.00"));

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Title is required", violations.iterator().next().getMessage());
    }

    @Test
    void eventCreateDTO_eventDateIsNull() {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setTitle("Spring Boot Workshop");
        dto.setDescription("Learn Spring Boot in depth");
        dto.setEventDate(null);
        dto.setLocation("Online");
        dto.setPrice(new BigDecimal("100.00"));

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Event date is required", violations.iterator().next().getMessage());
    }

    @Test
    void eventCreateDTO_eventDateIsInPast() {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setTitle("Spring Boot Workshop");
        dto.setDescription("Learn Spring Boot in depth");
        dto.setEventDate(LocalDateTime.now().minusDays(1));
        dto.setLocation("Online");
        dto.setPrice(new BigDecimal("100.00"));

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Event date must be in the future", violations.iterator().next().getMessage());
    }

    @Test
    void eventCreateDTO_locationIsNull() {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setTitle("Spring Boot Workshop");
        dto.setDescription("Learn Spring Boot in depth");
        dto.setEventDate(LocalDateTime.now().plusDays(1));
        dto.setLocation(null);
        dto.setPrice(new BigDecimal("100.00"));

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Location is required", violations.iterator().next().getMessage());
    }

    @Test
    void eventCreateDTO_locationIsEmpty() {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setTitle("Spring Boot Workshop");
        dto.setDescription("Learn Spring Boot in depth");
        dto.setEventDate(LocalDateTime.now().plusDays(1));
        dto.setLocation("");
        dto.setPrice(new BigDecimal("100.00"));

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Location is required", violations.iterator().next().getMessage());
    }

    @Test
    void eventCreateDTO_priceIsNull() {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setTitle("Spring Boot Workshop");
        dto.setDescription("Learn Spring Boot in depth");
        dto.setEventDate(LocalDateTime.now().plusDays(1));
        dto.setLocation("Online");
        dto.setPrice(null);

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Price is required", violations.iterator().next().getMessage());
    }

    @Test
    void eventCreateDTO_priceIsNegative() {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setTitle("Spring Boot Workshop");
        dto.setDescription("Learn Spring Boot in depth");
        dto.setEventDate(LocalDateTime.now().plusDays(1));
        dto.setLocation("Online");
        dto.setPrice(new BigDecimal("-10.00"));

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Price must be positive", violations.iterator().next().getMessage());
    }

    @Test
    void eventCreateDTO_priceIsZero() {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setTitle("Spring Boot Workshop");
        dto.setDescription("Learn Spring Boot in depth");
        dto.setEventDate(LocalDateTime.now().plusDays(1));
        dto.setLocation("Online");
        dto.setPrice(new BigDecimal("0.00"));

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto);
        assertEquals(1, violations.size());
        assertEquals("Price must be positive", violations.iterator().next().getMessage());
    }
}