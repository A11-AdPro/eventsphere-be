package id.ac.ui.cs.advprog.eventsphere.report.controller;

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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(NotificationControllerTest.TestConfig.class)
public class NotificationControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public NotificationManagementService notificationService() {
            return Mockito.mock(NotificationManagementService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationManagementService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetUserNotifications() throws Exception {
        // Create test data
        UUID userId = UUID.randomUUID();
        UUID notificationId1 = UUID.randomUUID();
        UUID notificationId2 = UUID.randomUUID();

        List<NotificationDTO> notificationDTOs = new ArrayList<>();

        NotificationDTO dto1 = new NotificationDTO();
        dto1.setId(notificationId1);
        dto1.setRecipientId(userId);
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
        when(notificationService.getUserNotifications(userId)).thenReturn(notificationDTOs);

        // Perform request
        mockMvc.perform(get("/api/notifications")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(notificationId1.toString()))
                .andExpect(jsonPath("$[0].recipientId").value(userId.toString()))
                .andExpect(jsonPath("$[0].senderRole").value("ADMIN"))
                .andExpect(jsonPath("$[1].id").value(notificationId2.toString()))
                .andExpect(jsonPath("$[1].recipientId").value(userId.toString()))
                .andExpect(jsonPath("$[1].senderRole").value("SYSTEM"));
    }

    @Test
    public void testGetUnreadUserNotifications() throws Exception {
        // Create test data
        UUID userId = UUID.randomUUID();
        UUID notificationId1 = UUID.randomUUID();

        List<NotificationDTO> notificationDTOs = new ArrayList<>();

        NotificationDTO dto1 = new NotificationDTO();
        dto1.setId(notificationId1);
        dto1.setRecipientId(userId);
        dto1.setSenderRole("ADMIN");
        dto1.setTitle("Title 1");
        dto1.setMessage("Message 1");
        dto1.setRead(false);
        dto1.setType("TYPE_1");
        dto1.setRelatedEntityId(UUID.randomUUID());
        dto1.setCreatedAt(LocalDateTime.now());

        notificationDTOs.add(dto1);

        // Mock service
        when(notificationService.getUnreadUserNotifications(userId)).thenReturn(notificationDTOs);

        // Perform request
        mockMvc.perform(get("/api/notifications/unread")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(notificationId1.toString()))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    public void testCountUnreadNotifications() throws Exception {
        // Create test data
        UUID userId = UUID.randomUUID();
        long unreadCount = 5;

        // Mock service
        when(notificationService.countUnreadNotifications(userId)).thenReturn(unreadCount);

        // Perform request
        mockMvc.perform(get("/api/notifications/count")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(unreadCount));
    }

    @Test
    public void testMarkNotificationAsRead() throws Exception {
        // Create test data
        UUID notificationId = UUID.randomUUID();

        NotificationDTO updatedNotification = new NotificationDTO();
        updatedNotification.setId(notificationId);
        updatedNotification.setRead(true);

        // Mock service
        when(notificationService.markNotificationAsRead(notificationId)).thenReturn(updatedNotification);

        // Perform request
        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notificationId.toString()))
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    public void testMarkAllNotificationsAsRead() throws Exception {
        // Create test data
        UUID userId = UUID.randomUUID();

        // Mock service
        doNothing().when(notificationService).markAllNotificationsAsRead(userId);

        // Perform request
        mockMvc.perform(patch("/api/notifications/read-all")
                        .param("userId", userId.toString()))
                .andExpect(status().isNoContent());

        // Verify service was called
        verify(notificationService).markAllNotificationsAsRead(userId);
    }

    @Test
    public void testDeleteNotification() throws Exception {
        // Create test data
        UUID notificationId = UUID.randomUUID();

        // Mock service
        doNothing().when(notificationService).deleteNotification(notificationId);

        // Perform request
        mockMvc.perform(delete("/api/notifications/{id}", notificationId))
                .andExpect(status().isNoContent());

        // Verify service was called
        verify(notificationService).deleteNotification(notificationId);
    }
}