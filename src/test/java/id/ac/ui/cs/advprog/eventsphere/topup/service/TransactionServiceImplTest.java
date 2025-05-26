package id.ac.ui.cs.advprog.eventsphere.topup.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketResponse;
import id.ac.ui.cs.advprog.eventsphere.ticket.model.TicketCategory;
import id.ac.ui.cs.advprog.eventsphere.ticket.service.TicketService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrentUserUtil currentUserUtil;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User testUser;
    private User adminUser;
    private Transaction testTransaction;
    private TicketResponse testTicketResponse;
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
                .eventId("1")
                .build();

        testTicketResponse = new TicketResponse.Builder()
                .id(1L)
                .name("Test Ticket")
                .price(200)
                .quota(10)
                .category(TicketCategory.VIP)
                .eventId(1L)
                .soldOut(false)
                .build();
    }

    @Test
    public void processTicketPurchaseById_SufficientBalance_ReturnsSuccessResponse() {
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(ticketService.getTicketById(1L)).thenReturn(testTicketResponse);
        when(ticketService.purchaseTicket(1L)).thenReturn(testTicketResponse);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction saved = invocation.getArgument(0);
            saved.setId("tx-123");
            return saved;
        });

        TopUpResponseDTO response = transactionService.processTicketPurchaseById(1L);

        assertNotNull(response);
        assertEquals("tx-123", response.getTransactionId());
        assertEquals(1L, response.getUserId());
        assertEquals(200, response.getAmount());
        assertEquals(300, response.getNewBalance());
        assertEquals("SUCCESS", response.getStatus());
        
        verify(userRepository, times(1)).save(testUser);
        verify(ticketService, times(1)).getTicketById(1L);
        verify(ticketService, times(1)).purchaseTicket(1L);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void processTicketPurchaseById_InsufficientBalance_ThrowsException() {
        TicketResponse expensiveTicket = new TicketResponse.Builder()
                .id(1L)
                .name("Expensive Ticket")
                .price(600)
                .quota(10)
                .category(TicketCategory.VIP)
                .eventId(1L)
                .soldOut(false)
                .build();

        when(currentUserUtil.getCurrentUserEmail()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(ticketService.getTicketById(1L)).thenReturn(expensiveTicket);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction saved = invocation.getArgument(0);
            saved.setId("tx-123");
            return saved;
        });

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.processTicketPurchaseById(1L);
        });
        
        assertEquals("Insufficient balance", exception.getMessage());
        
        verify(ticketService, times(1)).getTicketById(1L);
        verify(ticketService, never()).purchaseTicket(anyLong());
        verify(userRepository, never()).save(any(User.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void processTicketPurchaseById_SoldOutTicket_ThrowsException() {
        TicketResponse soldOutTicket = new TicketResponse.Builder()
                .id(1L)
                .name("Sold Out Ticket")
                .price(200)
                .quota(0)
                .category(TicketCategory.VIP)
                .eventId(1L)
                .soldOut(true)
                .build();

        when(currentUserUtil.getCurrentUserEmail()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(ticketService.getTicketById(1L)).thenReturn(soldOutTicket);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.processTicketPurchaseById(1L);
        });
        
        assertEquals("Ticket is sold out", exception.getMessage());
        
        verify(ticketService, times(1)).getTicketById(1L);
        verify(ticketService, never()).purchaseTicket(anyLong());
        verify(userRepository, never()).save(any(User.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    public void processTicketPurchaseById_UserNotFound_ThrowsException() {
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.processTicketPurchaseById(1L);
        });
        
        assertEquals("User not found", exception.getMessage());
        verify(ticketService, never()).getTicketById(anyLong());
        verify(ticketService, never()).purchaseTicket(anyLong());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getAllTransactions_ReturnsAllTransactionsMappedToDTO() {
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

        List<TransactionDTO> result = transactionService.getAllTransactions();

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

        List<TransactionDTO> result = transactionService.getCurrentUserTransactions();

        assertEquals(2, result.size());
        assertEquals("tx-123", result.get(0).getId());
        assertEquals("tx-789", result.get(1).getId());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals(1L, result.get(1).getUserId());
    }

    @Test
    public void getCurrentUserTransactions_UserNotFound_ThrowsException() {
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.getCurrentUserTransactions();
        });
        
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getUserTransactions_UserFound_ReturnsUserTransactions() {
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

        List<TransactionDTO> result = transactionService.getUserTransactions(1L);

        assertEquals(2, result.size());
        assertEquals("tx-123", result.get(0).getId());
        assertEquals("tx-789", result.get(1).getId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getUserTransactions_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.getUserTransactions(999L);
        });
        
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteTransaction_TransactionExists_ReturnsTrue() {
        when(transactionRepository.existsById("tx-123")).thenReturn(true);
        doNothing().when(transactionRepository).deleteById("tx-123");

        boolean result = transactionService.deleteTransaction("tx-123");

        assertTrue(result);
        verify(transactionRepository).deleteById("tx-123");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteTransaction_TransactionDoesNotExist_ReturnsFalse() {
        when(transactionRepository.existsById("nonexistent")).thenReturn(false);

        boolean result = transactionService.deleteTransaction("nonexistent");

        assertFalse(result);
        verify(transactionRepository, never()).deleteById(anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void markTransactionAsFailed_TransactionExists_UpdatesStatusAndReturnsTrue() {
        when(transactionRepository.findById("tx-123")).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(testTransaction)).thenReturn(testTransaction);

        boolean result = transactionService.markTransactionAsFailed("tx-123");

        assertTrue(result);
        assertEquals(Transaction.TransactionStatus.FAILED, testTransaction.getStatus());
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void markTransactionAsFailed_TransactionDoesNotExist_ReturnsFalse() {
        when(transactionRepository.findById("nonexistent")).thenReturn(Optional.empty());

        boolean result = transactionService.markTransactionAsFailed("nonexistent");

        assertFalse(result);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    public void getTransactionById_TransactionExistsAndUserIsOwner_ReturnsTransactionDTO() {
        when(transactionRepository.findById("tx-123")).thenReturn(Optional.of(testTransaction));
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("user@example.com");

        TransactionDTO result = transactionService.getTransactionById("tx-123");

        assertNotNull(result);
        assertEquals("tx-123", result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals("Test User", result.getUsername());
        assertEquals(200, result.getAmount());
        assertEquals("TICKET_PURCHASE", result.getType());
        assertEquals("SUCCESS", result.getStatus());
        assertEquals("Test Purchase", result.getDescription());
        assertEquals("1", result.getEventId());
    }

    @Test
    public void getTransactionById_TransactionExistsButUserIsNotOwner_ThrowsException() {
        when(transactionRepository.findById("tx-123")).thenReturn(Optional.of(testTransaction));
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("other@example.com");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.getTransactionById("tx-123");
        });
        
        assertEquals("Access denied: You can only view your own transactions", exception.getMessage());
    }

    @Test
    public void getTransactionById_TransactionDoesNotExist_ThrowsException() {
        when(transactionRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            transactionService.getTransactionById("nonexistent");
        });
        
        assertEquals("Transaction not found", exception.getMessage());
    }
}