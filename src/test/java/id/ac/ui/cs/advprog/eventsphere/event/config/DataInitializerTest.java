package id.ac.ui.cs.advprog.eventsphere.event.config;

import id.ac.ui.cs.advprog.eventsphere.event.model.User;
import id.ac.ui.cs.advprog.eventsphere.event.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void init_shouldCreateOrganizer_whenNoUsersExist() {
        // Arrange
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        // Act
        dataInitializer.init();

        // Assert
        verify(userRepository).count();
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void init_shouldNotCreateOrganizer_whenUsersExist() {
        // Arrange
        when(userRepository.count()).thenReturn(1L);

        // Act
        dataInitializer.init();

        // Assert
        verify(userRepository).count();
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}