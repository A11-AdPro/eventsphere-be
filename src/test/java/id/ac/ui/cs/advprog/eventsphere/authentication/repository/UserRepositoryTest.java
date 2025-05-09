package id.ac.ui.cs.advprog.eventsphere.authentication.repository;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private String uniqueEmail;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        userRepository.deleteAll();

        // Create a unique email for each test
        uniqueEmail = "test-" + UUID.randomUUID() + "@example.com";
        
        // Create a test user
        testUser = User.builder()
                .email(uniqueEmail)
                .password("password123")
                .role(Role.ATTENDEE)
                .fullName("Test User")
                .balance(1000)
                .build();

        // Save the user
        testUser = userRepository.save(testUser);
    }

    @Test
    void testFindByEmail() {
        // Test finding the user by email
        Optional<User> foundUser = userRepository.findByEmail(uniqueEmail);
        
        assertTrue(foundUser.isPresent());
        assertEquals(uniqueEmail, foundUser.get().getEmail());
        assertEquals(testUser.getId(), foundUser.get().getId());
    }

    @Test
    void testFindByEmailNotFound() {
        // Test finding a non-existent email
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");
        
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testExistsByEmail() {
        // Test checking if a user exists by email
        boolean exists = userRepository.existsByEmail(uniqueEmail);
        
        assertTrue(exists);
    }

    @Test
    void testExistsByEmailNotFound() {
        // Test checking if a non-existent user exists
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");
        
        assertFalse(exists);
    }
}