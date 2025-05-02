package id.ac.ui.cs.advprog.eventsphere.topup.service;

import id.ac.ui.cs.advprog.eventsphere.topup.dto.PurchaseRequestDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TopUpResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TransactionDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.entity.User;
import id.ac.ui.cs.advprog.eventsphere.topup.model.Transaction;
import id.ac.ui.cs.advprog.eventsphere.topup.repository.TransactionRepository;
import id.ac.ui.cs.advprog.eventsphere.topup.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceImplTest {

    private UserRepository userRepository;
    private TransactionRepository transactionRepository;
    private TransactionServiceImpl transactionService;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        transactionService = new TransactionServiceImpl(userRepository, transactionRepository);

        user = User.builder()
                .id("user-1")
                .username("testuser")
                .email("test@example.com")
                .balance(100000)
                .build();
    }

    @Test
    void testProcessTicketPurchaseSuccess() {
        PurchaseRequestDTO request = PurchaseRequestDTO.builder()
                .userId("user-1")
                .eventId("event-1")
                .amount(50000)
                .description("Buy ticket")
                .build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId("txn-1");
            return tx;
        });

        TopUpResponseDTO response = transactionService.processTicketPurchase(request);

        assertEquals("user-1", response.getUserId());
        assertEquals(50000, response.getAmount());
        assertEquals(50000, response.getNewBalance());
        assertEquals("SUCCESS", response.getStatus());
        assertNotNull(response.getTransactionId());
    }

    @Test
    void testProcessTicketPurchaseFailedDueToInsufficientBalance() {
        user.setBalance(10000);

        PurchaseRequestDTO request = PurchaseRequestDTO.builder()
                .userId("user-1")
                .eventId("event-1")
                .amount(50000)
                .description("Buy ticket")
                .build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId("txn-2");
            return tx;
        });

        TopUpResponseDTO response = transactionService.processTicketPurchase(request);

        assertEquals("FAILED", response.getStatus());
        assertEquals("txn-2", response.getTransactionId());
        assertEquals(10000, response.getNewBalance());
    }

    @Test
    void testGetAllTransactions() {
        Transaction tx = sampleTransaction();
        when(transactionRepository.findAll()).thenReturn(List.of(tx));

        List<TransactionDTO> result = transactionService.getAllTransactions();

        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    void testGetUserTransactions() {
        Transaction tx = sampleTransaction();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(transactionRepository.findByUser(user)).thenReturn(List.of(tx));

        List<TransactionDTO> result = transactionService.getUserTransactions("user-1");

        assertEquals(1, result.size());
        assertEquals("user-1", result.get(0).getUserId());
    }

    @Test
    void testDeleteTransactionExists() {
        when(transactionRepository.existsById("txn-1")).thenReturn(true);
        boolean result = transactionService.deleteTransaction("txn-1");

        assertTrue(result);
        verify(transactionRepository).deleteById("txn-1");
    }

    @Test
    void testDeleteTransactionNotExists() {
        when(transactionRepository.existsById("txn-2")).thenReturn(false);
        boolean result = transactionService.deleteTransaction("txn-2");

        assertFalse(result);
    }

    @Test
    void testMarkTransactionAsFailedSuccess() {
        Transaction tx = sampleTransaction();
        when(transactionRepository.findById("txn-1")).thenReturn(Optional.of(tx));

        boolean result = transactionService.markTransactionAsFailed("txn-1");

        assertTrue(result);
        assertEquals(Transaction.TransactionStatus.FAILED, tx.getStatus());
        verify(transactionRepository).save(tx);
    }

    @Test
    void testMarkTransactionAsFailedNotFound() {
        when(transactionRepository.findById("txn-x")).thenReturn(Optional.empty());

        boolean result = transactionService.markTransactionAsFailed("txn-x");

        assertFalse(result);
    }

    @Test
    void testGetTransactionByIdSuccess() {
        Transaction tx = sampleTransaction();
        when(transactionRepository.findById("txn-1")).thenReturn(Optional.of(tx));

        TransactionDTO dto = transactionService.getTransactionById("txn-1");

        assertEquals("txn-1", dto.getId());
        assertEquals("user-1", dto.getUserId());
    }

    private Transaction sampleTransaction() {
        return Transaction.builder()
                .id("txn-1")
                .user(user)
                .amount(30000)
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TICKET_PURCHASE)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Test")
                .eventId("event-1")
                .build();
    }
}
