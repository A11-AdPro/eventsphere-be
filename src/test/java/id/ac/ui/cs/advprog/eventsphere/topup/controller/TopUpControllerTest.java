package id.ac.ui.cs.advprog.eventsphere.topup.controller;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TopUpRequestDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TopUpResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.model.Transaction;
import id.ac.ui.cs.advprog.eventsphere.topup.service.TopUpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TopUpControllerTest {
    
    @Mock
    private TopUpService topUpService;
    
    @InjectMocks
    private TopUpController topUpController;
    
    private TopUpRequestDTO topUpRequest;
    private TopUpResponseDTO topUpResponse;
    private User testUser;
    private List<Transaction> transactions;
    
    @BeforeEach
    public void setUp() {
        topUpRequest = TopUpRequestDTO.builder()
                .amount(50000)
                .topUpType("FIXED")
                .build();
        
        topUpResponse = TopUpResponseDTO.builder()
                .transactionId("transaction-123")
                .userId(1L)
                .amount(50000)
                .newBalance(100000)
                .timestamp(LocalDateTime.now())
                .status("SUCCESS")
                .build();
        
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .balance(100000)
                .build();
        
        Transaction transaction = Transaction.builder()
                .id("transaction-123")
                .user(testUser)
                .amount(50000)
                .timestamp(LocalDateTime.now())
                .type(Transaction.TransactionType.TOP_UP)
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();
        
        transactions = Arrays.asList(transaction);
    }
    
    @Test
    @DisplayName("Should process top up successfully")
    public void testProcessTopUp() {
        when(topUpService.processTopUp(any(TopUpRequestDTO.class))).thenReturn(topUpResponse);
        
        ResponseEntity<TopUpResponseDTO> response = topUpController.processTopUp(topUpRequest);
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(topUpResponse, response.getBody());
    }
    
    @Test
    @DisplayName("Should handle IllegalArgumentException in processTopUp")
    public void testProcessTopUpIllegalArgumentException() {
        when(topUpService.processTopUp(any(TopUpRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid amount"));
        
        ResponseEntity<TopUpResponseDTO> response = topUpController.processTopUp(topUpRequest);
        
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    @DisplayName("Should handle general Exception in processTopUp")
    public void testProcessTopUpException() {
        when(topUpService.processTopUp(any(TopUpRequestDTO.class)))
                .thenThrow(new RuntimeException("Some error"));
        
        ResponseEntity<TopUpResponseDTO> response = topUpController.processTopUp(topUpRequest);
        
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    @DisplayName("Should get current user balance")
    public void testGetCurrentUserBalance() {
        when(topUpService.getCurrentUserDetails()).thenReturn(testUser);
        
        ResponseEntity<User> response = topUpController.getCurrentUserBalance();
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
    }
    
    @Test
    @DisplayName("Should handle Exception in getCurrentUserBalance")
    public void testGetCurrentUserBalanceException() {
        when(topUpService.getCurrentUserDetails()).thenThrow(new RuntimeException("Some error"));
        
        ResponseEntity<User> response = topUpController.getCurrentUserBalance();
        
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    @DisplayName("Should get current user top-up transactions")
    public void testGetCurrentUserTopUpTransactions() {
        when(topUpService.getCurrentUserTopUpTransactions()).thenReturn(transactions);
        
        ResponseEntity<List<Transaction>> response = topUpController.getCurrentUserTopUpTransactions();
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(transactions, response.getBody());
    }
    
    @Test
    @DisplayName("Should handle Exception in getCurrentUserTopUpTransactions")
    public void testGetCurrentUserTopUpTransactionsException() {
        when(topUpService.getCurrentUserTopUpTransactions()).thenThrow(new RuntimeException("Some error"));
        
        ResponseEntity<List<Transaction>> response = topUpController.getCurrentUserTopUpTransactions();
        
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    @DisplayName("Should get user balance by id")
    public void testGetUserBalance() {
        when(topUpService.getUserById(1L)).thenReturn(testUser);
        
        ResponseEntity<User> response = topUpController.getUserBalance(1L);
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUser, response.getBody());
    }
    
    @Test
    @DisplayName("Should handle Exception in getUserBalance")
    public void testGetUserBalanceException() {
        when(topUpService.getUserById(1L)).thenThrow(new RuntimeException("Some error"));
        
        ResponseEntity<User> response = topUpController.getUserBalance(1L);
        
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    @DisplayName("Should get user top-up transactions by id")
    public void testGetUserTopUpTransactions() {
        when(topUpService.getUserTopUpTransactions(1L)).thenReturn(transactions);
        
        ResponseEntity<List<Transaction>> response = topUpController.getUserTopUpTransactions(1L);
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(transactions, response.getBody());
    }
    
    @Test
    @DisplayName("Should handle Exception in getUserTopUpTransactions")
    public void testGetUserTopUpTransactionsException() {
        when(topUpService.getUserTopUpTransactions(1L)).thenThrow(new RuntimeException("Some error"));
        
        ResponseEntity<List<Transaction>> response = topUpController.getUserTopUpTransactions(1L);
        
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}