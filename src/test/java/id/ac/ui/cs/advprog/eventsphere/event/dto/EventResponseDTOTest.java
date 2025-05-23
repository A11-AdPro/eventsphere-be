package id.ac.ui.cs.advprog.eventsphere.event.dto;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EventResponseDTOTest {

    @Test
    void testEventResponseDTOFields() {
        EventResponseDTO dto = new EventResponseDTO();
        dto.setId(10L);
        dto.setTitle("Response Title");
        dto.setPrice(BigDecimal.valueOf(123.45));
        dto.setEventDate(LocalDateTime.now().plusDays(2));
        dto.setOrganizerId(99L);
        dto.setOrganizerName("Organizer Name");
        dto.setOrganizerRole(Role.ORGANIZER);

        assertEquals(10L, dto.getId());
        assertEquals("Response Title", dto.getTitle());
        assertEquals(BigDecimal.valueOf(123.45), dto.getPrice());
        assertEquals(99L, dto.getOrganizerId());
        assertNotNull(dto.getEventDate());
        assertEquals("Organizer Name", dto.getOrganizerName());
        assertEquals(Role.ORGANIZER, dto.getOrganizerRole());
    }
}