package id.ac.ui.cs.advprog.eventsphere.event.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EventCreateDTOTest {

    @Test
    void testEventCreateDTOFields() {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setTitle("DTO Title");
        dto.setDescription("DTO Desc");
        dto.setLocation("DTO Loc");
        dto.setPrice(BigDecimal.TEN);
        dto.setEventDate(LocalDateTime.now().plusDays(5));

        assertEquals("DTO Title", dto.getTitle());
        assertEquals("DTO Desc", dto.getDescription());
        assertEquals("DTO Loc", dto.getLocation());
        assertEquals(BigDecimal.TEN, dto.getPrice());
        assertNotNull(dto.getEventDate());
    }
}