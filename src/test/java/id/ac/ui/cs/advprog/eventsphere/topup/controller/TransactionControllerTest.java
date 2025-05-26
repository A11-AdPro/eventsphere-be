package id.ac.ui.cs.advprog.eventsphere.topup.controller;

import id.ac.ui.cs.advprog.eventsphere.topup.dto.TopUpResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TransactionDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.service.TransactionService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {
    
    @Mock
    private TransactionService transactionService;
    
    @InjectMocks
    private TransactionController transactionController;
    
    private TopUpResponseDTO purchaseResponse;
    private List<TransactionDTO> transactions;
    private TransactionDTO transactionDTO;
    
    @BeforeEach
    public void setUp() {        
        purchaseResponse = TopUpResponseDTO.builder()
                .transactionId("transaction-123")
                .userId(1L)
                .amount(50000)
                .newBalance(50000)
                .timestamp(LocalDateTime.now())
                .status("SUCCESS")
                .build();
        
        transactionDTO = TransactionDTO.builder()
                .id("transaction-123")
                .userId(1L)
                .username("testuser")
                .amount(50000)
                .timestamp(LocalDateTime.now())
                .type("TICKET_PURCHASE")
                .status("SUCCESS")
                .description("Purchase ticket")
                .eventId("event-123")
                .build();
        
        transactions = Arrays.asList(transactionDTO);
    }
    
    @Test
    @DisplayName("Should process ticket purchase by ID successfully")
    public void testPurchaseTicket() {
        when(transactionService.processTicketPurchaseById(anyLong())).thenReturn(purchaseResponse);
        
        ResponseEntity<TopUpResponseDTO> response = transactionController.purchaseTicket(1L);
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(purchaseResponse, response.getBody());
        
        verify(transactionService).processTicketPurchaseById(1L);
    }
    
    @Test
    @DisplayName("Should handle Exception in purchaseTicket")
    public void testPurchaseTicketException() {
        when(transactionService.processTicketPurchaseById(anyLong()))
                .thenThrow(new RuntimeException("Some error"));
        
        ResponseEntity<TopUpResponseDTO> response = transactionController.purchaseTicket(1L);
        
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("FAILED", response.getBody().getStatus());
        assertEquals("Some error", response.getBody().getMessage());
    }
    
    @Test
    @DisplayName("Should get current user transactions")
    public void testGetCurrentUserTransactions() {
        when(transactionService.getCurrentUserTransactions()).thenReturn(transactions);
        
        ResponseEntity<List<TransactionDTO>> response = transactionController.getCurrentUserTransactions();
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(transactions, response.getBody());
    }
    
    @Test
    @DisplayName("Should handle Exception in getCurrentUserTransactions")
    public void testGetCurrentUserTransactionsException() {
        when(transactionService.getCurrentUserTransactions()).thenThrow(new RuntimeException("Some error"));
        
        ResponseEntity<List<TransactionDTO>> response = transactionController.getCurrentUserTransactions();
        
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    @DisplayName("Should get all transactions")
    public void testGetAllTransactions() {
        when(transactionService.getAllTransactions()).thenReturn(transactions);
        
        ResponseEntity<List<TransactionDTO>> response = transactionController.getAllTransactions();
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(transactions, response.getBody());
    }
    
    @Test
    @DisplayName("Should get transaction by id")
    public void testGetTransactionById() {
        when(transactionService.getTransactionById("transaction-123")).thenReturn(transactionDTO);
        
        ResponseEntity<TransactionDTO> response = transactionController.getTransactionById("transaction-123");
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(transactionDTO, response.getBody());
    }
    
    @Test
    @DisplayName("Should handle Exception in getTransactionById")
    public void testGetTransactionByIdException() {
        when(transactionService.getTransactionById("transaction-123"))
                .thenThrow(new RuntimeException("Some error"));
        
        ResponseEntity<TransactionDTO> response = transactionController.getTransactionById("transaction-123");
        
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    @DisplayName("Should get user transactions by id")
    public void testGetUserTransactions() {
        when(transactionService.getUserTransactions(1L)).thenReturn(transactions);
        
        ResponseEntity<List<TransactionDTO>> response = transactionController.getUserTransactions(1L);
        
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(transactions, response.getBody());
    }
    
    @Test
    @DisplayName("Should handle Exception in getUserTransactions")
    public void testGetUserTransactionsException() {
        when(transactionService.getUserTransactions(1L)).thenThrow(new RuntimeException("Some error"));
        
        ResponseEntity<List<TransactionDTO>> response = transactionController.getUserTransactions(1L);
        
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    @DisplayName("Should delete transaction")
    public void testDeleteTransaction() {
        when(transactionService.deleteTransaction("transaction-123")).thenReturn(true);
        
        ResponseEntity<Void> response = transactionController.deleteTransaction("transaction-123");
        
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
    
    @Test
    @DisplayName("Should return not found when deleting non-existent transaction")
    public void testDeleteTransactionNotFound() {
        when(transactionService.deleteTransaction("non-existent")).thenReturn(false);
        
        ResponseEntity<Void> response = transactionController.deleteTransaction("non-existent");
        
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    
    @Test
    @DisplayName("Should mark transaction as failed")
    public void testMarkTransactionAsFailed() {
        when(transactionService.markTransactionAsFailed("transaction-123")).thenReturn(true);
        
        ResponseEntity<Void> response = transactionController.markTransactionAsFailed("transaction-123");
        
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
    
    @Test
    @DisplayName("Should return not found when marking non-existent transaction as failed")
    public void testMarkTransactionAsFailedNotFound() {
        when(transactionService.markTransactionAsFailed("non-existent")).thenReturn(false);
        
        ResponseEntity<Void> response = transactionController.markTransactionAsFailed("non-existent");
        
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}