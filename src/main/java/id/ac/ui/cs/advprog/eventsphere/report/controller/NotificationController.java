package id.ac.ui.cs.advprog.eventsphere.report.controller;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.AuthService;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.NotificationDTO;
import id.ac.ui.cs.advprog.eventsphere.report.service.NotificationManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationManagementService notificationService;
    private final AuthService authService;

    @Autowired
    public NotificationController(NotificationManagementService notificationService, AuthService authService) {
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications() {
        User currentUser = authService.getCurrentUser();
        List<NotificationDTO> notifications = notificationService.getUserNotificationsByEmail(currentUser.getEmail());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/by-id")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDTO>> getUserNotificationsById(@RequestParam Long userId) {
        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDTO>> getUnreadUserNotifications() {
        User currentUser = authService.getCurrentUser();
        List<NotificationDTO> notifications = notificationService.getUnreadUserNotificationsByEmail(currentUser.getEmail());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> countUnreadNotifications() {
        User currentUser = authService.getCurrentUser();
        long count = notificationService.countUnreadNotificationsByEmail(currentUser.getEmail());
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationDTO> markNotificationAsRead(@PathVariable UUID id) {
        NotificationDTO notification = notificationService.markNotificationAsRead(id);
        return ResponseEntity.ok(notification);
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllNotificationsAsRead() {
        User currentUser = authService.getCurrentUser();
        notificationService.markAllNotificationsAsReadByEmail(currentUser.getEmail());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
        notificationService.deleteNotification(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}