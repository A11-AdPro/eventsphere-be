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
    public NotificationService(NotificationRepository notificationRepository,
                               UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    @Override
    public void onStatusChanged(Report report, ReportStatus oldStatus, ReportStatus newStatus) {
        // Create notification for the report owner
        String title = "Report Status Updated";
        String message = String.format(
                "Your report status has been updated from %s to %s.\n\n" +
                        "Category: %s\n" +
                        "Description: %s",
                oldStatus.getDisplayName(),
                newStatus.getDisplayName(),
                report.getCategory().getDisplayName(),
                report.getDescription()
        );

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
        // Check if the response is from the report owner (attendee)
        boolean isFromAttendee = report.getUserEmail().equals(response.getResponderEmail()) ||
                report.getUserId().equals(response.getResponderId());

        // Check if the response is from admin/organizer
        boolean isFromStaff = "ADMIN".equals(response.getResponderRole()) ||
                "ORGANIZER".equals(response.getResponderRole());

        // Notify admins when attendee responds
        if (isFromAttendee) {
            notifyAdminsOfResponse(report, response);
        }
        // Notify attendee when admin/organizer responds
        else if (isFromStaff) {
            notifyAttendeeOfResponse(report, response);
        }
    }

    private void notifyAdminsOfResponse(Report report, ReportResponse response) {
        List<Long> adminIds = userService.getAdminIds();

        for (Long adminId : adminIds) {
            String adminEmail = userService.getUserEmail(adminId);
            String title = "New Response to Report";
            String message = String.format(
                    "The attendee has responded to report #%s:\n\n" +
                            "Message: %s\n\n" +
                            "Category: %s\n" +
                            "Status: %s",
                    report.getId().toString().substring(0, 8),
                    response.getMessage(),
                    report.getCategory().getDisplayName(),
                    report.getStatus().getDisplayName()
            );

            Notification notification = new Notification(
                    adminId,
                    adminEmail,
                    "ATTENDEE",
                    title,
                    message,
                    "NEW_RESPONSE",
                    report.getId()
            );

            notificationRepository.save(notification);
        }
    }

    private void notifyAttendeeOfResponse(Report report, ReportResponse response) {
        String title = "Response to Your Report";
        String message = String.format(
                "A staff member has responded to your report #%s:\n\n" +
                        "From: %s\n" +
                        "Message: %s\n\n" +
                        "Category: %s\n" +
                        "Status: %s",
                report.getId().toString().substring(0, 8),
                response.getResponderRole(),
                response.getMessage(),
                report.getCategory().getDisplayName(),
                report.getStatus().getDisplayName()
        );

        Notification notification = new Notification(
                report.getUserId(),
                report.getUserEmail(),
                response.getResponderRole(),
                title,
                message,
                "STAFF_RESPONSE",
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
            String message = String.format(
                    "A new report has been submitted:\n\n" +
                            "Category: %s\n" +
                            "Description: %s\n\n" +
                            "Please review this report in the admin dashboard.",
                    report.getCategory().getDisplayName(),
                    report.getDescription()
            );

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
            String message = String.format(
                    "A new report has been submitted for an event you manage:\n\n" +
                            "Category: %s\n" +
                            "Description: %s\n\n" +
                            "Please review this report in your organizer dashboard.",
                    report.getCategory().getDisplayName(),
                    report.getDescription()
            );

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