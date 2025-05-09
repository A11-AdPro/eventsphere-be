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

    public List<NotificationDTO> getUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
        return convertToDTOList(notifications);
    }

    public List<NotificationDTO> getUserNotificationsByEmail(String email) {
        List<Notification> notifications = notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);
        return convertToDTOList(notifications);
    }

    public List<NotificationDTO> getUnreadUserNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false);
        return convertToDTOList(notifications);
    }

    public List<NotificationDTO> getUnreadUserNotificationsByEmail(String email) {
        List<Notification> notifications = notificationRepository.findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false);
        return convertToDTOList(notifications);
    }

    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByRecipientIdAndRead(userId, false);
    }

    public long countUnreadNotificationsByEmail(String email) {
        return notificationRepository.countByRecipientEmailAndRead(email, false);
    }

    public NotificationDTO markNotificationAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with id: " + notificationId));

        notification.markAsRead();
        return convertToDTO(notificationRepository.save(notification));
    }

    public void markAllNotificationsAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false);

        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
            notificationRepository.save(notification);
        }
    }

    public void markAllNotificationsAsReadByEmail(String email) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false);

        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
            notificationRepository.save(notification);
        }
    }

    public void deleteNotification(UUID notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new EntityNotFoundException("Notification not found with id: " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }

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

    private List<NotificationDTO> convertToDTOList(List<Notification> notifications) {
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}