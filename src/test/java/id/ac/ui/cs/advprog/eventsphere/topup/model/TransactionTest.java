package id.ac.ui.cs.advprog.eventsphere.topup.model;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class TransactionTest {
    
    private User testUser;
    private LocalDateTime testTimestamp;
    
    @BeforeEach
    public void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();
        testTimestamp = LocalDateTime.now();
    }
    
    @Test
    @DisplayName("Should create Transaction with all fields")
    public void testCreateTransaction() {
        Transaction transaction = Transaction.builder()
                .id("transaction-123")
                .user(testUser)
                .amount(50000)
                .timestamp(testTimestamp)
                .type(Transaction.TransactionType.TOP_UP)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Test transaction")
                .eventId("event-123")
                .build();
        
        assertEquals("transaction-123", transaction.getId());
        assertEquals(testUser, transaction.getUser());
        assertEquals(50000, transaction.getAmount());
        assertEquals(testTimestamp, transaction.getTimestamp());
        assertEquals(Transaction.TransactionType.TOP_UP, transaction.getType());
        assertEquals(Transaction.TransactionStatus.SUCCESS, transaction.getStatus());
        assertEquals("Test transaction", transaction.getDescription());
        assertEquals("event-123", transaction.getEventId());
    }
    
    @Test
    @DisplayName("Should create Transaction with no-args constructor and setters")
    public void testCreateTransactionNoArgsConstructor() {
        Transaction transaction = new Transaction();
        transaction.setId("transaction-123");
        transaction.setUser(testUser);
        transaction.setAmount(50000);
        transaction.setTimestamp(testTimestamp);
        transaction.setType(Transaction.TransactionType.TICKET_PURCHASE);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setDescription("Test transaction");
        transaction.setEventId("event-123");
        
        assertEquals("transaction-123", transaction.getId());
        assertEquals(testUser, transaction.getUser());
        assertEquals(50000, transaction.getAmount());
        assertEquals(testTimestamp, transaction.getTimestamp());
        assertEquals(Transaction.TransactionType.TICKET_PURCHASE, transaction.getType());
        assertEquals(Transaction.TransactionStatus.PENDING, transaction.getStatus());
        assertEquals("Test transaction", transaction.getDescription());
        assertEquals("event-123", transaction.getEventId());
    }
    
    @Test
    @DisplayName("Should test equals, hashCode and toString methods")
    public void testEqualsHashCodeToString() {
        Transaction transaction1 = Transaction.builder()
                .id("transaction-123")
                .user(testUser)
                .amount(50000)
                .timestamp(testTimestamp)
                .type(Transaction.TransactionType.TOP_UP)
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();
        
        Transaction transaction2 = Transaction.builder()
                .id("transaction-123")
                .user(testUser)
                .amount(50000)
                .timestamp(testTimestamp)
                .type(Transaction.TransactionType.TOP_UP)
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();
        
        assertEquals(transaction1, transaction2);
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
        
        String toStringResult = transaction1.toString();
        assertTrue(toStringResult.contains("transaction-123"));
        assertTrue(toStringResult.contains("50000"));
        assertTrue(toStringResult.contains("TOP_UP"));
        assertTrue(toStringResult.contains("SUCCESS"));
    }
}