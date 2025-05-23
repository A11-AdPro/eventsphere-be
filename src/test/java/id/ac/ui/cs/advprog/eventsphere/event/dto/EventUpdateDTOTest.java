package id.ac.ui.cs.advprog.eventsphere.event.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EventUpdateDTOTest {

    @Test
    void testEventUpdateDTOFields() {
        EventUpdateDTO dto = new EventUpdateDTO();
        dto.setTitle("Updated title");
        dto.setPrice(BigDecimal.ONE);
        dto.setEventDate(LocalDateTime.now().plusDays(3));

        assertEquals("Updated title", dto.getTitle());
        assertEquals(BigDecimal.ONE, dto.getPrice());
        assertNotNull(dto.getEventDate());
    }
}