package id.ac.ui.cs.advprog.eventsphere.event.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserSummaryDTOTest {

    @Test
    void userSummaryDTO_equalsAndHashCode() {
        UserSummaryDTO dto1 = new UserSummaryDTO();
        dto1.setId(1L);
        dto1.setUsername("johndoe");
        dto1.setName("John Doe");

        UserSummaryDTO dto2 = new UserSummaryDTO();
        dto2.setId(1L);
        dto2.setUsername("johndoe");
        dto2.setName("John Doe");

        UserSummaryDTO dto3 = new UserSummaryDTO();
        dto3.setId(2L);
        dto3.setUsername("janedoe");
        dto3.setName("Jane Doe");

        // Test equals
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);

        // Test hashCode
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    void userSummaryDTO_toString() {
        UserSummaryDTO dto = new UserSummaryDTO();
        dto.setId(1L);
        dto.setUsername("johndoe");
        dto.setName("John Doe");

        String toString = dto.toString();
        
        // Verify the toString contains key information
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("username=johndoe"));
        assertTrue(toString.contains("name=John Doe"));
    }

    @Test
    void userSummaryDTO_gettersAndSetters() {
        UserSummaryDTO dto = new UserSummaryDTO();
        dto.setId(1L);
        dto.setUsername("johndoe");
        dto.setName("John Doe");

        assertEquals(1L, dto.getId());
        assertEquals("johndoe", dto.getUsername());
        assertEquals("John Doe", dto.getName());
    }

}