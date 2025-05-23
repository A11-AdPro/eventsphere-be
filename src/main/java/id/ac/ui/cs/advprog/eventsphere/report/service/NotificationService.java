package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.model.Notification;
import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.observer.ReportObserver;
import id.ac.ui.cs.advprog.eventsphere.report.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService implements ReportObserver {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    @Override
    public void onStatusChanged(Report report, ReportStatus oldStatus, ReportStatus newStatus) {
        // Create notification for the report owner
        String title = "Report Status Updated";
        String message = "Your report status has been updated from " + oldStatus.getDisplayName() +
                " to " + newStatus.getDisplayName() + ".\n\n" +
                "Category: " + report.getCategory().getDisplayName() + "\n" +
                "Description: " + report.getDescription();

        Notification notification = new Notification(
                report.getUserId(),
                report.getUserEmail(),
                "SYSTEM",
                title,
                message,
                "STATUS_UPDATE",
                report.getId()
        );

        notificationRepository.save(notification);
    }

    @Override
    public void onResponseAdded(Report report, ReportResponse response) {
        // Create notification for the report owner
        String title = "New Response to Your Report";
        String message = "A new response has been added to your report:\n\n" +
                "From: " + response.getResponderRole() + "\n" +
                "Message: " + response.getMessage() + "\n\n" +
                "Report Details:\n" +
                "Category: " + report.getCategory().getDisplayName() + "\n" +
                "Status: " + report.getStatus().getDisplayName();

        Notification notification = new Notification(
                report.getUserId(),
                report.getUserEmail(),
                response.getResponderRole(),
                title,
                message,
                "NEW_RESPONSE",
                report.getId()
        );

        notificationRepository.save(notification);
    }

    public void notifyNewReport(Report report) {
        // Get admin IDs
        List<Long> adminIds = userService.getAdminIds();

        // Create notification for each admin
        for (Long adminId : adminIds) {
            String adminEmail = userService.getUserEmail(adminId);
            String title = "New Report Submitted";
            String message = "A new report has been submitted:\n\n" +
                    "Category: " + report.getCategory().getDisplayName() + "\n" +
                    "Description: " + report.getDescription() + "\n\n" +
                    "Please review this report in the admin dashboard.";

            Notification notification = new Notification(
                    adminId,
                    adminEmail,
                    "SYSTEM",
                    title,
                    message,
                    "NEW_REPORT",
                    report.getId()
            );

            notificationRepository.save(notification);
        }
    }

    public void notifyOrganizerOfReport(Report report, UUID eventId) {
        // Get organizer IDs for the event
        List<Long> organizerIds = userService.getOrganizerIds(eventId);

        // Create notification for each organizer
        for (Long organizerId : organizerIds) {
            String organizerEmail = userService.getUserEmail(organizerId);
            String title = "New Report Related to Your Event";
            String message = "A new report has been submitted for an event you manage:\n\n" +
                    "Category: " + report.getCategory().getDisplayName() + "\n" +
                    "Description: " + report.getDescription() + "\n\n" +
                    "Please review this report in your organizer dashboard.";

            Notification notification = new Notification(
                    organizerId,
                    organizerEmail,
                    "SYSTEM",
                    title,
                    message,
                    "NEW_REPORT",
                    report.getId()
            );

            notificationRepository.save(notification);
        }
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUserNotificationsByEmail(String email) {
        return notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(email);
    }

    public List<Notification> getUnreadUserNotifications(Long userId) {
        return notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(userId, false);
    }

    public List<Notification> getUnreadUserNotificationsByEmail(String email) {
        return notificationRepository.findByRecipientEmailAndReadOrderByCreatedAtDesc(email, false);
    }

    public long countUnreadNotifications(Long userId) {
        return notificationRepository.countByRecipientIdAndRead(userId, false);
    }

    public long countUnreadNotificationsByEmail(String email) {
        return notificationRepository.countByRecipientEmailAndRead(email, false);
    }

    public Notification markNotificationAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        notification.markAsRead();
        return notificationRepository.save(notification);
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
}