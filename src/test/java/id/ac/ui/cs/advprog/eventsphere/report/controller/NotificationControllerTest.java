package id.ac.ui.cs.advprog.eventsphere.report.controller;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.AuthService;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.NotificationDTO;
import id.ac.ui.cs.advprog.eventsphere.report.service.NotificationManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class NotificationControllerTest {

    @Mock
    private NotificationManagementService notificationService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private NotificationController notificationController;

    private MockMvc mockMvc;
    private User regularUser;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();

        // Setup mock users
        regularUser = new User();
        regularUser.setId(1L);
        regularUser.setEmail("user@example.com");
        regularUser.setRole(Role.ATTENDEE);

        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);
    }

    @Test
    @DisplayName("Mendapatkan semua notifikasi milik pengguna")
    public void testGetUserNotifications() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(regularUser);

        UUID notificationId1 = UUID.randomUUID();
        UUID notificationId2 = UUID.randomUUID();

        List<NotificationDTO> notificationDTOs = new ArrayList<>();
        notificationDTOs.add(createNotificationDTO(notificationId1, "ADMIN", "Title 1", "Message 1", false));
        notificationDTOs.add(createNotificationDTO(notificationId2, "SYSTEM", "Title 2", "Message 2", true));

        when(notificationService.getUserNotificationsByEmail(regularUser.getEmail())).thenReturn(notificationDTOs);

        // Act
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(notificationId1.toString()))
                .andExpect(jsonPath("$[1].id").value(notificationId2.toString()));

        // Assert
        verify(authService).getCurrentUser();
        verify(notificationService).getUserNotificationsByEmail(regularUser.getEmail());
    }

    @Test
    @DisplayName("Mendapatkan notifikasi yang belum dibaca milik pengguna")
    public void testGetUnreadUserNotifications() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(regularUser);

        UUID notificationId1 = UUID.randomUUID();

        List<NotificationDTO> notificationDTOs = new ArrayList<>();
        notificationDTOs.add(createNotificationDTO(notificationId1, "ADMIN", "Title 1", "Message 1", false));

        when(notificationService.getUnreadUserNotificationsByEmail(regularUser.getEmail())).thenReturn(notificationDTOs);

        // Act
        mockMvc.perform(get("/api/notifications/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].read").value(false));

        // Assert
        verify(authService).getCurrentUser();
        verify(notificationService).getUnreadUserNotificationsByEmail(regularUser.getEmail());
    }

    @Test
    @DisplayName("Menghitung jumlah notifikasi yang belum dibaca milik pengguna")
    public void testCountUnreadNotifications() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(regularUser);

        long unreadCount = 5;
        when(notificationService.countUnreadNotificationsByEmail(regularUser.getEmail())).thenReturn(unreadCount);

        // Act
        mockMvc.perform(get("/api/notifications/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(unreadCount));

        // Assert
        verify(authService).getCurrentUser();
        verify(notificationService).countUnreadNotificationsByEmail(regularUser.getEmail());
    }

    @Test
    @DisplayName("Menandai notifikasi sebagai dibaca")
    public void testMarkNotificationAsRead() throws Exception {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        NotificationDTO updatedNotification = new NotificationDTO();
        updatedNotification.setId(notificationId);
        updatedNotification.setRead(true);

        when(notificationService.markNotificationAsRead(notificationId)).thenReturn(updatedNotification);

        // Act
        mockMvc.perform(patch("/api/notifications/{id}/read", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));

        // Assert
        verify(notificationService).markNotificationAsRead(notificationId);
    }

    @Test
    @DisplayName("Menandai semua notifikasi sebagai dibaca")
    public void testMarkAllNotificationsAsRead() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(regularUser);

        doNothing().when(notificationService).markAllNotificationsAsReadByEmail(regularUser.getEmail());

        // Act
        mockMvc.perform(patch("/api/notifications/read-all"))
                .andExpect(status().isNoContent());

        // Assert
        verify(authService).getCurrentUser();
        verify(notificationService).markAllNotificationsAsReadByEmail(regularUser.getEmail());
    }

    @Test
    @DisplayName("Menghapus notifikasi")
    public void testDeleteNotification() throws Exception {
        // Arrange
        UUID notificationId = UUID.randomUUID();

        doNothing().when(notificationService).deleteNotification(notificationId);

        // Act
        mockMvc.perform(delete("/api/notifications/{id}", notificationId))
                .andExpect(status().isNoContent());

        // Assert
        verify(notificationService).deleteNotification(notificationId);
    }

    @Test
    @DisplayName("Mendapatkan notifikasi berdasarkan ID pengguna")
    public void testGetUserNotificationsById() throws Exception {
        // Arrange
        Long targetUserId = 3L;
        UUID notificationId1 = UUID.randomUUID();
        UUID notificationId2 = UUID.randomUUID();

        List<NotificationDTO> notificationDTOs = new ArrayList<>();
        notificationDTOs.add(createNotificationDTO(notificationId1, "SYSTEM", "Title 1", "Message 1", false));
        notificationDTOs.add(createNotificationDTO(notificationId2, "ADMIN", "Title 2", "Message 2", true));

        when(notificationService.getUserNotifications(targetUserId)).thenReturn(notificationDTOs);

        // Act
        mockMvc.perform(get("/api/notifications/by-id")
                        .param("userId", targetUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(notificationId1.toString()))
                .andExpect(jsonPath("$[1].id").value(notificationId2.toString()));

        // Assert
        verify(notificationService).getUserNotifications(targetUserId);
    }

    private NotificationDTO createNotificationDTO(UUID notificationId, String senderRole, String title, String message, boolean isRead) {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setId(notificationId);
        notificationDTO.setRecipientId(1L);
        notificationDTO.setRecipientEmail("user@example.com");
        notificationDTO.setSenderRole(senderRole);
        notificationDTO.setTitle(title);
        notificationDTO.setMessage(message);
        notificationDTO.setRead(isRead);
        notificationDTO.setType("TYPE_1");
        notificationDTO.setRelatedEntityId(UUID.randomUUID());
        notificationDTO.setCreatedAt(LocalDateTime.now());
        return notificationDTO;
    }
}
