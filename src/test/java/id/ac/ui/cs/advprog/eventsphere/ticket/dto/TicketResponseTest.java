
package id.ac.ui.cs.advprog.eventsphere.ticket.dto;

import id.ac.ui.cs.advprog.eventsphere.ticket.model.TicketCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketResponseTest {

    @Test
    void builder_shouldCreateTicketResponseWithCorrectValues() {
        TicketResponse ticketResponse = TicketResponse.builder()
                .id(1L)
                .name("VIP")
                .price(500.0)
                .quota(100)
                .category(TicketCategory.VIP)
                .soldOut(false)
                .eventId(10L)
                .build();

        assertEquals(1L, ticketResponse.getId());
        assertEquals("VIP", ticketResponse.getName());
        assertEquals(500.0, ticketResponse.getPrice());
        assertEquals(100, ticketResponse.getQuota());
        assertEquals(TicketCategory.VIP, ticketResponse.getCategory());
        assertFalse(ticketResponse.isSoldOut());
        assertEquals(10L, ticketResponse.getEventId());
    }
}
