package id.ac.ui.cs.advprog.eventsphere.ticket.model;

import id.ac.ui.cs.advprog.eventsphere.event.model.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketTest {

    private Event event;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(1L);

        ticket = new Ticket(1L, "Regular Ticket", 100.0, 10, TicketCategory.REGULAR, event);
    }

    @Test
    void testConstructorShouldInitializeCorrectly() {
        assertEquals(1L, ticket.getId());
        assertEquals("Regular Ticket", ticket.getName());
        assertEquals(100.0, ticket.getPrice());
        assertEquals(10, ticket.getQuota());
        assertEquals(TicketCategory.REGULAR, ticket.getCategory());
        assertEquals(event, ticket.getEvent());
        assertEquals(0, ticket.getSold());
        assertFalse(ticket.isDeleted());
    }

    @Test
    void testIsSoldOutReturnsFalseInitially() {
        assertFalse(ticket.isSoldOut());
    }

    @Test
    void testIsSoldOutReturnsTrueWhenQuotaIsZero() {
        ticket.setSold(10);
        ticket.setQuota(10);
        assertTrue(ticket.isSoldOut());
    }

    @Test
    void testPurchaseIncrementsSoldAndDecrementsQuota() {
        ticket.purchase();
        assertEquals(1, ticket.getSold());
        assertEquals(9, ticket.getQuota());
    }

    @Test
    void testPurchaseThrowsExceptionWhenSoldOut() {
        ticket.setSold(10);
        ticket.setQuota(10);
        assertThrows(IllegalStateException.class, ticket::purchase);
    }

    @Test
    void testUpdateDetailsShouldChangePriceAndQuota() {
        ticket.updateDetails(200.0, 50);
        assertEquals(200.0, ticket.getPrice());
        assertEquals(50, ticket.getQuota());
    }

    @Test
    void testMarkDeletedShouldSetDeletedToTrue() {
        ticket.markDeleted();
        assertTrue(ticket.isDeleted());
    }

    @Test
    void testGetEventIdShouldReturnEventId() {
        assertEquals(1L, ticket.getEventId());
    }

    @Test
    void testGetEventIdReturnsNullIfEventIsNull() {
        ticket.setEvent(null);
        assertNull(ticket.getEventId());
    }
}
