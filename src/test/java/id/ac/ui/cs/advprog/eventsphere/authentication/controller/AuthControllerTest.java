package id.ac.ui.cs.advprog.eventsphere.authentication.controller;

import id.ac.ui.cs.advprog.eventsphere.authentication.dto.*;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.*;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLogin() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("password");

        JwtResponse jwtResponse = JwtResponse.builder()
                .token("token")
                .email("user@example.com")
                .role(Role.ATTENDEE)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(jwtResponse);

        // When
        ResponseEntity<JwtResponse> response = authController.login(loginRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("token", response.getBody().getToken());
        assertEquals("user@example.com", response.getBody().getEmail());
        assertEquals(Role.ATTENDEE, response.getBody().getRole());
    }

    @Test
    public void testRegister() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password");
        registerRequest.setFullName("New User");
        registerRequest.setRole(Role.ATTENDEE); // Role is explicitly set

        User user = new User();
        user.setId(1L);
        user.setEmail("new@example.com");

        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        // When
        ResponseEntity<?> response = authController.register(registerRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());
    }

    @Test
    public void testRegisterWithNullRole() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password");
        registerRequest.setFullName("New User");
        registerRequest.setRole(null); // Role is explicitly set to null

        User user = new User();
        user.setId(1L);
        user.setEmail("new@example.com");

        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        // When
        ResponseEntity<?> response = authController.register(registerRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());

        // Verify that the role was set to ATTENDEE before passing to service
        verify(authService).register(argThat(request ->
                request.getRole() == Role.ATTENDEE
        ));
    }

    @Test
    public void testRegisterAdmin() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("admin@example.com");
        registerRequest.setPassword("password");
        registerRequest.setFullName("Admin User");
        registerRequest.setRole(Role.ADMIN);

        User user = new User();
        user.setId(1L);
        user.setEmail("admin@example.com");

        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        // When
        ResponseEntity<?> response = authController.registerAdmin(registerRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());
    }

    @Test
    public void testGetCurrentUser() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(Role.ATTENDEE);

        when(authService.getCurrentUser()).thenReturn(user);

        // When
        ResponseEntity<User> response = authController.getCurrentUser();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("user@example.com", response.getBody().getEmail());
    }
}