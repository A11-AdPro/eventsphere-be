package id.ac.ui.cs.advprog.eventsphere.repository;

import id.ac.ui.cs.advprog.eventsphere.model.User;
import id.ac.ui.cs.advprog.eventsphere.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

// import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser = User.builder()
                .username("testuser")
                .password("password123")
                .name("Test User")
                .email("test@example.com")
                .role(UserRole.ROLE_USER)
                .build();
        entityManager.persist(testUser);

        adminUser = User.builder()
                .username("adminuser")
                .password("adminpass")
                .name("Admin User")
                .email("admin@example.com")
                .role(UserRole.ROLE_ADMIN)
                .build();
        entityManager.persist(adminUser);

        entityManager.flush();
    }

    @Test
    void findByUsername_WithExistingUsername_ShouldReturnUser() {
        // Act
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
        assertEquals(testUser.getName(), foundUser.get().getName());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
        assertEquals(UserRole.ROLE_USER, foundUser.get().getRole());
    }

    @Test
    void findByUsername_WithNonExistingUsername_ShouldReturnEmpty() {
        // Act
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // Assert
        assertFalse(foundUser.isPresent());
    }

    @Test
    void save_NewUser_ShouldPersistUser() {
        // Arrange
        User newUser = User.builder()
                .username("newuser")
                .password("newpass")
                .name("New User")
                .email("new@example.com")
                .role(UserRole.ROLE_ORGANIZER)
                .build();

        // Act
        User savedUser = userRepository.save(newUser);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("newuser", foundUser.get().getUsername());
        assertEquals("New User", foundUser.get().getName());
        assertEquals("new@example.com", foundUser.get().getEmail());
        assertEquals(UserRole.ROLE_ORGANIZER, foundUser.get().getRole());
    }

    @Test
    void save_UpdateUser_ShouldUpdateFields() {
        // Arrange
        testUser.setName("Updated Name");
        testUser.setEmail("updated@example.com");

        // Act
        userRepository.save(testUser);
        Optional<User> foundUser = userRepository.findById(testUser.getId());

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("Updated Name", foundUser.get().getName());
        assertEquals("updated@example.com", foundUser.get().getEmail());
    }

    @Test
    void delete_ShouldRemoveUser() {
        // Act
        userRepository.delete(testUser);
        Optional<User> foundUser = userRepository.findById(testUser.getId());

        // Assert
        assertFalse(foundUser.isPresent());
    }
}