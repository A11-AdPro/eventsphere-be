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
import id.ac.ui.cs.advprog.eventsphere.authentication.service.AdminUserService;
import id.ac.ui.cs.advprog.eventsphere.authentication.dto.UpdateUserRequest;
import id.ac.ui.cs.advprog.eventsphere.authentication.dto.UserResponseDTO;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
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
    public void testLogin_Success() {
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
    public void testLogin_Failure() {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("user@example.com");
        loginRequest.setPassword("wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // When & Then
        assertThrows(RuntimeException.class, () -> authController.login(loginRequest));
    }

    @Test
    public void testRegister_Success() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password");
        registerRequest.setFullName("New User");
        registerRequest.setRole(Role.ATTENDEE);

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
    public void testRegister_Failure() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("existing@example.com");
        registerRequest.setPassword("password");
        registerRequest.setFullName("Existing User");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        // When & Then
        assertThrows(RuntimeException.class, () -> authController.register(registerRequest));
    }

    @Test
    public void testRegisterWithNullRole() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password");
        registerRequest.setFullName("New User");
        registerRequest.setRole(null);

        User user = new User();
        user.setId(1L);
        user.setEmail("new@example.com");

        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        // When
        ResponseEntity<?> response = authController.register(registerRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());

        verify(authService).register(argThat(request -> 
            request.getRole() == Role.ATTENDEE
        ));
    }

    @Test
    public void testRegisterAdmin_Success() {
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
    public void testRegisterAdmin_Failure() {
        // Given
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("admin@example.com");
        registerRequest.setPassword("password");
        registerRequest.setFullName("Admin User");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Admin registration failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> authController.registerAdmin(registerRequest));
    }

    @Test
    public void testGetCurrentUser_Success() {
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

    @Test
    public void testGetCurrentUser_Failure() {
        // Given
        when(authService.getCurrentUser())
                .thenThrow(new RuntimeException("User not authenticated"));

        // When & Then
        assertThrows(RuntimeException.class, () -> authController.getCurrentUser());
    }

    @Test
    public void testHandleAdminOperations_Logging() throws Exception {
        AdminUserService adminUserService = mock(AdminUserService.class);
        
        Field adminUserServiceField = AuthController.class.getDeclaredField("adminUserService");
        adminUserServiceField.setAccessible(true);
        adminUserServiceField.set(authController, adminUserService);

        UserResponseDTO mockUserDTO = UserResponseDTO.builder()
            .id(1L)
            .email("test@example.com")
            .fullName("Test User")
            .role(Role.ATTENDEE)
            .balance(1000)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // 1. Test getAllUsers logging
        when(adminUserService.getAllUsers()).thenReturn(List.of(mockUserDTO));
        ResponseEntity<?> response = authController.getAllUsers();
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 2. Test getUserById logging
        Long testId = 1L;
        when(adminUserService.getUserById(testId)).thenReturn(mockUserDTO);
        response = authController.getUserById(testId);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 3. Test updateUser logging
        Long updateId = 2L;
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        when(adminUserService.updateUser(eq(updateId), any(UpdateUserRequest.class)))
            .thenReturn(mockUserDTO);
        response = authController.updateUser(updateId, updateRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 4. Test deleteUser logging
        Long deleteId = 3L;
        doNothing().when(adminUserService).deleteUser(deleteId);
        response = authController.deleteUser(deleteId);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 5. Test updateOwnProfile logging
        UpdateUserRequest profileRequest = new UpdateUserRequest();
        when(adminUserService.updateOwnProfile(any(UpdateUserRequest.class)))
            .thenReturn(mockUserDTO);
        response = authController.updateOwnProfile(profileRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // 6. Test getUsersByRole logging
        Role testRole = Role.ATTENDEE;
        when(adminUserService.getUsersByRole(testRole)).thenReturn(List.of(mockUserDTO));
        response = authController.getUsersByRole(testRole);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}