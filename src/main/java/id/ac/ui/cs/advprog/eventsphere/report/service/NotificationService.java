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
    private final AsyncNotificationService asyncNotificationService;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository,
                               UserService userService,
                               AsyncNotificationService asyncNotificationService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.asyncNotificationService = asyncNotificationService;
    }

    // ASYNC METHODS - These methods now delegate to AsyncNotificationService
    @Override
    public void onStatusChanged(Report report, ReportStatus oldStatus, ReportStatus newStatus) {
        // Process asynchronously
        asyncNotificationService.processStatusChangeNotificationsAsync(report, oldStatus, newStatus);
    }

    @Override
    public void onResponseAdded(Report report, ReportResponse response) {
        // Process asynchronously
        asyncNotificationService.processResponseNotificationsAsync(report, response);
    }

    public void notifyNewReport(Report report) {
        // Process asynchronously
        asyncNotificationService.processNewReportNotificationsAsync(report);
    }

    // SYNCHRONOUS METHODS - These methods remain synchronous for immediate operations

    // Synchronous fallback method for critical notifications (if needed)
    public void onStatusChangedSync(Report report, ReportStatus oldStatus, ReportStatus newStatus) {
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

    // Synchronous fallback method for critical notifications (if needed)
    public void onResponseAddedSync(Report report, ReportResponse response) {
        boolean isFromAttendee = report.getUserEmail().equals(response.getResponderEmail()) ||
                report.getUserId().equals(response.getResponderId());

        boolean isFromStaff = "ADMIN".equals(response.getResponderRole()) ||
                "ORGANIZER".equals(response.getResponderRole());

        if (isFromAttendee) {
            notifyAdminsOfResponseSync(report, response);

            if (report.getEventId() != null) {
                notifyEventOrganizersOfResponseSync(report, response);
            }
        }
        else if (isFromStaff) {
            notifyAttendeeOfResponseSync(report, response);

            if (report.getEventId() != null && "ADMIN".equals(response.getResponderRole())) {
                notifyEventOrganizersOfAdminResponseSync(report, response);
            }
        }
    }

    private void notifyAdminsOfResponseSync(Report report, ReportResponse response) {
        List<Long> adminIds = userService.getAdminIds();

        for (Long adminId : adminIds) {
            try {
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
            } catch (Exception e) {
                System.err.println("Failed to notify admin " + adminId + ": " + e.getMessage());
            }
        }
    }

    private void notifyAttendeeOfResponseSync(Report report, ReportResponse response) {
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

    // Synchronous fallback method for critical notifications (if needed)
    public void notifyNewReportSync(Report report) {
        notifyAdminsOfNewReportSync(report);

        if (report.getEventId() != null) {
            notifyEventOrganizersOfNewReportSync(report);
        }
    }

    private void notifyAdminsOfNewReportSync(Report report) {
        List<Long> adminIds = userService.getAdminIds();

        for (Long adminId : adminIds) {
            try {
                String adminEmail = userService.getUserEmail(adminId);
                String title = "New Report Submitted";
                String message;

                if (report.getEventId() != null && report.getEventTitle() != null) {
                    message = String.format(
                            "A new event-related report has been submitted:\n\n" +
                                    "Event: %s\n" +
                                    "Category: %s\n" +
                                    "Description: %s\n\n" +
                                    "Please review this report in the admin dashboard.",
                            report.getEventTitle(),
                            report.getCategory().getDisplayName(),
                            report.getDescription()
                    );
                } else {
                    message = String.format(
                            "A new general report has been submitted:\n\n" +
                                    "Category: %s\n" +
                                    "Description: %s\n\n" +
                                    "Please review this report in the admin dashboard.",
                            report.getCategory().getDisplayName(),
                            report.getDescription()
                    );
                }

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
            } catch (Exception e) {
                System.err.println("Failed to notify admin " + adminId + ": " + e.getMessage());
            }
        }
    }

    private void notifyEventOrganizersOfNewReportSync(Report report) {
        List<Long> organizerIds = userService.getOrganizerIds(report.getEventId());

        for (Long organizerId : organizerIds) {
            try {
                String organizerEmail = userService.getUserEmail(organizerId);
                String title = "New Report for Your Event";
                String message = String.format(
                        "A new report has been submitted for your event '%s':\n\n" +
                                "Category: %s\n" +
                                "Description: %s\n" +
                                "User: %s\n\n" +
                                "Please review and respond to this report in your organizer dashboard.",
                        report.getEventTitle() != null ? report.getEventTitle() : "Event #" + report.getEventId(),
                        report.getCategory().getDisplayName(),
                        report.getDescription(),
                        report.getUserEmail()
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

            } catch (Exception e) {
                System.err.println("Failed to notify organizer " + organizerId + ": " + e.getMessage());
            }
        }
    }

    private void notifyEventOrganizersOfResponseSync(Report report, ReportResponse response) {
        List<Long> organizerIds = userService.getOrganizerIds(report.getEventId());

        for (Long organizerId : organizerIds) {
            try {
                String organizerEmail = userService.getUserEmail(organizerId);
                String title = "New Response to Event Report";
                String message = String.format(
                        "The attendee has responded to a report about your event '%s':\n\n" +
                                "User: %s\n" +
                                "Message: %s\n\n" +
                                "Category: %s\n" +
                                "Status: %s\n\n" +
                                "Please review and respond in your organizer dashboard.",
                        report.getEventTitle() != null ? report.getEventTitle() : "Event #" + report.getEventId(),
                        report.getUserEmail(),
                        response.getMessage(),
                        report.getCategory().getDisplayName(),
                        report.getStatus().getDisplayName()
                );

                Notification notification = new Notification(
                        organizerId,
                        organizerEmail,
                        "ATTENDEE",
                        title,
                        message,
                        "NEW_RESPONSE",
                        report.getId()
                );

                notificationRepository.save(notification);

            } catch (Exception e) {
                System.err.println("Failed to notify organizer " + organizerId + " of response: " + e.getMessage());
            }
        }
    }

    private void notifyEventOrganizersOfAdminResponseSync(Report report, ReportResponse response) {
        List<Long> organizerIds = userService.getOrganizerIds(report.getEventId());

        for (Long organizerId : organizerIds) {
            if (organizerId.equals(response.getResponderId())) {
                continue;
            }

            try {
                String organizerEmail = userService.getUserEmail(organizerId);
                String title = "Admin Response to Event Report";
                String message = String.format(
                        "An admin has responded to a report about your event '%s':\n\n" +
                                "Admin Message: %s\n\n" +
                                "Category: %s\n" +
                                "Status: %s\n\n" +
                                "You may want to review this in your organizer dashboard.",
                        report.getEventTitle() != null ? report.getEventTitle() : "Event #" + report.getEventId(),
                        response.getMessage(),
                        report.getCategory().getDisplayName(),
                        report.getStatus().getDisplayName()
                );

                Notification notification = new Notification(
                        organizerId,
                        organizerEmail,
                        "ADMIN",
                        title,
                        message,
                        "ADMIN_RESPONSE",
                        report.getId()
                );

                notificationRepository.save(notification);

            } catch (Exception e) {
                System.err.println("Failed to notify organizer " + organizerId + " of admin response: " + e.getMessage());
            }
        }
    }

    // EXISTING SYNCHRONOUS METHODS - These remain unchanged for immediate read operations
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