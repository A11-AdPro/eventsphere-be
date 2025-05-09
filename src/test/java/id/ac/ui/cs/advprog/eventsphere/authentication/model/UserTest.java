package id.ac.ui.cs.advprog.eventsphere.authentication.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Should test toString method")
    public void testToString() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.ATTENDEE)
                .fullName("Test Attendee")
                .balance(1000)
                .build();
        
        String result = user.toString();
        
        assertTrue(result.contains("1"));
        assertTrue(result.contains("test@example.com"));
        assertTrue(result.contains("ATTENDEE"));  
        assertTrue(result.contains("Test Attendee"));
        assertTrue(result.contains("1000"));
    }

    @Test
    @DisplayName("Should successfully top up user balance with positive amount")
    void testTopUpWithPositiveAmount() {
        user.setBalance(100);
        user.topUp(50);
        assertEquals(150, user.getBalance());
    }

    @Test
    @DisplayName("Should throw exception when topping up with negative amount")
    void testTopUpWithNegativeAmount() {
        user.setBalance(100);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            user.topUp(-50);
        });
        assertEquals("Top-up amount must be positive", exception.getMessage());
        assertEquals(100, user.getBalance());
    }

    @Test
    @DisplayName("Should successfully deduct balance when sufficient")
    void testDeductBalanceSuccess() {
        user.setBalance(100);
        boolean result = user.deductBalance(50);
        assertTrue(result);
        assertEquals(50, user.getBalance());
    }

    @Test
    @DisplayName("Should fail to deduct balance when insufficient")
    void testDeductBalanceInsufficientFunds() {
        user.setBalance(100);
        boolean result = user.deductBalance(150);
        assertFalse(result);
        assertEquals(100, user.getBalance());
    }

    @Test
    @DisplayName("Should throw exception when deducting negative amount")
    void testDeductBalanceNegativeAmount() {
        user.setBalance(100);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            user.deductBalance(-50);
        });
        assertEquals("Deduction amount must be positive", exception.getMessage());
        assertEquals(100, user.getBalance());
    }

    @Test
    @DisplayName("Should initialize null balance when topping up")
    void testTopUpWithNullBalance() {
        // Create a new user with null balance using reflection
        try {
            java.lang.reflect.Field balanceField = User.class.getDeclaredField("balance");
            balanceField.setAccessible(true);
            balanceField.set(user, null);
            
            user.topUp(50);
            assertEquals(50, user.getBalance());
        } catch (Exception e) {
            fail("Failed to set null balance: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should fail to deduct from null balance")
    void testDeductBalanceFromNullBalance() {
        // Create a new user with null balance using reflection
        try {
            java.lang.reflect.Field balanceField = User.class.getDeclaredField("balance");
            balanceField.setAccessible(true);
            balanceField.set(user, null);
            
            boolean result = user.deductBalance(50);
            assertFalse(result);
        } catch (Exception e) {
            fail("Failed to set null balance: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should return 0 for null balance")
    void testGetBalanceWithNullBalance() {
        // Create a new user with null balance using reflection
        try {
            java.lang.reflect.Field balanceField = User.class.getDeclaredField("balance");
            balanceField.setAccessible(true);
            balanceField.set(user, null);
            
            assertEquals(0, user.getBalance());
        } catch (Exception e) {
            fail("Failed to set null balance: " + e.getMessage());
        }
    }


    @Test
    @DisplayName("Should get fullName as username when fullName exists")
    void testGetUsernameWithFullName() {
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        assertEquals("John Doe", user.getUsername());
    }

    @Test
    @DisplayName("Should get email as username when fullName is empty")
    void testGetUsernameWithEmptyFullName() {
        user.setFullName("");
        user.setEmail("john@example.com");
        assertEquals("john@example.com", user.getUsername());
    }

    @Test
    @DisplayName("Should get email as username when fullName is null")
    void testGetUsernameWithNullFullName() {
        user.setFullName(null);
        user.setEmail("john@example.com");
        assertEquals("john@example.com", user.getUsername());
    }

    @Test
    @DisplayName("Should set balance correctly")
    void testSetBalance() {
        user.setBalance(200);
        assertEquals(200, user.getBalance());
    }
}