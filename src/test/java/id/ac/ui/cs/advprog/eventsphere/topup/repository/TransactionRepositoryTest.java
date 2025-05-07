package id.ac.ui.cs.advprog.eventsphere.topup.repository;

import id.ac.ui.cs.advprog.eventsphere.topup.entity.User;
import id.ac.ui.cs.advprog.eventsphere.topup.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .balance(100000)
                .build();
        
        // Save user to get ID
        testUser = entityManager.persist(testUser);
        
        // Create test transactions
        Transaction transaction1 = Transaction.builder()
                .user(testUser)
                .amount(50000)
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TOP_UP)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Test Top Up")
                .build();
        
        Transaction transaction2 = Transaction.builder()
                .user(testUser)
                .amount(25000)
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TICKET_PURCHASE)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Test Ticket Purchase")
                .eventId("event-123")
                .build();
        
        Transaction transaction3 = Transaction.builder()
                .user(testUser)
                .amount(15000)
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TOP_UP)
                .status(Transaction.TransactionStatus.PENDING)
                .description("Pending Top Up")
                .build();
        
        // Persist test transactions
        entityManager.persist(transaction1);
        entityManager.persist(transaction2);
        entityManager.persist(transaction3);
        
        // Flush to ensure data is saved before tests run
        entityManager.flush();
    }

    @Test
    void testFindByUser() {
        List<Transaction> transactions = transactionRepository.findByUser(testUser);
        
        assertNotNull(transactions);
        assertEquals(3, transactions.size());
    }

    @Test
    void testFindByUserAndType() {
        List<Transaction> topUps = transactionRepository.findByUserAndType(
                testUser, Transaction.TransactionType.TOP_UP);
        
        assertNotNull(topUps);
        assertEquals(2, topUps.size());
        
        List<Transaction> purchases = transactionRepository.findByUserAndType(
                testUser, Transaction.TransactionType.TICKET_PURCHASE);
        
        assertNotNull(purchases);
        assertEquals(1, purchases.size());
    }

    @Test
    void testFindByUserAndStatus() {
        List<Transaction> successTransactions = transactionRepository.findByUserAndStatus(
                testUser, Transaction.TransactionStatus.SUCCESS);
        
        assertNotNull(successTransactions);
        assertEquals(2, successTransactions.size());
        
        List<Transaction> pendingTransactions = transactionRepository.findByUserAndStatus(
                testUser, Transaction.TransactionStatus.PENDING);
        
        assertNotNull(pendingTransactions);
        assertEquals(1, pendingTransactions.size());
    }

    @Test
    void testFindByStatus() {
        List<Transaction> successTransactions = transactionRepository.findByStatus(
                Transaction.TransactionStatus.SUCCESS);
        
        assertNotNull(successTransactions);
        assertEquals(2, successTransactions.size());
        
        List<Transaction> pendingTransactions = transactionRepository.findByStatus(
                Transaction.TransactionStatus.PENDING);
        
        assertNotNull(pendingTransactions);
        assertEquals(1, pendingTransactions.size());
    }
}