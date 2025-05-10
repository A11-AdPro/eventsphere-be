package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.dto.response.NotificationDTO;
import id.ac.ui.cs.advprog.eventsphere.report.model.Notification;
import id.ac.ui.cs.advprog.eventsphere.report.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NotificationManagementServiceTest {

    private NotificationRepository notificationRepository;
    private NotificationManagementService notificationService;

    @BeforeEach
    public void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        notificationService = new NotificationManagementService(notificationRepository);
    }

    @Test
    @DisplayName("Mendapatkan daftar notifikasi pengguna berdasarkan ID")
    public void testGetUserNotifications() {
        // Arrange
        Long userId = 1L;
        Notification notification1 = new Notification(userId, "user@example.com", "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        Notification notification2 = new Notification(userId, "user@example.com", "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());

        List<Notification> notificationList = Arrays.asList(notification1, notification2);
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)).thenReturn(notificationList);

        // Act
        List<NotificationDTO> result = notificationService.getUserNotifications(userId);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Title 1", result.get(0).getTitle());
        assertEquals("Title 2", result.get(1).getTitle());
        verify(notificationRepository).findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("Mendapatkan daftar notifikasi pengguna berdasarkan email")
    public void testGetUserNotificationsByEmail() {
        // Arrange
        String email = "user@example.com";
        Notification notification1 = new Notification(1L, email, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        Notification notification2 = new Notification(2L, email, "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());

        List<Notification> notificationList = Arrays.asList(notification1, notification2);
        when(notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email)).thenReturn(notificationList);

        // Act
        List<NotificationDTO> result = notificationService.getUserNotificationsByEmail(email);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Title 1", result.get(0).getTitle());
        assertEquals("Title 2", result.get(1).getTitle());
        verify(notificationRepository).findByRecipientEmailOrderByCreatedAtDesc(email);
    }

    @Test
    @DisplayName("Mendapatkan daftar notifikasi yang belum dibaca berdasarkan ID pengguna")
    public void testGetUnreadUserNotifications() {
        // Arrange
        Long userId = 1L;
        Notification notification1 = new Notification(userId, "user@example.com", "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        Notification notification2 = new Notification(userId, "user@example.com", "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        notification2.setRead(false);

        List<Notification> notificationList = Arrays.asList(notification1, notification2);
        when(notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false)).thenReturn(notificationList);

        // Act
        List<NotificationDTO> result = notificationService.getUnreadUserNotifications(userId);

        // Assert
        assertEquals(2, result.size());
        assertFalse(result.get(0).isRead());
        assertFalse(result.get(1).isRead());
        verify(notificationRepository).findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false);
    }

    @Test
    @DisplayName("Mendapatkan daftar notifikasi yang belum dibaca berdasarkan email pengguna")
    public void testGetUnreadUserNotificationsByEmail() {
        // Arrange
        String email = "user@example.com";
        Notification notification1 = new Notification(1L, email, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        Notification notification2 = new Notification(2L, email, "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        notification2.setRead(false);

        List<Notification> notificationList = Arrays.asList(notification1, notification2);
        when(notificationRepository.findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false)).thenReturn(notificationList);

        // Act
        List<NotificationDTO> result = notificationService.getUnreadUserNotificationsByEmail(email);

        // Assert
        assertEquals(2, result.size());
        assertFalse(result.get(0).isRead());
        assertFalse(result.get(1).isRead());
        verify(notificationRepository).findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false);
    }

    @Test
    @DisplayName("Menghitung jumlah notifikasi yang belum dibaca berdasarkan ID pengguna")
    public void testCountUnreadNotifications() {
        // Arrange
        Long userId = 1L;
        long expectedCount = 5;
        when(notificationRepository.countByRecipientIdAndRead(userId, false)).thenReturn(expectedCount);

        // Act
        long result = notificationService.countUnreadNotifications(userId);

        // Assert
        assertEquals(expectedCount, result);
        verify(notificationRepository).countByRecipientIdAndRead(userId, false);
    }

    @Test
    @DisplayName("Menghitung jumlah notifikasi yang belum dibaca berdasarkan email pengguna")
    public void testCountUnreadNotificationsByEmail() {
        // Arrange
        String email = "user@example.com";
        long expectedCount = 5;
        when(notificationRepository.countByRecipientEmailAndRead(email, false)).thenReturn(expectedCount);

        // Act
        long result = notificationService.countUnreadNotificationsByEmail(email);

        // Assert
        assertEquals(expectedCount, result);
        verify(notificationRepository).countByRecipientEmailAndRead(email, false);
    }

    @Test
    @DisplayName("Menandai notifikasi sebagai telah dibaca")
    public void testMarkNotificationAsRead() {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        Notification notification = new Notification(
                1L, "user@example.com", "ADMIN", "Title", "Message", "TYPE", UUID.randomUUID());
        notification.setId(notificationId);
        notification.setRead(false);

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        NotificationDTO result = notificationService.markNotificationAsRead(notificationId);

        // Assert
        assertTrue(result.isRead());
        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("Menangani error saat notifikasi yang akan ditandai tidak ditemukan")
    public void testMarkNotificationAsReadNotFound() {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            notificationService.markNotificationAsRead(notificationId);
        });

        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Menandai semua notifikasi pengguna sebagai telah dibaca berdasarkan ID")
    public void testMarkAllNotificationsAsRead() {
        // Arrange
        Long userId = 1L;
        Notification notification1 = new Notification(userId, "user@example.com", "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        Notification notification2 = new Notification(userId, "user@example.com", "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        notification2.setRead(false);

        List<Notification> notificationList = Arrays.asList(notification1, notification2);
        when(notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false)).thenReturn(notificationList);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        notificationService.markAllNotificationsAsRead(userId);

        // Assert
        verify(notificationRepository).findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false);
        verify(notificationRepository, times(2)).save(any(Notification.class));
        assertTrue(notification1.isRead());
        assertTrue(notification2.isRead());
    }

    @Test
    @DisplayName("Menandai semua notifikasi pengguna sebagai telah dibaca berdasarkan email")
    public void testMarkAllNotificationsAsReadByEmail() {
        // Arrange
        String email = "user@example.com";
        Notification notification1 = new Notification(1L, email, "ADMIN", "Title 1", "Message 1", "TYPE_1", UUID.randomUUID());
        notification1.setRead(false);

        Notification notification2 = new Notification(2L, email, "SYSTEM", "Title 2", "Message 2", "TYPE_2", UUID.randomUUID());
        notification2.setRead(false);

        List<Notification> notificationList = Arrays.asList(notification1, notification2);
        when(notificationRepository.findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false)).thenReturn(notificationList);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        notificationService.markAllNotificationsAsReadByEmail(email);

        // Assert
        verify(notificationRepository).findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false);
        verify(notificationRepository, times(2)).save(any(Notification.class));
        assertTrue(notification1.isRead());
        assertTrue(notification2.isRead());
    }

    @Test
    @DisplayName("Menghapus notifikasi berdasarkan ID")
    public void testDeleteNotification() {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.existsById(notificationId)).thenReturn(true);
        doNothing().when(notificationRepository).deleteById(notificationId);

        // Act
        notificationService.deleteNotification(notificationId);

        // Assert
        verify(notificationRepository).existsById(notificationId);
        verify(notificationRepository).deleteById(notificationId);
    }

    @Test
    @DisplayName("Menangani error saat notifikasi yang akan dihapus tidak ditemukan")
    public void testDeleteNotificationNotFound() {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.existsById(notificationId)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            notificationService.deleteNotification(notificationId);
        });

        verify(notificationRepository).existsById(notificationId);
        verify(notificationRepository, never()).deleteById(any(UUID.class));
    }
}