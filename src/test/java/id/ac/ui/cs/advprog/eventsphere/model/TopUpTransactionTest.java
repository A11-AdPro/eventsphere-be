package id.ac.ui.cs.advprog.eventsphere.topup.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TopUpTransactionTest {

    @Test
    public void testCreateTopUpTransaction() {
        UUID userId = UUID.randomUUID();
        int nominal = 100000;
        LocalDateTime timestamp = LocalDateTime.now();
        String status = "SUCCESS";

        TopUpTransaction transaction = new TopUpTransaction(userId, nominal, timestamp, status);

        assertEquals(userId, transaction.getUserId());
        assertEquals(nominal, transaction.getNominal());
        assertEquals(timestamp, transaction.getTimestamp());
        assertEquals(status, transaction.getStatus());
        assertNull(transaction.getId());
    }

    @Test
    public void testSettersAndGetters() {
        TopUpTransaction transaction = new TopUpTransaction();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        int nominal = 50000;
        LocalDateTime timestamp = LocalDateTime.now();
        String status = "FAILED";

        transaction.setId(id);
        transaction.setUserId(userId);
        transaction.setNominal(nominal);
        transaction.setTimestamp(timestamp);
        transaction.setStatus(status);

        assertEquals(id, transaction.getId());
        assertEquals(userId, transaction.getUserId());
        assertEquals(nominal, transaction.getNominal());
        assertEquals(timestamp, transaction.getTimestamp());
        assertEquals(status, transaction.getStatus());
    }
}
