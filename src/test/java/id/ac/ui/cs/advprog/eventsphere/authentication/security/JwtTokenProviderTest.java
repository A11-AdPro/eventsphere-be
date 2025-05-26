package id.ac.ui.cs.advprog.eventsphere.authentication.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private Authentication authentication;
    private UserDetails userDetails;
    private SecretKey testKey;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();

        // Generate and inject test key and expiration
        testKey = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        ReflectionTestUtils.setField(jwtTokenProvider, "key", testKey);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 86400000L); // 24 hours

        List<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        userDetails = new User("testuser", "password", authorities);
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    @Test
    void testGenerateToken_Success() {
        String token = jwtTokenProvider.generateToken(authentication);

        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertEquals(3, token.split("\\.").length); // JWT = header.payload.signature

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(testKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("testuser", claims.getSubject());
        assertEquals("ROLE_USER,ROLE_ADMIN", claims.get("roles"));
    }

    @Test
    void testGenerateToken_WithSingleRole() {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails user = new User("singleuser", "password", authorities);
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, authorities);

        String token = jwtTokenProvider.generateToken(auth);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(testKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("singleuser", claims.getSubject());
        assertEquals("ROLE_USER", claims.get("roles"));
    }

    @Test
    void testValidateToken_ValidToken() {
        String token = jwtTokenProvider.generateToken(authentication);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testValidateToken_InvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token"));
    }

    @Test
    void testValidateToken_ExpiredToken() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", -1000L); // expired
        String token = jwtTokenProvider.generateToken(authentication);

        // Reset expiration for other tests
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 86400000L);
        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testValidateToken_NullToken() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }

    @Test
    void testValidateToken_EmptyToken() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    void testGetAuthentication_Success() {
        String token = jwtTokenProvider.generateToken(authentication);
        Authentication result = jwtTokenProvider.getAuthentication(token);

        assertNotNull(result);
        assertEquals("testuser", result.getName());
        assertEquals(token, result.getCredentials());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testGetAuthentication_SingleRole() {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                new User("singleuser", "pass", authorities), null, authorities);

        String token = jwtTokenProvider.generateToken(auth);
        Authentication result = jwtTokenProvider.getAuthentication(token);

        assertEquals("singleuser", result.getName());
        assertEquals(1, result.getAuthorities().size());
    }

    @Test
    void testGetUsernameFromToken_Success() {
        String token = jwtTokenProvider.generateToken(authentication);
        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals("testuser", username);
    }

    @Test
    void testGetUsernameFromToken_DifferentUser() {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                new User("differentuser", "pass", authorities), null, authorities);

        String token = jwtTokenProvider.generateToken(auth);
        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals("differentuser", username);
    }

    @Test
    void testTokenExpirationTime() {
        long customExpiration = 3600000L;
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", customExpiration);

        Date before = new Date();
        String token = jwtTokenProvider.generateToken(authentication);
        Date after = new Date();

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(testKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        long actualDuration = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();

        assertTrue(Math.abs(actualDuration - customExpiration) < 5000);
    }

    @Test
    void testGetAuthentication_WithInvalidToken_ThrowsException() {
        assertThrows(Exception.class, () -> jwtTokenProvider.getAuthentication("invalid.token"));
    }

    @Test
    void testGetUsernameFromToken_WithInvalidToken_ThrowsException() {
        assertThrows(Exception.class, () -> jwtTokenProvider.getUsernameFromToken("invalid.token"));
    }
}
