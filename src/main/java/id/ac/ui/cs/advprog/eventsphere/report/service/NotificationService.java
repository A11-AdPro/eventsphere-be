package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.model.Notification;
import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.observer.ReportObserver;
import id.ac.ui.cs.advprog.eventsphere.report.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class NotificationService implements ReportObserver {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    @Override
    @Async("notificationExecutor")
    public void onStatusChanged(Report report, ReportStatus oldStatus, ReportStatus newStatus) {
        logger.info("Processing ASYNC status change notification for report: {} - THREAD: {}",
                report.getId(), Thread.currentThread().getName());

        try {
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
            logger.info("Status change notification sent successfully - THREAD: {}",
                    Thread.currentThread().getName());

        } catch (Exception e) {
            logger.error("Failed to send status change notification - THREAD: {}",
                    Thread.currentThread().getName(), e);
        }
    }

    @Override
    @Async("notificationExecutor")
    public void onResponseAdded(Report report, ReportResponse response) {
        logger.info("Processing async response notification for report: {}", report.getId());

        try {
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
            logger.info("Response notification sent successfully for report: {}", report.getId());

        } catch (Exception e) {
            logger.error("Failed to send response notification for report: {}", report.getId(), e);
        }
    }

    @Async("notificationExecutor")
    public CompletableFuture<Void> notifyNewReportAsync(Report report) {
        logger.info("Processing ASYNC new report notifications for report: {} - THREAD: {}",
                report.getId(), Thread.currentThread().getName());

        return CompletableFuture.runAsync(() -> {
            try {
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

                logger.info("All admin notifications sent successfully ({} admins) - THREAD: {}",
                        adminIds.size(), Thread.currentThread().getName());

            } catch (Exception e) {
                logger.error("Failed to send admin notifications for report: {}", report.getId(), e);
                throw new RuntimeException("Admin notification failed", e);
            }
        });
    }

    // Synchronous version
    public void notifyNewReport(Report report) {
        notifyNewReportAsync(report)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        logger.error("Async admin notification failed for report: {}", report.getId(), exception);
                    } else {
                        logger.info("Async admin notifications completed for report: {}", report.getId());
                    }
                });
    }

    @Async("notificationExecutor")
    public CompletableFuture<Void> notifyOrganizerOfReportAsync(Report report, UUID eventId) {
        logger.info("📨 Processing ASYNC organizer notifications for report: {} and event: {} - THREAD: {}",
                report.getId(), eventId, Thread.currentThread().getName());

        return CompletableFuture.runAsync(() -> {
            try {
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

                logger.info("All organizer notifications sent successfully ({} organizers) - THREAD: {}",
                        organizerIds.size(), Thread.currentThread().getName());

            } catch (Exception e) {
                logger.error("Failed to send organizer notifications for report: {}", report.getId(), e);
                throw new RuntimeException("Organizer notification failed", e);
            }
        });
    }

    // Synchronous version
    public void notifyOrganizerOfReport(Report report, UUID eventId) {
        notifyOrganizerOfReportAsync(report, eventId)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        logger.error("Async organizer notification failed for report: {}", report.getId(), exception);
                    } else {
                        logger.info("Async organizer notifications completed for report: {}", report.getId());
                    }
                });
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