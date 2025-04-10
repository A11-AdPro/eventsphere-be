package id.ac.ui.cs.advprog.eventsphere.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class TopUpTransactionTest {

    @Test
    public void testDefaultConstructor() {
        TopUpTransaction transaction = new TopUpTransaction();
        assertNotNull(transaction.getTimestamp());
        assertEquals("PENDING", transaction.getStatus());
    }

    @Test
    public void testParameterizedConstructor() {
        Long attendeeId = 1L;
        BigDecimal amount = new BigDecimal("100000");

        TopUpTransaction transaction = new TopUpTransaction(attendeeId, amount);

        assertEquals(attendeeId, transaction.getAttendeeId());
        assertEquals(amount, transaction.getAmount());
        assertEquals("PENDING", transaction.getStatus());
        assertNotNull(transaction.getTimestamp());
    }

    @Test
    public void testSettersAndGetters() {
        TopUpTransaction transaction = new TopUpTransaction();

        Long id = 1L;
        Long attendeeId = 2L;
        BigDecimal amount = new BigDecimal("200000");
        LocalDateTime timestamp = LocalDateTime.now().minusHours(1);
        String status = "SUCCESS";

        transaction.setId(id);
        transaction.setAttendeeId(attendeeId);
        transaction.setAmount(amount);
        transaction.setTimestamp(timestamp);
        transaction.setStatus(status);

        assertEquals(id, transaction.getId());
        assertEquals(attendeeId, transaction.getAttendeeId());
        assertEquals(amount, transaction.getAmount());
        assertEquals(timestamp, transaction.getTimestamp());
        assertEquals(status, transaction.getStatus());
    }
}