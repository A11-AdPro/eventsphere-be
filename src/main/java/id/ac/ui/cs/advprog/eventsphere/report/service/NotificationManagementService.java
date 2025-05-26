package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.dto.response.NotificationDTO;
import id.ac.ui.cs.advprog.eventsphere.report.model.Notification;
import id.ac.ui.cs.advprog.eventsphere.report.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationManagementService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationManagementService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Fungsi ini digunakan untuk mendapatkan semua notifikasi yang diterima oleh pengguna berdasarkan ID pengguna.
    public List<NotificationDTO> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
        return convertToDTOList(notifications);
    }

    // Fungsi ini digunakan untuk mendapatkan semua notifikasi yang diterima oleh pengguna berdasarkan email pengguna.
    public List<NotificationDTO> getUserNotificationsByEmail(String email) {
        List<Notification> notifications = notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);
        return convertToDTOList(notifications);
    }

    // Fungsi ini digunakan untuk mendapatkan semua notifikasi yang belum dibaca oleh pengguna berdasarkan ID pengguna.
    public List<NotificationDTO> getUnreadUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false);
        return convertToDTOList(notifications);
    }

    // Fungsi ini digunakan untuk mendapatkan semua notifikasi yang belum dibaca oleh pengguna berdasarkan email pengguna.
    public List<NotificationDTO> getUnreadUserNotificationsByEmail(String email) {
        List<Notification> notifications = notificationRepository.findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false);
        return convertToDTOList(notifications);
    }

    // Fungsi ini digunakan untuk menghitung jumlah notifikasi yang belum dibaca berdasarkan ID pengguna.
    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByRecipientIdAndRead(userId, false);
    }

    // Fungsi ini digunakan untuk menghitung jumlah notifikasi yang belum dibaca berdasarkan email pengguna.
    public long countUnreadNotificationsByEmail(String email) {
        return notificationRepository.countByRecipientEmailAndRead(email, false);
    }

    // Fungsi ini digunakan untuk menandai notifikasi sebagai sudah dibaca berdasarkan ID notifikasi.
    public NotificationDTO markNotificationAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with id: " + notificationId));

        notification.markAsRead();
        return convertToDTO(notificationRepository.save(notification));
    }

    // Fungsi ini digunakan untuk menandai semua notifikasi yang belum dibaca sebagai sudah dibaca berdasarkan ID pengguna.
    public void markAllNotificationsAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false);

        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
            notificationRepository.save(notification);
        }
    }

    // Fungsi ini digunakan untuk menandai semua notifikasi yang belum dibaca sebagai sudah dibaca berdasarkan email pengguna.
    public void markAllNotificationsAsReadByEmail(String email) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false);

        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
            notificationRepository.save(notification);
        }
    }

    // Fungsi ini digunakan untuk menghapus notifikasi berdasarkan ID notifikasi.
    public void deleteNotification(UUID notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new EntityNotFoundException("Notification not found with id: " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }

    // Fungsi ini digunakan untuk mengonversi objek Notification menjadi objek NotificationDTO.
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setRecipientId(notification.getRecipientId());
        dto.setRecipientEmail(notification.getRecipientEmail());
        dto.setSenderRole(notification.getSenderRole());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setType(notification.getType());
        dto.setRelatedEntityId(notification.getRelatedEntityId());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }

    // Fungsi ini digunakan untuk mengonversi daftar objek Notification menjadi daftar objek NotificationDTO.
    private List<NotificationDTO> convertToDTOList(List<Notification> notifications) {
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}