package id.ac.ui.cs.advprog.eventsphere.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserRoleTest {
    
    @Test
    void testUserRoleValues() {
        assertEquals("ROLE_USER", UserRole.ROLE_USER.name());
        assertEquals("ROLE_ORGANIZER", UserRole.ROLE_ORGANIZER.name());
        assertEquals("ROLE_ADMIN", UserRole.ROLE_ADMIN.name());
    }
    
    @Test
    void testUserRoleValuesCount() {
        assertEquals(3, UserRole.values().length);
    }
    
    @Test
    void testUserRoleValueOf() {
        assertEquals(UserRole.ROLE_USER, UserRole.valueOf("ROLE_USER"));
        assertEquals(UserRole.ROLE_ORGANIZER, UserRole.valueOf("ROLE_ORGANIZER"));
        assertEquals(UserRole.ROLE_ADMIN, UserRole.valueOf("ROLE_ADMIN"));
    }
    
    @Test
    void testUserRoleOrdinal() {
        assertEquals(0, UserRole.ROLE_USER.ordinal());
        assertEquals(1, UserRole.ROLE_ORGANIZER.ordinal());
        assertEquals(2, UserRole.ROLE_ADMIN.ordinal());
    }
}