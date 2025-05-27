package id.ac.ui.cs.advprog.eventsphere.authentication.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.dto.JwtResponse;
import id.ac.ui.cs.advprog.eventsphere.authentication.dto.LoginRequest;
import id.ac.ui.cs.advprog.eventsphere.authentication.dto.RegisterRequest;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.authentication.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .role(Role.ATTENDEE)
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("newPassword");
        registerRequest.setFullName("New User");
        registerRequest.setRole(Role.ATTENDEE);
    }

    @Test
    void loginShouldReturnJwtResponseWhenCredentialsAreValid() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt.token.string");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        JwtResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt.token.string", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(Role.ATTENDEE, response.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(authentication);
        verify(securityContext).setAuthentication(authentication);
        
        // Clean up
        SecurityContextHolder.clearContext();
    }

    @Test
    void loginShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt.token.string");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void registerShouldCreateAndReturnUserWhenEmailIsNotInUse() {
        // Arrange
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        assertEquals("encodedNewPassword", result.getPassword());
        assertEquals("New User", result.getFullName());
        assertEquals(Role.ATTENDEE, result.getRole());

        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerShouldThrowExceptionWhenEmailIsAlreadyInUse() {
        // Arrange
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getCurrentUserShouldReturnUserWhenUserExists() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        User result = authService.getCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result);

        verify(securityContext).getAuthentication();
        verify(userRepository).findByEmail("test@example.com");
        
        // Clean up
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUserShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.getCurrentUser();
        });

        assertEquals("User not found", exception.getMessage());
        
        // Clean up
        SecurityContextHolder.clearContext();
    }
}