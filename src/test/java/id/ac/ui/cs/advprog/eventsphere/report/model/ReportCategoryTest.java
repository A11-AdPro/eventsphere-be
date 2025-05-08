package id.ac.ui.cs.advprog.eventsphere.report.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReportCategoryTest {

    @Test
    public void testEnumValues() {
        assertEquals("PAYMENT", ReportCategory.PAYMENT.name());
        assertEquals("TICKET", ReportCategory.TICKET.name());
        assertEquals("EVENT", ReportCategory.EVENT.name());
        assertEquals("OTHER", ReportCategory.OTHER.name());
    }

    @Test
    public void testGetDisplayName() {
        assertEquals("Payment Issue", ReportCategory.PAYMENT.getDisplayName());
        assertEquals("Ticket Issue", ReportCategory.TICKET.getDisplayName());
        assertEquals("Event Issue", ReportCategory.EVENT.getDisplayName());
        assertEquals("Other Issue", ReportCategory.OTHER.getDisplayName());
    }
}