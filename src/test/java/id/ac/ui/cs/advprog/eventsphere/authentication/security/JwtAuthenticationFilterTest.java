package id.ac.ui.cs.advprog.eventsphere.authentication.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(tokenProvider);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void doFilterInternal_WithValidJwtToken_ShouldSetAuthentication() throws ServletException, IOException {
        // Arrange
        String validToken = "valid.jwt.token";
        String bearerToken = "Bearer " + validToken;
        
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(tokenProvider.validateToken(validToken)).thenReturn(true);
        when(tokenProvider.getAuthentication(validToken)).thenReturn(authentication);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider).validateToken(validToken);
        verify(tokenProvider).getAuthentication(validToken);
        verify(securityContext).setAuthentication(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithInvalidJwtToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        String invalidToken = "invalid.jwt.token";
        String bearerToken = "Bearer " + invalidToken;
        
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(tokenProvider.validateToken(invalidToken)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider).validateToken(invalidToken);
        verify(tokenProvider, never()).getAuthentication(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNoAuthorizationHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, never()).validateToken(anyString());
        verify(tokenProvider, never()).getAuthentication(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithEmptyAuthorizationHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, never()).validateToken(anyString());
        verify(tokenProvider, never()).getAuthentication(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithWhitespaceAuthorizationHeader_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("   ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, never()).validateToken(anyString());
        verify(tokenProvider, never()).getAuthentication(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNonBearerToken_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, never()).validateToken(anyString());
        verify(tokenProvider, never()).getAuthentication(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithBearerTokenWithoutSpace_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearertoken");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, never()).validateToken(anyString());
        verify(tokenProvider, never()).getAuthentication(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithOnlyBearerPrefix_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, never()).validateToken(anyString());
        verify(tokenProvider, never()).getAuthentication(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithBearerPrefixAndWhitespace_ShouldNotSetAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer    ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, never()).validateToken(anyString());
        verify(tokenProvider, never()).getAuthentication(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenValidateTokenThrowsException_ShouldContinueFilterChain() throws ServletException, IOException {
        // Arrange
        String validToken = "valid.jwt.token";
        String bearerToken = "Bearer " + validToken;
        
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(tokenProvider.validateToken(validToken)).thenThrow(new RuntimeException("Token validation failed"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider).validateToken(validToken);
        verify(tokenProvider, never()).getAuthentication(anyString());
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenGetAuthenticationThrowsException_ShouldContinueFilterChain() throws ServletException, IOException {
        // Arrange
        String validToken = "valid.jwt.token";
        String bearerToken = "Bearer " + validToken;
        
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(tokenProvider.validateToken(validToken)).thenReturn(true);
        when(tokenProvider.getAuthentication(validToken)).thenThrow(new RuntimeException("Authentication failed"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider).validateToken(validToken);
        verify(tokenProvider).getAuthentication(validToken);
        verify(securityContext, never()).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenSetAuthenticationThrowsException_ShouldContinueFilterChain() throws ServletException, IOException {
        // Arrange
        String validToken = "valid.jwt.token";
        String bearerToken = "Bearer " + validToken;
        
        when(request.getHeader("Authorization")).thenReturn(bearerToken);
        when(tokenProvider.validateToken(validToken)).thenReturn(true);
        when(tokenProvider.getAuthentication(validToken)).thenReturn(authentication);
        doThrow(new RuntimeException("Security context error")).when(securityContext).setAuthentication(authentication);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider).validateToken(validToken);
        verify(tokenProvider).getAuthentication(validToken);
        verify(securityContext).setAuthentication(authentication);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_AlwaysCallsFilterChain_EvenWhenExceptionOccurs() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenThrow(new RuntimeException("Request error"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void getJwtFromRequest_WithValidBearerToken_ShouldReturnToken() throws Exception {
        // This test uses reflection to test the private method for completeness
        // Arrange
        String token = "valid.jwt.token";
        String bearerToken = "Bearer " + token;
        when(request.getHeader("Authorization")).thenReturn(bearerToken);

        // Act
        java.lang.reflect.Method method = JwtAuthenticationFilter.class.getDeclaredMethod("getJwtFromRequest", HttpServletRequest.class);
        method.setAccessible(true);
        String result = (String) method.invoke(jwtAuthenticationFilter, request);

        // Assert
        assertEquals(token, result);
    }

    @Test
    void getJwtFromRequest_WithNullHeader_ShouldReturnNull() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        java.lang.reflect.Method method = JwtAuthenticationFilter.class.getDeclaredMethod("getJwtFromRequest", HttpServletRequest.class);
        method.setAccessible(true);
        String result = (String) method.invoke(jwtAuthenticationFilter, request);

        // Assert
        assertNull(result);
    }

    @Test
    void getJwtFromRequest_WithEmptyHeader_ShouldReturnNull() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("");

        // Act
        java.lang.reflect.Method method = JwtAuthenticationFilter.class.getDeclaredMethod("getJwtFromRequest", HttpServletRequest.class);
        method.setAccessible(true);
        String result = (String) method.invoke(jwtAuthenticationFilter, request);

        // Assert
        assertNull(result);
    }

    @Test
    void getJwtFromRequest_WithNonBearerHeader_ShouldReturnNull() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Basic token123");

        // Act
        java.lang.reflect.Method method = JwtAuthenticationFilter.class.getDeclaredMethod("getJwtFromRequest", HttpServletRequest.class);
        method.setAccessible(true);
        String result = (String) method.invoke(jwtAuthenticationFilter, request);

        // Assert
        assertNull(result);
    }
}