package id.ac.ui.cs.advprog.eventsphere.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired

    @Test
    void passwordEncoder_shouldReturnBCryptPasswordEncoder() {
        // Assert
        assertNotNull(passwordEncoder);
        
        // Test that it's a BCrypt encoder by checking a known pattern
        String encoded = passwordEncoder.encode("testPassword");
        assertTrue(encoded.startsWith("$2a$") || encoded.startsWith("$2b$") || encoded.startsWith("$2y$"));
    }

    @Test
    void securityFilterChain_shouldAllowAccessToEventEndpoints() throws Exception {
        // Assert
        mockMvc.perform(get("/api/events"))
               .andExpect(status().isOk());
    }

    @Test
    void securityFilterChain_shouldRequireAuthenticationForOtherEndpoints() throws Exception {
        // Assert
        mockMvc.perform(get("/some-other-endpoint"))
               .andExpect(status().isUnauthorized());
    }
}