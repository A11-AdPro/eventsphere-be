package id.ac.ui.cs.advprog.eventsphere.topup.repository;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.topup.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        transactionRepository.deleteAll();
        userRepository.deleteAll();

        // Create a new test user with unique email
        testUser = User.builder()
                .email("test-" + UUID.randomUUID() + "@example.com")
                .password("password")
                .role(Role.ATTENDEE)
                .fullName("Test User")
                .balance(1000)
                .build();

        testUser = userRepository.save(testUser);
    }

    @Test
    void testFindByUser() {
        Transaction transaction = Transaction.builder()
                .user(testUser)
                .amount(100)
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TOP_UP)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Test transaction")
                .build();

        transactionRepository.save(transaction);

        List<Transaction> found = transactionRepository.findByUser(testUser);
        
        assertFalse(found.isEmpty());
        assertEquals(1, found.size());
        assertEquals(testUser.getId(), found.get(0).getUser().getId());
    }

    @Test
    void testFindByUserAndType() {
        Transaction topUpTransaction = Transaction.builder()
                .user(testUser)
                .amount(100)
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TOP_UP)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Top-up transaction")
                .build();

        Transaction purchaseTransaction = Transaction.builder()
                .user(testUser)
                .amount(50)
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TICKET_PURCHASE)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Purchase transaction")
                .build();

        transactionRepository.save(topUpTransaction);
        transactionRepository.save(purchaseTransaction);

        List<Transaction> topUpTransactions = transactionRepository.findByUserAndType(
                testUser, Transaction.TransactionType.TOP_UP);
        
        assertFalse(topUpTransactions.isEmpty());
        assertEquals(1, topUpTransactions.size());
        assertEquals(Transaction.TransactionType.TOP_UP, topUpTransactions.get(0).getType());
    }

    @Test
    void testFindByUserAndStatus() {
        Transaction successTransaction = Transaction.builder()
                .user(testUser)
                .amount(100)
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TOP_UP)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Success transaction")
                .build();

        Transaction failedTransaction = Transaction.builder()
                .user(testUser)
                .amount(50)
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TOP_UP)
                .status(Transaction.TransactionStatus.FAILED)
                .description("Failed transaction")
                .build();

        transactionRepository.save(successTransaction);
        transactionRepository.save(failedTransaction);

        List<Transaction> successTransactions = transactionRepository.findByUserAndStatus(
                testUser, Transaction.TransactionStatus.SUCCESS);
        
        assertFalse(successTransactions.isEmpty());
        assertEquals(1, successTransactions.size());
        assertEquals(Transaction.TransactionStatus.SUCCESS, successTransactions.get(0).getStatus());
    }

    @Test
    void testFindByStatus() {
        User anotherUser = User.builder()
                .email("another-" + UUID.randomUUID() + "@example.com")
                .password("password")
                .role(Role.ATTENDEE)
                .fullName("Another User")
                .balance(500)
                .build();
                
        anotherUser = userRepository.save(anotherUser);

        Transaction successTransaction1 = Transaction.builder()
                .user(testUser)
                .amount(100)
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TOP_UP)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Success transaction 1")
                .build();

        Transaction successTransaction2 = Transaction.builder()
                .user(anotherUser)
                .amount(200)
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TOP_UP)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Success transaction 2")
                .build();

        Transaction failedTransaction = Transaction.builder()
                .user(testUser)
                .amount(50)
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TOP_UP)
                .status(Transaction.TransactionStatus.FAILED)
                .description("Failed transaction")
                .build();

        transactionRepository.save(successTransaction1);
        transactionRepository.save(successTransaction2);
        transactionRepository.save(failedTransaction);

        List<Transaction> successTransactions = transactionRepository.findByStatus(
                Transaction.TransactionStatus.SUCCESS);
        
        assertFalse(successTransactions.isEmpty());
        assertEquals(2, successTransactions.size());
        assertTrue(successTransactions.stream()
                .allMatch(t -> t.getStatus() == Transaction.TransactionStatus.SUCCESS));
    }
}