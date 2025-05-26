package id.ac.ui.cs.advprog.eventsphere.ticket.dto;

import id.ac.ui.cs.advprog.eventsphere.ticket.model.TicketCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketRequestTest {

    @Test
    void testGetterSetterAndConstructor() {
        // test constructor all args
        TicketRequest request = new TicketRequest("VIP", 500.0, 100, TicketCategory.VIP, 1L);

        assertEquals("VIP", request.getName());
        assertEquals(500.0, request.getPrice());
        assertEquals(100, request.getQuota());
        assertEquals(TicketCategory.VIP, request.getCategory());
        assertEquals(1L, request.getEventId());

        // test setters
        request.setName("Regular");
        request.setPrice(250.0);
        request.setQuota(50);
        request.setCategory(TicketCategory.REGULAR);
        request.setEventId(2L);

        assertEquals("Regular", request.getName());
        assertEquals(250.0, request.getPrice());
        assertEquals(50, request.getQuota());
        assertEquals(TicketCategory.REGULAR, request.getCategory());
        assertEquals(2L, request.getEventId());
    }
}
