package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.model.Notification;
import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    @Autowired
    public AsyncNotificationService(NotificationRepository notificationRepository, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    @Async("taskExecutor")
    public void processNewReportNotificationsAsync(Report report) {
        try {
            // Process admin notifications
            List<Long> adminIds = userService.getAdminIds();
            for (Long adminId : adminIds) {
                createAndSaveNotification(adminId, report, "NEW_REPORT", "SYSTEM");
            }

            // Process organizer notifications if event-related
            if (report.getEventId() != null) {
                List<Long> organizerIds = userService.getOrganizerIds(report.getEventId());
                for (Long organizerId : organizerIds) {
                    createAndSaveNotification(organizerId, report, "NEW_REPORT", "SYSTEM");
                }
            }

            System.out.println("Async notification processing completed for report: " + report.getId());
        } catch (Exception e) {
            System.err.println("Error in async notification processing: " + e.getMessage());
        }

        CompletableFuture.completedFuture(null);
    }

    @Async("taskExecutor")
    public void processStatusChangeNotificationsAsync(Report report, ReportStatus oldStatus, ReportStatus newStatus) {
        try {
            // Notify the report creator
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
            System.out.println("Async status change notification sent for report: " + report.getId());
        } catch (Exception e) {
            System.err.println("Error in async status change notification: " + e.getMessage());
        }

        CompletableFuture.completedFuture(null);
    }

    @Async("taskExecutor")
    public void processResponseNotificationsAsync(Report report, ReportResponse response) {
        try {
            boolean isFromAttendee = report.getUserEmail().equals(response.getResponderEmail()) ||
                    report.getUserId().equals(response.getResponderId());

            if (isFromAttendee) {
                // Notify admins and organizers
                notifyStaffAsync(report, response);
            } else {
                // Notify attendee
                notifyAttendeeAsync(report, response);
            }

            System.out.println("Async response notification processing completed for report: " + report.getId());
        } catch (Exception e) {
            System.err.println("Error in async response notification: " + e.getMessage());
        }

        CompletableFuture.completedFuture(null);
    }

    private void createAndSaveNotification(Long recipientId, Report report, String type, String senderRole) {
        try {
            String recipientEmail = userService.getUserEmail(recipientId);
            String title = getNotificationTitle(type);
            String message = buildNotificationMessage(report, type);

            Notification notification = new Notification(
                    recipientId,
                    recipientEmail,
                    senderRole,
                    title,
                    message,
                    type,
                    report.getId()
            );

            notificationRepository.save(notification);
        } catch (Exception e) {
            System.err.println("Failed to create notification for user " + recipientId + ": " + e.getMessage());
        }
    }

    private void notifyStaffAsync(Report report, ReportResponse response) {
        // Notify admins
        List<Long> adminIds = userService.getAdminIds();
        for (Long adminId : adminIds) {
            createResponseNotification(adminId, report, response, "NEW_RESPONSE");
        }

        // Notify organizers if event-related
        if (report.getEventId() != null) {
            List<Long> organizerIds = userService.getOrganizerIds(report.getEventId());
            for (Long organizerId : organizerIds) {
                createResponseNotification(organizerId, report, response, "NEW_RESPONSE");
            }
        }
    }

    private void notifyAttendeeAsync(Report report, ReportResponse response) {
        String title = "Response to Your Report";
        String message = String.format(
                "A staff member has responded to your report:\n\n" +
                        "From: %s\n" +
                        "Message: %s\n\n" +
                        "Category: %s\n" +
                        "Status: %s",
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

    private void createResponseNotification(Long recipientId, Report report, ReportResponse response, String type) {
        try {
            String recipientEmail = userService.getUserEmail(recipientId);
            String title = "New Response to Report";
            String message = String.format(
                    "There's a new response to report #%s:\n\n" +
                            "Message: %s\n\n" +
                            "Category: %s\n" +
                            "Status: %s",
                    report.getId().toString().substring(0, 8),
                    response.getMessage(),
                    report.getCategory().getDisplayName(),
                    report.getStatus().getDisplayName()
            );

            Notification notification = new Notification(
                    recipientId,
                    recipientEmail,
                    response.getResponderRole(),
                    title,
                    message,
                    type,
                    report.getId()
            );

            notificationRepository.save(notification);
        } catch (Exception e) {
            System.err.println("Failed to create response notification for user " + recipientId + ": " + e.getMessage());
        }
    }

    private String getNotificationTitle(String type) {
        switch (type) {
            case "NEW_REPORT": return "New Report Submitted";
            case "STATUS_UPDATE": return "Report Status Updated";
            default: return "Notification";
        }
    }

    private String buildNotificationMessage(Report report, String type) {
        if ("NEW_REPORT".equals(type)) {
            if (report.getEventId() != null && report.getEventTitle() != null) {
                return String.format(
                        "A new event-related report has been submitted:\n\n" +
                                "Event: %s\n" +
                                "Category: %s\n" +
                                "Description: %s",
                        report.getEventTitle(),
                        report.getCategory().getDisplayName(),
                        report.getDescription()
                );
            } else {
                return String.format(
                        "A new general report has been submitted:\n\n" +
                                "Category: %s\n" +
                                "Description: %s",
                        report.getCategory().getDisplayName(),
                        report.getDescription()
                );
            }
        }
        return "New notification";
    }
}