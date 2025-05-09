package id.ac.ui.cs.advprog.eventsphere.report.controller;

import id.ac.ui.cs.advprog.eventsphere.report.dto.response.NotificationDTO;
import id.ac.ui.cs.advprog.eventsphere.report.service.NotificationManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationManagementService notificationService;

    @Autowired
    public NotificationController(NotificationManagementService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@RequestParam UUID userId) {
        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadUserNotifications(@RequestParam UUID userId) {
        List<NotificationDTO> notifications = notificationService.getUnreadUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countUnreadNotifications(@RequestParam UUID userId) {
        long count = notificationService.countUnreadNotifications(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDTO> markNotificationAsRead(@PathVariable UUID id) {
        NotificationDTO notification = notificationService.markNotificationAsRead(id);
        return ResponseEntity.ok(notification);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllNotificationsAsRead(@RequestParam UUID userId) {
        notificationService.markAllNotificationsAsRead(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}