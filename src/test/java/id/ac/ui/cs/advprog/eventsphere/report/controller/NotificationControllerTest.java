package id.ac.ui.cs.advprog.eventsphere.report.controller;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.AuthService;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.NotificationDTO;
import id.ac.ui.cs.advprog.eventsphere.report.service.NotificationManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(NotificationControllerTest.TestConfig.class)
@ActiveProfiles("test")
public class NotificationControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public NotificationManagementService notificationService() {
            return Mockito.mock(NotificationManagementService.class);
        }

        @Bean
        public AuthService authService() {
            AuthService mockAuthService = Mockito.mock(AuthService.class);
            User mockUser = new User();
            mockUser.setId(1L);
            mockUser.setEmail("user@example.com");
            when(mockAuthService.getCurrentUser()).thenReturn(mockUser);
            return mockAuthService;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationManagementService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testGetUserNotifications() throws Exception {
        // Create test data
        Long userId = 1L;
        String userEmail = "user@example.com";
        UUID notificationId1 = UUID.randomUUID();
        UUID notificationId2 = UUID.randomUUID();

        List<NotificationDTO> notificationDTOs = new ArrayList<>();

        NotificationDTO dto1 = new NotificationDTO();
        dto1.setId(notificationId1);
        dto1.setRecipientId(userId);
        dto1.setRecipientEmail(userEmail);
        dto1.setSenderRole("ADMIN");
        dto1.setTitle("Title 1");
        dto1.setMessage("Message 1");
        dto1.setRead(false);
        dto1.setType("TYPE_1");
        dto1.setRelatedEntityId(UUID.randomUUID());
        dto1.setCreatedAt(LocalDateTime.now());

        NotificationDTO dto2 = new NotificationDTO();
        dto2.setId(notificationId2);
        dto2.setRecipientId(userId);
        dto2.setRecipientEmail(userEmail);
        dto2.setSenderRole("SYSTEM");
        dto2.setTitle("Title 2");
        dto2.setMessage("Message 2");
        dto2.setRead(true);
        dto2.setType("TYPE_2");
        dto2.setRelatedEntityId(UUID.randomUUID());
        dto2.setCreatedAt(LocalDateTime.now());

        notificationDTOs.add(dto1);
        notificationDTOs.add(dto2);

        // Mock service
        when(notificationService.getUserNotificationsByEmail(userEmail)).thenReturn(notificationDTOs);

        // Perform request
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(notificationId1.toString()))
                .andExpect(jsonPath("$[0].recipientId").value(userId))
                .andExpect(jsonPath("$[0].recipientEmail").value(userEmail))
                .andExpect(jsonPath("$[0].senderRole").value("ADMIN"))
                .andExpect(jsonPath("$[1].id").value(notificationId2.toString()))
                .andExpect(jsonPath("$[1].recipientId").value(userId))
                .andExpect(jsonPath("$[1].recipientEmail").value(userEmail))
                .andExpect(jsonPath("$[1].senderRole").value("SYSTEM"));
    }

    @Test
    @WithMockUser
    public void testGetUnreadUserNotifications() throws Exception {
        // Create test data
        Long userId = 1L;
        String userEmail = "user@example.com";
        UUID notificationId1 = UUID.randomUUID();

        List<NotificationDTO> notificationDTOs = new ArrayList<>();

        NotificationDTO dto1 = new NotificationDTO();
        dto1.setId(notificationId1);
        dto1.setRecipientId(userId);
        dto1.setRecipientEmail(userEmail);
        dto1.setSenderRole("ADMIN");
        dto1.setTitle("Title 1");
        dto1.setMessage("Message 1");
        dto1.setRead(false);
        dto1.setType("TYPE_1");
        dto1.setRelatedEntityId(UUID.randomUUID());
        dto1.setCreatedAt(LocalDateTime.now());

        notificationDTOs.add(dto1);

        // Mock service
        when(notificationService.getUnreadUserNotificationsByEmail(userEmail)).thenReturn(notificationDTOs);

        // Perform request
        mockMvc.perform(get("/api/notifications/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(notificationId1.toString()))
                .andExpect(jsonPath("$[0].recipientId").value(userId))
                .andExpect(jsonPath("$[0].recipientEmail").value(userEmail))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    @WithMockUser
    public void testCountUnreadNotifications() throws Exception {
        // Create test data
        String userEmail = "user@example.com";
        long unreadCount = 5;

        // Mock service
        when(notificationService.countUnreadNotificationsByEmail(userEmail)).thenReturn(unreadCount);

        // Perform request
        mockMvc.perform(get("/api/notifications/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(unreadCount));
    }

    @Test
    @WithMockUser
    public void testMarkNotificationAsRead() throws Exception {
        // Create test data
        UUID notificationId = UUID.randomUUID();
        Long recipientId = 1L;
        String recipientEmail = "user@example.com";

        NotificationDTO updatedNotification = new NotificationDTO();
        updatedNotification.setId(notificationId);
        updatedNotification.setRecipientId(recipientId);
        updatedNotification.setRecipientEmail(recipientEmail);
        updatedNotification.setRead(true);

        // Mock service
        when(notificationService.markNotificationAsRead(notificationId)).thenReturn(updatedNotification);

        // Perform request with CSRF token
        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notificationId.toString()))
                .andExpect(jsonPath("$.recipientId").value(recipientId))
                .andExpect(jsonPath("$.recipientEmail").value(recipientEmail))
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    @WithMockUser
    public void testMarkAllNotificationsAsRead() throws Exception {
        // Create test data
        String userEmail = "user@example.com";

        // Mock service
        doNothing().when(notificationService).markAllNotificationsAsReadByEmail(userEmail);

        // Perform request with CSRF token
        mockMvc.perform(patch("/api/notifications/read-all")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Verify service was called
        verify(notificationService).markAllNotificationsAsReadByEmail(userEmail);
    }

    @Test
    @WithMockUser
    public void testDeleteNotification() throws Exception {
        // Create test data
        UUID notificationId = UUID.randomUUID();

        // Mock service
        doNothing().when(notificationService).deleteNotification(notificationId);

        // Perform request with CSRF token
        mockMvc.perform(delete("/api/notifications/{id}", notificationId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Verify service was called
        verify(notificationService).deleteNotification(notificationId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserNotificationsById() throws Exception {
        // Create test data
        Long targetUserId = 2L;  // ID of user whose notifications we're retrieving (different from authenticated user)
        UUID notificationId1 = UUID.randomUUID();
        UUID notificationId2 = UUID.randomUUID();

        List<NotificationDTO> notificationDTOs = new ArrayList<>();

        NotificationDTO dto1 = new NotificationDTO();
        dto1.setId(notificationId1);
        dto1.setRecipientId(targetUserId);
        dto1.setRecipientEmail("target@example.com");
        dto1.setSenderRole("SYSTEM");
        dto1.setTitle("Title 1");
        dto1.setMessage("Message 1");
        dto1.setRead(false);
        dto1.setType("TYPE_1");
        dto1.setRelatedEntityId(UUID.randomUUID());
        dto1.setCreatedAt(LocalDateTime.now());

        NotificationDTO dto2 = new NotificationDTO();
        dto2.setId(notificationId2);
        dto2.setRecipientId(targetUserId);
        dto2.setRecipientEmail("target@example.com");
        dto2.setSenderRole("ADMIN");
        dto2.setTitle("Title 2");
        dto2.setMessage("Message 2");
        dto2.setRead(true);
        dto2.setType("TYPE_2");
        dto2.setRelatedEntityId(UUID.randomUUID());
        dto2.setCreatedAt(LocalDateTime.now());

        notificationDTOs.add(dto1);
        notificationDTOs.add(dto2);

        // Mock service
        when(notificationService.getUserNotifications(targetUserId)).thenReturn(notificationDTOs);

        // Perform request
        mockMvc.perform(get("/api/notifications/by-id")
                        .param("userId", targetUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(notificationId1.toString()))
                .andExpect(jsonPath("$[0].recipientId").value(targetUserId))
                .andExpect(jsonPath("$[0].recipientEmail").value("target@example.com"))
                .andExpect(jsonPath("$[0].senderRole").value("SYSTEM"))
                .andExpect(jsonPath("$[1].id").value(notificationId2.toString()))
                .andExpect(jsonPath("$[1].recipientId").value(targetUserId))
                .andExpect(jsonPath("$[1].recipientEmail").value("target@example.com"))
                .andExpect(jsonPath("$[1].senderRole").value("ADMIN"));

        // Verify service method was called with correct parameter
        verify(notificationService).getUserNotifications(targetUserId);
    }
}