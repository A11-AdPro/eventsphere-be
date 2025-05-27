package id.ac.ui.cs.advprog.eventsphere.topup.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.ticket.dto.TicketResponse;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

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
    private User otherUser;
    private TicketResponse testTicket;
    private Transaction testTransaction;
    private final String TEST_EMAIL = "test@example.com";
    private final String ADMIN_EMAIL = "admin@example.com";
    private final String OTHER_EMAIL = "other@example.com";
    private final Long TICKET_ID = 1L;
    private final Long USER_ID = 1L;
    private final Long ADMIN_ID = 2L;
    private final Long OTHER_USER_ID = 3L;
    private final String TRANSACTION_ID = "trans-123";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(USER_ID)
                .email(TEST_EMAIL)
                .fullName("Test User")
                .role(Role.ATTENDEE)
                .balance(100000)
                .build();

        adminUser = User.builder()
                .id(ADMIN_ID)
                .email(ADMIN_EMAIL)
                .fullName("Admin User")
                .role(Role.ADMIN)
                .balance(200000)
                .build();

        otherUser = User.builder()
                .id(OTHER_USER_ID)
                .email(OTHER_EMAIL)
                .fullName("Other User")
                .role(Role.ATTENDEE)
                .balance(50000)
                .build();

        testTicket = new TicketResponse.Builder()
                .id(TICKET_ID)
                .name("Test Event Ticket")
                .price(50000.0)
                .quota(10)
                .soldOut(false)
                .eventId(1L)
                .build();

        testTransaction = Transaction.builder()
                .id(TRANSACTION_ID)
                .user(testUser)
                .amount(50000)
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TICKET_PURCHASE)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Purchase ticket: Test Event Ticket")
                .eventId("1")
                .build();
    }

    @Test
    void processTicketPurchaseById_SuccessfulPurchase_ReturnsTopUpResponseDTO() {
        when(currentUserUtil.getCurrentUserEmail()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(ticketService.getTicketById(TICKET_ID)).thenReturn(testTicket);
        when(ticketService.purchaseTicket(TICKET_ID)).thenReturn(testTicket);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TopUpResponseDTO result = transactionService.processTicketPurchaseById(TICKET_ID);

        assertNotNull(result);
        assertEquals(TRANSACTION_ID, result.getTransactionId());
        assertEquals(USER_ID, result.getUserId());
        assertEquals(50000, result.getAmount());
        assertEquals(50000, result.getNewBalance()); 
        assertEquals("SUCCESS", result.getStatus());

        verify(currentUserUtil).getCurrentUserEmail();
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(ticketService).getTicketById(TICKET_ID);
        verify(ticketService).purchaseTicket(TICKET_ID);
        verify(userRepository).save(testUser);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void processTicketPurchaseById_InvalidTicketId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.processTicketPurchaseById(null));
        assertEquals("Invalid ticket ID", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.processTicketPurchaseById(0L));
        assertEquals("Invalid ticket ID", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.processTicketPurchaseById(-1L));
        assertEquals("Invalid ticket ID", exception.getMessage());
    }

    @Test
    void processTicketPurchaseById_UserNotFound_ThrowsException() {
        when(currentUserUtil.getCurrentUserEmail()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transactionService.processTicketPurchaseById(TICKET_ID));
        assertEquals("User not found", exception.getMessage());

        verify(currentUserUtil).getCurrentUserEmail();
        verify(userRepository).findByEmail(TEST_EMAIL);
        verifyNoInteractions(ticketService);
    }

    @Test
    void processTicketPurchaseById_TicketNotFound_ThrowsException() {
        when(currentUserUtil.getCurrentUserEmail()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(ticketService.getTicketById(TICKET_ID)).thenThrow(new RuntimeException("Ticket not found"));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transactionService.processTicketPurchaseById(TICKET_ID));
        assertTrue(exception.getMessage().contains("Ticket not found"));

        verify(currentUserUtil).getCurrentUserEmail();
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(ticketService).getTicketById(TICKET_ID);
        verify(transactionRepository).save(any(Transaction.class)); 
        verifyNoMoreInteractions(ticketService);
    }

    @Test
    void processTicketPurchaseById_SoldOutTicket_ThrowsException() {
        TicketResponse soldOutTicket = new TicketResponse.Builder()
                .id(TICKET_ID)
                .name("Sold Out Ticket")
                .price(50000.0)
                .quota(0)
                .soldOut(true)
                .eventId(1L)
                .build();

        when(currentUserUtil.getCurrentUserEmail()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(ticketService.getTicketById(TICKET_ID)).thenReturn(soldOutTicket);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transactionService.processTicketPurchaseById(TICKET_ID));
        assertEquals("Ticket is sold out", exception.getMessage());

        verify(currentUserUtil).getCurrentUserEmail();
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(ticketService).getTicketById(TICKET_ID);
        verify(transactionRepository).save(any(Transaction.class)); 
        verify(ticketService, never()).purchaseTicket(any()); 
    }

    @Test
    void processTicketPurchaseById_InsufficientBalance_ThrowsException() {
        testUser.setBalance(10000); 
        when(currentUserUtil.getCurrentUserEmail()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(ticketService.getTicketById(TICKET_ID)).thenReturn(testTicket);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transactionService.processTicketPurchaseById(TICKET_ID));
        assertTrue(exception.getMessage().contains("Insufficient balance"));
        assertTrue(exception.getMessage().contains("Required: 50000"));
        assertTrue(exception.getMessage().contains("Available: 10000"));

        verify(currentUserUtil).getCurrentUserEmail();
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(ticketService).getTicketById(TICKET_ID);
        verify(transactionRepository).save(any(Transaction.class));
        verify(ticketService, never()).purchaseTicket(any()); 
    }

    @Test
    void processTicketPurchaseById_PurchaseTicketFails_ThrowsException() {
        when(currentUserUtil.getCurrentUserEmail()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(ticketService.getTicketById(TICKET_ID)).thenReturn(testTicket);
        when(ticketService.purchaseTicket(TICKET_ID)).thenThrow(new RuntimeException("Purchase failed"));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transactionService.processTicketPurchaseById(TICKET_ID));
        assertTrue(exception.getMessage().contains("Failed to purchase ticket"));

        verify(currentUserUtil).getCurrentUserEmail();
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(ticketService).getTicketById(TICKET_ID);
        verify(ticketService).purchaseTicket(TICKET_ID);
        verify(transactionRepository).save(any(Transaction.class)); 
        verify(userRepository, never()).save(testUser); 
    }

    @Test
    void processTicketPurchaseById_UnexpectedErrorDuringProcessing_ThrowsException() {
        when(currentUserUtil.getCurrentUserEmail()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(ticketService.getTicketById(TICKET_ID)).thenReturn(testTicket);
        when(ticketService.purchaseTicket(TICKET_ID)).thenReturn(testTicket);
        when(userRepository.save(testUser)).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transactionService.processTicketPurchaseById(TICKET_ID));
        assertEquals("Database error", exception.getMessage());

        verify(currentUserUtil).getCurrentUserEmail();
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(ticketService).getTicketById(TICKET_ID);
        verify(ticketService).purchaseTicket(TICKET_ID);
        verify(userRepository).save(testUser);
    }

    @Test
    void processTicketPurchaseByIdAsync_SuccessfulPurchase_ReturnsCompletableFuture() throws Exception {
        when(currentUserUtil.getCurrentUserEmail()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(ticketService.getTicketById(TICKET_ID)).thenReturn(testTicket);
        when(ticketService.purchaseTicket(TICKET_ID)).thenReturn(testTicket);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        CompletableFuture<TopUpResponseDTO> result = transactionService.processTicketPurchaseByIdAsync(TICKET_ID);

        assertNotNull(result);
        TopUpResponseDTO response = result.get();
        assertNotNull(response);
        assertEquals(TRANSACTION_ID, response.getTransactionId());
    }

    @Test
    void processTicketPurchaseByIdAsync_PurchaseFails_ReturnsFailedFuture() {
        when(currentUserUtil.getCurrentUserEmail()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        CompletableFuture<TopUpResponseDTO> result = transactionService.processTicketPurchaseByIdAsync(TICKET_ID);

        assertNotNull(result);
        assertTrue(result.isCompletedExceptionally());
    }

    @Test
    void getAllTransactions_ReturnsAllTransactions() {
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(transactionRepository.findAll()).thenReturn(transactions);

        List<TransactionDTO> result = transactionService.getAllTransactions();

        assertNotNull(result);
        assertEquals(1, result.size());
        TransactionDTO dto = result.get(0);
        assertEquals(TRANSACTION_ID, dto.getId());
        assertEquals(USER_ID, dto.getUserId());
        assertEquals("Test User", dto.getUsername());
        assertEquals(50000, dto.getAmount());
        assertEquals("TICKET_PURCHASE", dto.getType());
        assertEquals("SUCCESS", dto.getStatus());

        verify(transactionRepository).findAll();
    }

    @Test
    void getCurrentUserTransactions_ReturnsUserTransactions() {
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(currentUserUtil.getCurrentUserEmail()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUser(testUser)).thenReturn(transactions);

        List<TransactionDTO> result = transactionService.getCurrentUserTransactions();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TRANSACTION_ID, result.get(0).getId());

        verify(currentUserUtil).getCurrentUserEmail();
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(transactionRepository).findByUser(testUser);
    }

    @Test
    void getCurrentUserTransactions_UserNotFound_ThrowsException() {
        when(currentUserUtil.getCurrentUserEmail()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transactionService.getCurrentUserTransactions());
        assertEquals("User not found", exception.getMessage());

        verify(currentUserUtil).getCurrentUserEmail();
        verify(userRepository).findByEmail(TEST_EMAIL);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void getUserTransactions_ValidUserId_ReturnsUserTransactions() {
        List<Transaction> transactions = Arrays.asList(testTransaction);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUser(testUser)).thenReturn(transactions);

        List<TransactionDTO> result = transactionService.getUserTransactions(USER_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TRANSACTION_ID, result.get(0).getId());

        verify(userRepository).findById(USER_ID);
        verify(transactionRepository).findByUser(testUser);
    }

    @Test
    void getUserTransactions_UserNotFound_ThrowsException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transactionService.getUserTransactions(USER_ID));
        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findById(USER_ID);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void deleteTransaction_TransactionExists_ReturnsTrue() {
        when(transactionRepository.existsById(TRANSACTION_ID)).thenReturn(true);

        boolean result = transactionService.deleteTransaction(TRANSACTION_ID);

        assertTrue(result);
        verify(transactionRepository).existsById(TRANSACTION_ID);
        verify(transactionRepository).deleteById(TRANSACTION_ID);
    }

    @Test
    void deleteTransaction_TransactionNotExists_ReturnsFalse() {
        when(transactionRepository.existsById(TRANSACTION_ID)).thenReturn(false);

        boolean result = transactionService.deleteTransaction(TRANSACTION_ID);

        assertFalse(result);
        verify(transactionRepository).existsById(TRANSACTION_ID);
        verify(transactionRepository, never()).deleteById(any());
    }

    @Test
    void markTransactionAsFailed_TransactionExists_ReturnsTrue() {
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(testTransaction)).thenReturn(testTransaction);

        boolean result = transactionService.markTransactionAsFailed(TRANSACTION_ID);

        assertTrue(result);
        assertEquals(Transaction.TransactionStatus.FAILED, testTransaction.getStatus());
        verify(transactionRepository).findById(TRANSACTION_ID);
        verify(transactionRepository).save(testTransaction);
    }

    @Test
    void markTransactionAsFailed_TransactionNotExists_ReturnsFalse() {
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.empty());

        boolean result = transactionService.markTransactionAsFailed(TRANSACTION_ID);

        assertFalse(result);
        verify(transactionRepository).findById(TRANSACTION_ID);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getTransactionById_TransactionExistsAndUserIsOwner_ReturnsTransactionDTO() {
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(testTransaction));
        when(currentUserUtil.getCurrentUserEmail()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        TransactionDTO result = transactionService.getTransactionById(TRANSACTION_ID);

        assertNotNull(result);
        assertEquals(TRANSACTION_ID, result.getId());
        assertEquals(USER_ID, result.getUserId());
        assertEquals("Test User", result.getUsername());

        verify(transactionRepository).findById(TRANSACTION_ID);
        verify(currentUserUtil).getCurrentUserEmail();
        verify(userRepository).findByEmail(TEST_EMAIL);
    }

    @Test
    void getTransactionById_TransactionExistsAndUserIsAdmin_ReturnsTransactionDTO() {
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(testTransaction));
        when(currentUserUtil.getCurrentUserEmail()).thenReturn(ADMIN_EMAIL);
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(adminUser));

        TransactionDTO result = transactionService.getTransactionById(TRANSACTION_ID);

        assertNotNull(result);
        assertEquals(TRANSACTION_ID, result.getId());

        verify(transactionRepository).findById(TRANSACTION_ID);
        verify(currentUserUtil).getCurrentUserEmail();
        verify(userRepository).findByEmail(ADMIN_EMAIL);
    }

    @Test
    void getTransactionById_TransactionExistsButUserIsNotOwner_ThrowsException() {
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(testTransaction));
        when(currentUserUtil.getCurrentUserEmail()).thenReturn(OTHER_EMAIL);
        when(userRepository.findByEmail(OTHER_EMAIL)).thenReturn(Optional.of(otherUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transactionService.getTransactionById(TRANSACTION_ID));
        assertEquals("Access denied: You can only view your own transactions", exception.getMessage());

        verify(transactionRepository).findById(TRANSACTION_ID);
        verify(currentUserUtil).getCurrentUserEmail();
        verify(userRepository).findByEmail(OTHER_EMAIL);
    }

    @Test
    void getTransactionById_TransactionNotExists_ThrowsException() {
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transactionService.getTransactionById(TRANSACTION_ID));
        assertEquals("Transaction not found", exception.getMessage());

        verify(transactionRepository).findById(TRANSACTION_ID);
        verifyNoInteractions(currentUserUtil);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getTransactionById_CurrentUserNotFound_ThrowsException() {
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(testTransaction));
        when(currentUserUtil.getCurrentUserEmail()).thenReturn(TEST_EMAIL);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                transactionService.getTransactionById(TRANSACTION_ID));
        assertEquals("Current user not found", exception.getMessage());

        verify(transactionRepository).findById(TRANSACTION_ID);
        verify(currentUserUtil).getCurrentUserEmail();
        verify(userRepository).findByEmail(TEST_EMAIL);
    }

    @Test
    void mapTransactionsToDTO_EmptyList_ReturnsEmptyList() {
        when(transactionRepository.findAll()).thenReturn(Arrays.asList());

        List<TransactionDTO> result = transactionService.getAllTransactions();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void mapTransactionsToDTO_MultipleTransactions_ReturnsMultipleDTOs() {
        Transaction transaction2 = Transaction.builder()
                .id("trans-456")
                .user(testUser)
                .amount(25000)
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TOP_UP)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Top-up")
                .build();

        List<Transaction> transactions = Arrays.asList(testTransaction, transaction2);
        when(transactionRepository.findAll()).thenReturn(transactions);

        List<TransactionDTO> result = transactionService.getAllTransactions();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(TRANSACTION_ID, result.get(0).getId());
        assertEquals("trans-456", result.get(1).getId());
    }
}