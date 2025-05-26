package id.ac.ui.cs.advprog.eventsphere.authentication.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.dto.UpdateUserRequest;
import id.ac.ui.cs.advprog.eventsphere.authentication.dto.UserResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User user;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setRole(Role.ATTENDEE);
        user.setPassword("password");
        user.setBalance(1000);

        updateRequest = new UpdateUserRequest();
    }

    @Test
    void updateOwnProfile_ShouldOnlyUpdateEmail() {
        setupSecurityContext();
        
        updateRequest.setEmail("new@example.com");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserResponseDTO result = adminUserService.updateOwnProfile(updateRequest);
        
        assertEquals("new@example.com", result.getEmail());
        assertEquals("Test User", result.getFullName()); // Original remains
    }

    @Test
    void updateOwnProfile_ShouldOnlyUpdateFullName() {
        setupSecurityContext();
        
        updateRequest.setFullName("New Name");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserResponseDTO result = adminUserService.updateOwnProfile(updateRequest);
        
        assertEquals("New Name", result.getFullName());
        assertEquals("test@example.com", result.getEmail()); // Original remains
    }

    @Test
    void updateOwnProfile_ShouldOnlyUpdatePassword() {
        setupSecurityContext();
        
        updateRequest.setPassword("newpassword");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserResponseDTO result = adminUserService.updateOwnProfile(updateRequest);
        
        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(user);
    }

    @Test
    void updateOwnProfile_ShouldNotUpdateRole() {
        setupSecurityContext();
        
        updateRequest.setRole(Role.ADMIN);
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserResponseDTO result = adminUserService.updateOwnProfile(updateRequest);
        
        assertEquals(Role.ADMIN, result.getRole());
    }

    // Helper method for security context setup
    private void setupSecurityContext() {
        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void updateUser_ShouldOnlyUpdateEmailWhenOtherFieldsAreNull() {
        updateRequest.setEmail("new@example.com");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserResponseDTO result = adminUserService.updateUser(1L, updateRequest);
        
        assertEquals("new@example.com", result.getEmail());
        assertEquals("Test User", result.getFullName()); // Original value remains
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_ShouldOnlyUpdateFullName() {
        updateRequest.setFullName("New Name");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserResponseDTO result = adminUserService.updateUser(1L, updateRequest);
        
        assertEquals("New Name", result.getFullName());
        assertEquals("test@example.com", result.getEmail()); // Original value remains
    }

    @Test
    void updateUser_ShouldOnlyUpdatePassword() {
        updateRequest.setPassword("newpassword");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserResponseDTO result = adminUserService.updateUser(1L, updateRequest);
        
        verify(passwordEncoder).encode("newpassword");
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_ShouldOnlyUpdateBalance() {
        updateRequest.setBalance(5000);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserResponseDTO result = adminUserService.updateUser(1L, updateRequest);
        
        assertEquals(5000, result.getBalance());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_ShouldOnlyUpdateRole() {
        updateRequest.setRole(Role.ADMIN);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserResponseDTO result = adminUserService.updateUser(1L, updateRequest);
        
        assertEquals(Role.ADMIN, result.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        
        List<UserResponseDTO> result = adminUserService.getAllUsers();
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(user.getEmail(), result.get(0).getEmail());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        UserResponseDTO result = adminUserService.getUserById(1L);
        
        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> adminUserService.getUserById(1L));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void updateUser_ShouldUpdateUserDetails() {
        updateRequest.setEmail("new@example.com");
        updateRequest.setFullName("New Name");
        updateRequest.setRole(Role.ADMIN);
        updateRequest.setBalance(2000);
        updateRequest.setPassword("newpassword");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserResponseDTO result = adminUserService.updateUser(1L, updateRequest);
        
        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        assertEquals("New Name", result.getFullName());
        assertEquals(Role.ADMIN, result.getRole());
        assertEquals(2000, result.getBalance());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUser_WithExistingEmail_ShouldThrowException() {
        updateRequest.setEmail("existing@example.com");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        
        assertThrows(RuntimeException.class, () -> adminUserService.updateUser(1L, updateRequest));
    }

    @Test
    void deleteUser_ShouldDeleteUser() {
        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("admin@example.com");
        SecurityContextHolder.setContext(securityContext);
        
        adminUserService.deleteUser(1L);
        
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUser_WhenDeletingOwnAccount_ShouldThrowException() {
        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        SecurityContextHolder.setContext(securityContext);
        
        assertThrows(RuntimeException.class, () -> adminUserService.deleteUser(1L));
    }

    @Test
    void updateOwnProfile_ShouldUpdateCurrentUser() {
        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@example.com");
        SecurityContextHolder.setContext(securityContext);
        
        updateRequest.setEmail("new@example.com");
        updateRequest.setFullName("New Name");
        updateRequest.setPassword("newpassword");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        UserResponseDTO result = adminUserService.updateOwnProfile(updateRequest);
        
        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        assertEquals("New Name", result.getFullName());
    }

    @Test
    void getUsersByRole_ShouldReturnFilteredUsers() {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);
        
        when(userRepository.findAll()).thenReturn(List.of(user, adminUser));
        
        List<UserResponseDTO> result = adminUserService.getUsersByRole(Role.ADMIN);
        
        assertEquals(1, result.size());
        assertEquals(Role.ADMIN, result.get(0).getRole());
        assertEquals("admin@example.com", result.get(0).getEmail());
    }

    // @Test
    // void toResponseDTO_ShouldConvertUserToDTO() {
    //     UserResponseDTO result = adminUserService.toResponseDTO(user);
        
    //     assertNotNull(result);
    //     assertEquals(user.getId(), result.getId());
    //     assertEquals(user.getEmail(), result.getEmail());
    //     assertEquals(user.getFullName(), result.getFullName());
    //     assertEquals(user.getRole(), result.getRole());
    //     assertEquals(user.getBalance(), result.getBalance());
    //     assertEquals(user.getCreatedAt(), result.getCreatedAt());
    //     assertEquals(user.getUpdatedAt(), result.getUpdatedAt());
    // }
}