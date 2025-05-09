package id.ac.ui.cs.advprog.eventsphere.topup.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CurrentUserUtilTest {
    
    private CurrentUserUtil currentUserUtil;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private UserDetails userDetails;
    
    @BeforeEach
    public void setUp() {
        currentUserUtil = new CurrentUserUtil();
    }
    
    @Test
    @DisplayName("Should get email from authenticated UserDetails")
    public void testGetCurrentUserEmailFromUserDetails() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(userDetails.getUsername()).thenReturn("test@example.com");
            
            String result = currentUserUtil.getCurrentUserEmail();
            
            assertEquals("test@example.com", result);
        }
    }
    
    @Test
    @DisplayName("Should get email from authenticated principal string")
    public void testGetCurrentUserEmailFromPrincipalString() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn("test@example.com");
            
            String result = currentUserUtil.getCurrentUserEmail();
            
            assertEquals("test@example.com", result);
        }
    }
    
    @Test
    @DisplayName("Should throw exception when not authenticated")
    public void testGetCurrentUserEmailNotAuthenticated() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(false);
            
            Exception exception = assertThrows(RuntimeException.class, () -> {
                currentUserUtil.getCurrentUserEmail();
            });
            
            assertEquals("User not authenticated", exception.getMessage());
        }
    }
    
    @Test
    @DisplayName("Should throw exception when authentication is null")
    public void testGetCurrentUserEmailNullAuthentication() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);
            
            Exception exception = assertThrows(RuntimeException.class, () -> {
                currentUserUtil.getCurrentUserEmail();
            });
            
            assertEquals("User not authenticated", exception.getMessage());
        }
    }
}