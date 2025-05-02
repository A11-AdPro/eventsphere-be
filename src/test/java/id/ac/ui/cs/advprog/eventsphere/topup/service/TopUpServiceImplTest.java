package id.ac.ui.cs.advprog.eventsphere.topup.service;

import id.ac.ui.cs.advprog.eventsphere.topup.dto.TopUpRequestDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.dto.TopUpResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.topup.entity.User;
import id.ac.ui.cs.advprog.eventsphere.topup.model.TopUp;
import id.ac.ui.cs.advprog.eventsphere.topup.model.Transaction;
import id.ac.ui.cs.advprog.eventsphere.topup.repository.TransactionRepository;
import id.ac.ui.cs.advprog.eventsphere.topup.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.topup.strategy.TopUpFactory;
import id.ac.ui.cs.advprog.eventsphere.topup.strategy.TopUpStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TopUpServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TopUpStrategy topUpStrategy;

    @Mock
    private TopUpFactory topUpFactory;

    private TopUpService topUpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        topUpService = new TopUpServiceImpl(userRepository, transactionRepository, topUpStrategy, topUpFactory);
    }

    @Test
    void processTopUp_ValidFixedTopUp_Success() {
        User user = User.builder().id("user-123").username("testuser").balance(0).build();
        TopUpRequestDTO request = new TopUpRequestDTO("user-123", 50000, "FIXED");
        TopUp topUp = mock(TopUp.class);
        when(topUp.getAmount()).thenReturn(50000);
        when(topUp.getType()).thenReturn("FIXED");

        when(userRepository.findById("user-123")).thenReturn(Optional.of(user));
        when(topUpFactory.createTopUp("FIXED", 50000)).thenReturn(topUp);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            return t;
        });

        TopUpResponseDTO response = topUpService.processTopUp(request);

        assertNotNull(response);
        assertEquals("user-123", response.getUserId());
        assertEquals(50000, response.getAmount());
        assertEquals("SUCCESS", response.getStatus());

        verify(topUpStrategy).executeTopUp(user, topUp);
        verify(userRepository).save(user);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void processTopUp_ValidCustomTopUp_Success() {
        // Arrange
        User user = User.builder().id("user-123").username("testuser").balance(0).build();
        TopUpRequestDTO request = new TopUpRequestDTO("user-123", 25000, "CUSTOM");
        TopUp topUp = mock(TopUp.class);
        when(topUp.getAmount()).thenReturn(25000);
        when(topUp.getType()).thenReturn("CUSTOM");

        when(userRepository.findById("user-123")).thenReturn(Optional.of(user));
        when(topUpFactory.createTopUp("CUSTOM", 25000)).thenReturn(topUp);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            return t;
        });

        TopUpResponseDTO response = topUpService.processTopUp(request);

        assertNotNull(response);
        assertEquals("user-123", response.getUserId());
        assertEquals(25000, response.getAmount());
        assertEquals("SUCCESS", response.getStatus());

        verify(topUpStrategy).executeTopUp(user, topUp);
        verify(userRepository).save(user);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void processTopUp_UserNotFound_ThrowsException() {
        TopUpRequestDTO request = new TopUpRequestDTO("non-existent-user", 50000, "FIXED");
        when(userRepository.findById("non-existent-user")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> topUpService.processTopUp(request));
        verify(topUpFactory, never()).createTopUp(anyString(), anyInt());
        verify(topUpStrategy, never()).executeTopUp(any(), any());
    }

    @Test
    void processTopUp_InvalidTopUpAmount_ThrowsException() {
        User user = User.builder().id("user-123").username("testuser").balance(0).build();
        TopUpRequestDTO request = new TopUpRequestDTO("user-123", 5000, "CUSTOM"); // Too low amount

        when(userRepository.findById("user-123")).thenReturn(Optional.of(user));
        when(topUpFactory.createTopUp("CUSTOM", 5000)).thenThrow(new IllegalArgumentException("Custom top-up amount must be between 10,000 and 1,000,000"));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(IllegalArgumentException.class, () -> topUpService.processTopUp(request));
        verify(topUpStrategy, never()).executeTopUp(any(), any());
        verify(userRepository, never()).save(any(User.class));
        verify(transactionRepository).save(any(Transaction.class)); 
    }
}