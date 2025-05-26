package id.ac.ui.cs.advprog.eventsphere.authentication.controller;

import id.ac.ui.cs.advprog.eventsphere.authentication.dto.*;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.AuthService;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final AdminUserService adminUserService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getEmail());
        try {
            JwtResponse response = authService.login(loginRequest);
            log.info("Login successful for user: {}", loginRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Login failed for user: {} - {}", loginRequest.getEmail(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        log.info("Registration attempt for user: {}", registerRequest.getEmail());
        try {
            if (registerRequest.getRole() == null) {
                registerRequest.setRole(Role.ATTENDEE);
            }
            authService.register(registerRequest);
            log.info("Registration successful for user: {}", registerRequest.getEmail());
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            log.warn("Registration failed for user: {} - {}", registerRequest.getEmail(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/admin/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest registerRequest) {
        log.info("Admin registration for user: {}", registerRequest.getEmail());
        try {
            authService.register(registerRequest);
            log.info("Admin registration successful for user: {}", registerRequest.getEmail());
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            log.warn("Admin registration failed for user: {} - {}", registerRequest.getEmail(), e.getMessage());
            throw e;
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getCurrentUser() {
        log.debug("Get current user request");
        return ResponseEntity.ok(authService.getCurrentUser());
    }
    
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        log.info("Admin requesting all users");
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @GetMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        log.info("Admin requesting user ID: {}", id);
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    @PutMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest updateRequest) {
        log.info("Admin updating user ID: {}", id);
        return ResponseEntity.ok(adminUserService.updateUser(id, updateRequest));
    }

    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        log.warn("Admin deleting user ID: {}", id);
        adminUserService.deleteUser(id);
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "User deleted successfully"
        ));
    }

    @PutMapping("/admin/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateOwnProfile(@RequestBody UpdateUserRequest updateRequest) {
        log.info("Admin updating own profile");
        return ResponseEntity.ok(adminUserService.updateOwnProfile(updateRequest));
    }

    @GetMapping("/admin/users/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(@PathVariable Role role) {
        log.info("Admin requesting users with role: {}", role);
        return ResponseEntity.ok(adminUserService.getUsersByRole(role));
    }
}