package id.ac.ui.cs.advprog.eventsphere.authentication.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserBalanceServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserBalanceService userBalanceService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .balance(100)
                .build();
    }

    @Test
    void topUpShouldIncreaseBalanceAndSaveUser() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userBalanceService.topUp(testUser, 50);

        // Assert
        assertEquals(150, testUser.getBalance());
        verify(userRepository).save(testUser);
    }

    @Test
    void topUpShouldThrowExceptionWhenAmountIsNegative() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userBalanceService.topUp(testUser, -50);
        });

        assertEquals("Top-up amount must be positive", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deductBalanceShouldReduceBalanceAndReturnTrueWhenSufficient() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        boolean result = userBalanceService.deductBalance(testUser, 50);

        // Assert
        assertTrue(result);
        assertEquals(50, testUser.getBalance());
        verify(userRepository).save(testUser);
    }

    @Test
    void deductBalanceShouldReturnFalseWhenInsufficientBalance() {
        // Act
        boolean result = userBalanceService.deductBalance(testUser, 150);

        // Assert
        assertFalse(result);
        assertEquals(100, testUser.getBalance());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deductBalanceShouldThrowExceptionWhenAmountIsNegative() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userBalanceService.deductBalance(testUser, -50);
        });

        assertEquals("Deduction amount must be positive", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getBalanceShouldReturnUserBalance() {
        // Act
        int balance = userBalanceService.getBalance(testUser);

        // Assert
        assertEquals(100, balance);
    }
}