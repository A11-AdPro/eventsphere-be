package id.ac.ui.cs.advprog.eventsphere.ticket.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketCategoryTest {

    @Test
    void testEnumValues() {
        TicketCategory[] values = TicketCategory.values();
        assertEquals(2, values.length);
        assertArrayEquals(new TicketCategory[]{TicketCategory.VIP, TicketCategory.REGULAR}, values);
    }

    @Test
    void testValueOfVIP() {
        assertEquals(TicketCategory.VIP, TicketCategory.valueOf("VIP"));
    }

    @Test
    void testValueOfREGULAR() {
        assertEquals(TicketCategory.REGULAR, TicketCategory.valueOf("REGULAR"));
    }

    @Test
    void testValueOfInvalidThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> TicketCategory.valueOf("INVALID"));
    }
}
