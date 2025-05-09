package id.ac.ui.cs.advprog.eventsphere.topup.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TopUpRequestDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TopUpResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.model.CustomTopUp;
import id.ac.ui.cs.advprog.eventsphere.topup.model.TopUp;
import id.ac.ui.cs.advprog.eventsphere.topup.model.Transaction;
import id.ac.ui.cs.advprog.eventsphere.topup.repository.TransactionRepository;
import id.ac.ui.cs.advprog.eventsphere.topup.strategy.TopUpFactory;
import id.ac.ui.cs.advprog.eventsphere.topup.strategy.TopUpStrategy;
import id.ac.ui.cs.advprog.eventsphere.topup.util.CurrentUserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TopUpServiceImplTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private TopUpStrategy topUpStrategy;
    
    @Mock
    private TopUpFactory topUpFactory;
    
    @Mock
    private CurrentUserUtil currentUserUtil;
    
    private TopUpServiceImpl topUpService;
    private User testUser;
    private Transaction testTransaction;
    
    @BeforeEach
    public void setUp() {
        topUpService = new TopUpServiceImpl(
                userRepository, 
                transactionRepository, 
                topUpStrategy, 
                topUpFactory, 
                currentUserUtil
        );
        
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .balance(50000)
                .build();
        
        testTransaction = Transaction.builder()
                .id("transaction-123")
                .user(testUser)
                .amount(50000)
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TOP_UP)
                .status(Transaction.TransactionStatus.SUCCESS)
                .description("Test transaction")
                .build();
    }
    
    @Test
    @DisplayName("Should process top up successfully")
    public void testProcessTopUp() {
        // Setup
        TopUpRequestDTO request = TopUpRequestDTO.builder()
                .amount(50000)
                .topUpType("CUSTOM")
                .build();
        
        TopUp topUp = new CustomTopUp(50000);
        
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(topUpFactory.createTopUp("CUSTOM", 50000)).thenReturn(topUp);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        
        // Execute
        TopUpResponseDTO response = topUpService.processTopUp(request);
        
        // Verify
        assertNotNull(response);
        assertEquals("transaction-123", response.getTransactionId());
        assertEquals(1L, response.getUserId());
        assertEquals(50000, response.getAmount());
        assertEquals(50000, response.getNewBalance());
        assertEquals("SUCCESS", response.getStatus());
        
        verify(topUpStrategy).executeTopUp(testUser, topUp);
        verify(userRepository).save(testUser);
        verify(transactionRepository).save(any(Transaction.class));
    }
    
    @Test
    @DisplayName("Should handle top up failure")
    public void testProcessTopUpFailure() {
        // Setup
        TopUpRequestDTO request = TopUpRequestDTO.builder()
                .amount(5000) // Too low, will cause IllegalArgumentException
                .topUpType("CUSTOM")
                .build();
        
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(topUpFactory.createTopUp("CUSTOM", 5000)).thenThrow(new IllegalArgumentException("Invalid amount"));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        
        // Execute and verify
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            topUpService.processTopUp(request);
        });
        
        assertEquals("Invalid amount", exception.getMessage());
        verify(transactionRepository).save(any(Transaction.class));
    }
    
    @Test
    @DisplayName("Should get current user top-up transactions")
    public void testGetCurrentUserTopUpTransactions() {
        // Setup
        List<Transaction> transactions = Arrays.asList(testTransaction);
        
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserAndType(testUser, Transaction.TransactionType.TOP_UP))
                .thenReturn(transactions);
        
        // Execute
        List<Transaction> result = topUpService.getCurrentUserTopUpTransactions();
        
        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("transaction-123", result.get(0).getId());
    }
    
    @Test
    @DisplayName("Should get user top-up transactions by id")
    public void testGetUserTopUpTransactions() {
        // Setup
        List<Transaction> transactions = Arrays.asList(testTransaction);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserAndType(testUser, Transaction.TransactionType.TOP_UP))
                .thenReturn(transactions);
        
        // Execute
        List<Transaction> result = topUpService.getUserTopUpTransactions(1L);
        
        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("transaction-123", result.get(0).getId());
    }
    
    @Test
    @DisplayName("Should get current user details")
    public void testGetCurrentUserDetails() {
        // Setup
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        
        // Execute
        User result = topUpService.getCurrentUserDetails();
        
        // Verify
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
    }
    
    @Test
    @DisplayName("Should get user by id")
    public void testGetUserById() {
        // Setup
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // Execute
        User result = topUpService.getUserById(1L);
        
        // Verify
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
    }
    
    @Test
    @DisplayName("Should throw exception when user not found in processTopUp")
    public void testProcessTopUpUserNotFound() {
        // Setup
        TopUpRequestDTO request = TopUpRequestDTO.builder()
                .amount(50000)
                .topUpType("CUSTOM")
                .build();
        
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        // Execute and verify
        Exception exception = assertThrows(RuntimeException.class, () -> {
            topUpService.processTopUp(request);
        });
        
        assertEquals("User not found", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when user not found in getCurrentUserTopUpTransactions")
    public void testGetCurrentUserTopUpTransactionsUserNotFound() {
        // Setup
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        // Execute and verify
        Exception exception = assertThrows(RuntimeException.class, () -> {
            topUpService.getCurrentUserTopUpTransactions();
        });
        
        assertEquals("User not found", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when user not found in getUserTopUpTransactions")
    public void testGetUserTopUpTransactionsUserNotFound() {
        // Setup
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute and verify
        Exception exception = assertThrows(RuntimeException.class, () -> {
            topUpService.getUserTopUpTransactions(999L);
        });
        
        assertEquals("User not found", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when user not found in getCurrentUserDetails")
    public void testGetCurrentUserDetailsUserNotFound() {
        // Setup
        when(currentUserUtil.getCurrentUserEmail()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        // Execute and verify
        Exception exception = assertThrows(RuntimeException.class, () -> {
            topUpService.getCurrentUserDetails();
        });
        
        assertEquals("User not found", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw exception when user not found in getUserById")
    public void testGetUserByIdUserNotFound() {
        // Setup
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Execute and verify
        Exception exception = assertThrows(RuntimeException.class, () -> {
            topUpService.getUserById(999L);
        });
        
        assertEquals("User not found", exception.getMessage());
    }
}