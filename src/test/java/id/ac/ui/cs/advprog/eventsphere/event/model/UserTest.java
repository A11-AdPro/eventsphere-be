package id.ac.ui.cs.advprog.eventsphere.event.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    
    private User user;
    
    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password123")
                .name("Test User")
                .email("test@example.com")
                .role(UserRole.ROLE_USER)
                .build();
    }
    
    @Test
    void testUserConstructor() {
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(UserRole.ROLE_USER, user.getRole());
    }
    
    @Test
    void testNoArgsConstructor() {
        User emptyUser = new User();
        assertNotNull(emptyUser);
        assertNull(emptyUser.getId());
        assertNull(emptyUser.getUsername());
        assertNull(emptyUser.getPassword());
        assertNull(emptyUser.getName());
        assertNull(emptyUser.getEmail());
        assertNull(emptyUser.getRole());
    }
    
    @Test
    void testPrePersist() {
        User newUser = new User();
        newUser.onCreate();
        
        assertNotNull(newUser.getCreatedAt());
        assertNotNull(newUser.getUpdatedAt());
        assertEquals(newUser.getCreatedAt(), newUser.getUpdatedAt());
    }
    
    @Test
    void testPreUpdate() {
        LocalDateTime initialUpdateTime = LocalDateTime.now().minusDays(1);
        user.setUpdatedAt(initialUpdateTime);
        
        user.onUpdate();
        
        assertNotEquals(initialUpdateTime, user.getUpdatedAt());
        assertTrue(user.getUpdatedAt().isAfter(initialUpdateTime));
    }
    
    @Test
    void testSetters() {
        User newUser = new User();
        
        newUser.setId(2L);
        newUser.setUsername("newuser");
        newUser.setPassword("newpassword");
        newUser.setName("New User");
        newUser.setEmail("new@example.com");
        newUser.setRole(UserRole.ROLE_ADMIN);
        
        assertEquals(2L, newUser.getId());
        assertEquals("newuser", newUser.getUsername());
        assertEquals("newpassword", newUser.getPassword());
        assertEquals("New User", newUser.getName());
        assertEquals("new@example.com", newUser.getEmail());
        assertEquals(UserRole.ROLE_ADMIN, newUser.getRole());
    }
    
    @Test
    void testUserRoleValues() {
        assertEquals(UserRole.ROLE_USER, user.getRole());
        
        user.setRole(UserRole.ROLE_ORGANIZER);
        assertEquals(UserRole.ROLE_ORGANIZER, user.getRole());
        
        user.setRole(UserRole.ROLE_ADMIN);
        assertEquals(UserRole.ROLE_ADMIN, user.getRole());
    }
}