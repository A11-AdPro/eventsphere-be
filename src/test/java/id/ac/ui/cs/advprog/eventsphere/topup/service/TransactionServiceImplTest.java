package id.ac.ui.cs.advprog.eventsphere.topup.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.PurchaseRequestDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TopUpResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TransactionDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.model.Transaction;
import id.ac.ui.cs.advprog.eventsphere.topup.repository.TransactionRepository;
import id.ac.ui.cs.advprog.eventsphere.topup.util.CurrentUserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrentUserUtil currentUserUtil;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User testUser;
    private User adminUser;
    private Transaction testTransaction;
    private PurchaseRequestDTO purchaseRequest;
    private LocalDateTime testTime;

    @BeforeEach
    public void setup() {
        testTime = LocalDateTime.now();
        
        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .fullName("Test User")
                .role(Role.ATTENDEE)
                .balance(500)
                .build();
                
        adminUser = User.builder()
                .id(2L)
                .email("admin@example.com")
                .fullName("Admin User")
                .role(Role.ADMIN)
                .balance(1000)
                .build();

        testTransaction = Transaction.builder()
                .id("tx-123")
                .user(testUser)
                .amount(200)
                .timestamp(testTime)
                .type(Transaction.TransactionType.TICKET_PURCHASE)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Test Purchase")
                .eventId("event-123")
                .build();

        purchaseRequest = PurchaseRequestDTO.builder()
                .amount(200)
                .description("Test Purchase")
                .eventId("event-123")
                .build();
    }

    @Test
    public void processTicketPurchase_SufficientBalance_ReturnsSuccessResponse() {
        // Arrange
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction saved = invocation.getArgument(0);
            saved.setId("tx-123");
            return saved;
        });

        // Act
        TopUpResponseDTO response = transactionService.processTicketPurchase(purchaseRequest);

        // Assert
        assertNotNull(response);
        assertEquals("tx-123", response.getTransactionId());
        assertEquals(1L, response.getUserId());
        assertEquals(200, response.getAmount());
        assertEquals(300, response.getNewBalance()); // 500 - 200 = 300
        assertEquals("SUCCESS", response.getStatus());
        
        verify(userRepository, times(1)).save(testUser);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void processTicketPurchase_InsufficientBalance_ReturnsFailedResponse() {
        // Arrange
        // Create a purchase request with amount greater than user balance
        PurchaseRequestDTO largeRequest = PurchaseRequestDTO.builder()
                .amount(600) // User balance is 500
                .description("Large Purchase")
                .eventId("event-123")
                .build();

        when(currentUserUtil.getCurrentUserEmail()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction saved = invocation.getArgument(0);
            saved.setId("tx-123");
            return saved;
        });

        // Act
        TopUpResponseDTO response = transactionService.processTicketPurchase(largeRequest);

        // Assert
        assertNotNull(response);
        assertEquals("tx-123", response.getTransactionId());
        assertEquals(1L, response.getUserId());
        assertEquals(600, response.getAmount());
        assertEquals(500, response.getNewBalance()); // Unchanged
        assertEquals("FAILED", response.getStatus());
        
        verify(userRepository, never()).save(any(User.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void processTicketPurchase_UserNotFound_ThrowsException() {
        // Arrange
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.processTicketPurchase(purchaseRequest);
        });
        
        assertEquals("User not found", exception.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getAllTransactions_ReturnsAllTransactionsMappedToDTO() {
        // Arrange
        List<Transaction> transactions = Arrays.asList(
                testTransaction,
                Transaction.builder()
                        .id("tx-456")
                        .user(adminUser)
                        .amount(100)
                        .timestamp(testTime)
                        .type(Transaction.TransactionType.TOP_UP)
                        .status(Transaction.TransactionStatus.SUCCESS)
                        .build()
        );
        
        when(transactionRepository.findAll()).thenReturn(transactions);

        // Act
        List<TransactionDTO> result = transactionService.getAllTransactions();

        // Assert
        assertEquals(2, result.size());
        assertEquals("tx-123", result.get(0).getId());
        assertEquals("tx-456", result.get(1).getId());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals(2L, result.get(1).getUserId());
        assertEquals("Test User", result.get(0).getUsername());
        assertEquals("Admin User", result.get(1).getUsername());
    }

    @Test
    public void getCurrentUserTransactions_UserFound_ReturnsUserTransactions() {
        // Arrange
        List<Transaction> userTransactions = Arrays.asList(
                testTransaction,
                Transaction.builder()
                        .id("tx-789")
                        .user(testUser)
                        .amount(50)
                        .timestamp(testTime)
                        .type(Transaction.TransactionType.TOP_UP)
                        .status(Transaction.TransactionStatus.SUCCESS)
                        .build()
        );
        
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUser(testUser)).thenReturn(userTransactions);

        // Act
        List<TransactionDTO> result = transactionService.getCurrentUserTransactions();

        // Assert
        assertEquals(2, result.size());
        assertEquals("tx-123", result.get(0).getId());
        assertEquals("tx-789", result.get(1).getId());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals(1L, result.get(1).getUserId());
    }

    @Test
    public void getCurrentUserTransactions_UserNotFound_ThrowsException() {
        // Arrange
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.getCurrentUserTransactions();
        });
        
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getUserTransactions_UserFound_ReturnsUserTransactions() {
        // Arrange
        List<Transaction> userTransactions = Arrays.asList(
                testTransaction,
                Transaction.builder()
                        .id("tx-789")
                        .user(testUser)
                        .amount(50)
                        .timestamp(testTime)
                        .type(Transaction.TransactionType.TOP_UP)
                        .status(Transaction.TransactionStatus.SUCCESS)
                        .build()
        );
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUser(testUser)).thenReturn(userTransactions);

        // Act
        List<TransactionDTO> result = transactionService.getUserTransactions(1L);

        // Assert
        assertEquals(2, result.size());
        assertEquals("tx-123", result.get(0).getId());
        assertEquals("tx-789", result.get(1).getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getUserTransactions_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.getUserTransactions(999L);
        });
        
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteTransaction_TransactionExists_ReturnsTrue() {
        // Arrange
        when(transactionRepository.existsById("tx-123")).thenReturn(true);
        doNothing().when(transactionRepository).deleteById("tx-123");

        // Act
        boolean result = transactionService.deleteTransaction("tx-123");

        // Assert
        assertTrue(result);
        verify(transactionRepository).deleteById("tx-123");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteTransaction_TransactionDoesNotExist_ReturnsFalse() {
        // Arrange
        when(transactionRepository.existsById("nonexistent")).thenReturn(false);

        // Act
        boolean result = transactionService.deleteTransaction("nonexistent");

        // Assert
        assertFalse(result);
        verify(transactionRepository, never()).deleteById(anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void markTransactionAsFailed_TransactionExists_UpdatesStatusAndReturnsTrue() {
        // Arrange
        when(transactionRepository.findById("tx-123")).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(testTransaction)).thenReturn(testTransaction);

        // Act
        boolean result = transactionService.markTransactionAsFailed("tx-123");

        // Assert
        assertTrue(result);
        assertEquals(Transaction.TransactionStatus.FAILED, testTransaction.getStatus());
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void markTransactionAsFailed_TransactionDoesNotExist_ReturnsFalse() {
        // Arrange
        when(transactionRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act
        boolean result = transactionService.markTransactionAsFailed("nonexistent");

        // Assert
        assertFalse(result);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    public void getTransactionById_TransactionExistsAndUserIsOwner_ReturnsTransactionDTO() {
        // Arrange
        when(transactionRepository.findById("tx-123")).thenReturn(Optional.of(testTransaction));
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("user@example.com");

        // Act
        TransactionDTO result = transactionService.getTransactionById("tx-123");

        // Assert
        assertNotNull(result);
        assertEquals("tx-123", result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals("Test User", result.getUsername());
        assertEquals(200, result.getAmount());
        assertEquals("TICKET_PURCHASE", result.getType());
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("Test Purchase", result.getDescription());
        assertEquals("event-123", result.getEventId());
    }

    @Test
    public void getTransactionById_TransactionExistsButUserIsNotOwner_ThrowsException() {
        // Arrange
        when(transactionRepository.findById("tx-123")).thenReturn(Optional.of(testTransaction));
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("other@example.com");

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.getTransactionById("tx-123");
        });
        
        assertEquals("Access denied: You can only view your own transactions", exception.getMessage());
    }

    @Test
    public void getTransactionById_TransactionDoesNotExist_ThrowsException() {
        // Arrange
        when(transactionRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.getTransactionById("nonexistent");
        });
        
        assertEquals("Transaction not found", exception.getMessage());
    }
}