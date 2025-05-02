package id.ac.ui.cs.advprog.eventsphere.authentication.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    void testUserIdGetterAndSetter() {
        Long id = 1L;
        user.setId(id);
        assertEquals(id, user.getId());
    }

    @Test
    void testUserEmailGetterAndSetter() {
        String email = "test@example.com";
        user.setEmail(email);
        assertEquals(email, user.getEmail());
    }

    @Test
    void testUserPasswordGetterAndSetter() {
        String password = "securePassword123";
        user.setPassword(password);
        assertEquals(password, user.getPassword());
    }

    @Test
    void testUserRoleGetterAndSetter() {
        Role role = Role.ATTENDEE;
        user.setRole(role);
        assertEquals(role, user.getRole());
    }

    @Test
    void testUserFullNameGetterAndSetter() {
        String fullName = "John Doe";
        user.setFullName(fullName);
        assertEquals(fullName, user.getFullName());
    }

    @Test
    void testUserCreatedAtGetterAndSetter() {
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    void testUserUpdatedAtGetterAndSetter() {
        LocalDateTime now = LocalDateTime.now();
        user.setUpdatedAt(now);
        assertEquals(now, user.getUpdatedAt());
    }

    @Test
    void testOnCreateSetsCreatedAtAndUpdatedAt() {
        try {
            java.lang.reflect.Method onCreate = User.class.getDeclaredMethod("onCreate");
            onCreate.setAccessible(true);
            onCreate.invoke(user);

            assertNotNull(user.getCreatedAt());
            assertNotNull(user.getUpdatedAt());
            assertEquals(
                    user.getCreatedAt().withNano(0),
                    user.getUpdatedAt().withNano(0)
            );
        } catch (Exception e) {
            fail("Failed to test onCreate method: " + e.getMessage());
        }
    }

    @Test
    void testOnUpdateSetsUpdatedAt() {
        LocalDateTime initialTime = LocalDateTime.now().minusHours(1);
        user.setCreatedAt(initialTime);
        user.setUpdatedAt(initialTime);

        try {
            java.lang.reflect.Method onUpdate = User.class.getDeclaredMethod("onUpdate");
            onUpdate.setAccessible(true);
            onUpdate.invoke(user);

            assertEquals(initialTime, user.getCreatedAt());
            assertNotEquals(initialTime, user.getUpdatedAt());
            assertTrue(user.getUpdatedAt().isAfter(initialTime));
        } catch (Exception e) {
            fail("Failed to test onUpdate method: " + e.getMessage());
        }
    }

    @Test
    void testEqualsAndHashCode() {
        User user1 = new User();
        user1.setId(1L);
        user1.setEmail("same@example.com");

        User user2 = new User();
        user2.setId(1L);
        user2.setEmail("same@example.com");

        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setEmail("different@example.com");

        assertEquals(user1, user2);
        assertNotEquals(user1, differentUser);

        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), differentUser.hashCode());
    }

    @Test
    void testToString() {
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        String toString = user.toString();

        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("email=test@example.com"));
        assertTrue(toString.contains("fullName=Test User"));
    }
}