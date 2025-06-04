package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.model.Notification;
import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncNotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    @Autowired
    public AsyncNotificationService(NotificationRepository notificationRepository, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    // Fungsi ini digunakan untuk memproses notifikasi laporan baru secara asinkron.
    // Memberikan notifikasi ke admin dan organizer acara jika laporan terkait acara.
    @Async("taskExecutor")
    public CompletableFuture<String> processNewReportNotificationsAsync(Report report) {
        try {
            // Memproses pemberitahuan untuk admin
            List<Long> adminIds = userService.getAdminIds();
            for (Long adminId : adminIds) {
                createAndSaveNotification(adminId, report);
            }

            // Memproses pemberitahuan untuk organizer acara jika laporan terkait dengan acara
            if (report.getEventId() != null) {
                List<Long> organizerIds = userService.getOrganizerIds(report.getEventId());
                for (Long organizerId : organizerIds) {
                    createAndSaveNotification(organizerId, report);
                }
            }

            logger.info("Async notification processing completed for report: {}", report.getId());
            return CompletableFuture.completedFuture("Successfully processed new report notifications for report: " + report.getId());
        } catch (Exception e) {
            logger.error("Error in async notification processing for report {}: {}", report.getId(), e.getMessage(), e);
            return CompletableFuture.completedFuture("Error processing notifications for report: " + report.getId());
        }
    }

    // Fungsi ini digunakan untuk memproses pemberitahuan perubahan status laporan secara asinkron.
    @Async("taskExecutor")
    public CompletableFuture<String> processStatusChangeNotificationsAsync(Report report, ReportStatus oldStatus, ReportStatus newStatus) {
        try {
            // Memberi pemberitahuan kepada pembuat laporan mengenai perubahan status
            String title = "Report Status Updated";
            String message = String.format(
                    "Your report status has been updated from %s to %s.\n\n" +
                            "Category: %s\n" +
                            "Description: %s",
                    oldStatus != null ? oldStatus.getDisplayName() : "Unknown",
                    newStatus != null ? newStatus.getDisplayName() : "Unknown",
                    report.getCategory() != null ? report.getCategory().getDisplayName() : "No category",
                    report.getDescription() != null ? report.getDescription() : "No description"
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
            logger.info("Async status change notification sent for report: {}", report.getId());
            return CompletableFuture.completedFuture("Successfully sent status update notification for report: " + report.getId());
        } catch (Exception e) {
            logger.error("Error in async status change notification for report {}: {}", report.getId(), e.getMessage(), e);
            return CompletableFuture.completedFuture("Error processing status change notification for report: " + report.getId());
        }
    }

    // Fungsi ini digunakan untuk memproses pemberitahuan tanggapan pada laporan secara asinkron.
    @Async("taskExecutor")
    public CompletableFuture<String> processResponseNotificationsAsync(Report report, ReportResponse response) {
        try {
            boolean isFromAttendee = report.getUserEmail() != null && report.getUserEmail().equals(response.getResponderEmail()) ||
                    report.getUserId() != null && report.getUserId().equals(response.getResponderId());

            if (isFromAttendee) {
                // Pemberitahuan untuk staf dan organizer acara
                notifyStaffAsync(report, response);
            } else {
                // Pemberitahuan untuk peserta
                notifyAttendeeAsync(report, response);
            }

            logger.info("Async response notification processing completed for report: {}", report.getId());
            return CompletableFuture.completedFuture("Successfully processed response notifications for report: " + report.getId());
        } catch (Exception e) {
            logger.error("Error in async response notification for report {}: {}", report.getId(), e.getMessage(), e);
            return CompletableFuture.completedFuture("Error processing response  notificationsfor report: " + report.getId());
        }
    }

    // Fungsi ini digunakan untuk membuat dan menyimpan notifikasi untuk penerima tertentu (admin atau organizer).
    private void createAndSaveNotification(Long recipientId, Report report) {
        try {
            String recipientEmail = userService.getUserEmail(recipientId);
            String title = "New Report Submitted";
            String message = String.format(
                    "A new event-related report has been submitted:\n\n" +
                            "Event: %s\n" +
                            "Category: %s\n" +
                            "Description: %s",
                    report.getEventTitle() != null ? report.getEventTitle() : "No event title",
                    report.getCategory() != null ? report.getCategory().getDisplayName() : "No category",
                    report.getDescription() != null ? report.getDescription() : "No description"
            );

            Notification notification = new Notification(
                    recipientId,
                    recipientEmail,
                    "SYSTEM",
                    title,
                    message,
                    "NEW_REPORT",
                    report.getId()
            );

            notificationRepository.save(notification);
            logger.info("Notification created and saved for user: {}", recipientId);
        } catch (Exception e) {
            logger.error("Failed to create notification for user {}: {}", recipientId, e.getMessage(), e);
        }
    }

    // Fungsi ini digunakan untuk memberi pemberitahuan kepada staf (admin dan organizer) terkait tanggapan pada laporan.
    private void notifyStaffAsync(Report report, ReportResponse response) {
        // Memberi pemberitahuan kepada admin
        List<Long> adminIds = userService.getAdminIds();
        for (Long adminId : adminIds) {
            createResponseNotification(adminId, report, response);
        }

        // Memberi pemberitahuan kepada organizer acara jika laporan terkait acara
        if (report.getEventId() != null) {
            List<Long> organizerIds = userService.getOrganizerIds(report.getEventId());
            for (Long organizerId : organizerIds) {
                createResponseNotification(organizerId, report, response);
            }
        }
    }

    // Fungsi ini digunakan untuk memberi pemberitahuan kepada peserta terkait tanggapan pada laporan.
    private void notifyAttendeeAsync(Report report, ReportResponse response) {
        try {
            String title = "Response to Your Report";

            // Format pesan untuk pemberitahuan
            String message = String.format(
                    "A staff member has responded to your report:\n\n" +
                            "From: %s\n" +
                            "Message: %s\n\n" +
                            "Category: %s\n" +
                            "Status: %s",
                    response.getResponderRole() != null ? response.getResponderRole() : "Staff",
                    response.getMessage() != null ? response.getMessage() : "No message provided",
                    report.getCategory() != null ? report.getCategory().getDisplayName() : "No category",
                    report.getStatus() != null ? report.getStatus().getDisplayName() : "No status"
            );

            Notification notification = new Notification(
                    report.getUserId(),
                    report.getUserEmail(),
                    response.getResponderRole() != null ? response.getResponderRole() : "STAFF",
                    title,
                    message,
                    "STAFF_RESPONSE",
                    report.getId()
            );

            notificationRepository.save(notification);
            logger.info("Attendee notification sent for report: {}", report.getId());
        } catch (Exception e) {
            logger.error("Failed to notify attendee for report {}: {}", report.getId(), e.getMessage(), e);
        }
    }

    // Fungsi ini digunakan untuk membuat pemberitahuan untuk tanggapan pada laporan yang dikirimkan kepada admin atau organizer.
    private void createResponseNotification(Long recipientId, Report report, ReportResponse response) {
        try {
            String recipientEmail = userService.getUserEmail(recipientId);
            String title = "New Response to Report";
            String message = String.format(
                    "There's a new response to report:\n\n" +
                            "From: %s\n" +
                            "Message: %s\n\n" +
                            "Category: %s\n" +
                            "Status: %s",
                    response.getResponderRole() != null ? response.getResponderRole() : "User",
                    response.getMessage() != null ? response.getMessage() : "No message provided",
                    report.getCategory() != null ? report.getCategory().getDisplayName() : "No category",
                    report.getStatus() != null ? report.getStatus().getDisplayName() : "No status"
            );

            Notification notification = new Notification(
                    recipientId,
                    recipientEmail,
                    response.getResponderRole() != null ? response.getResponderRole() : "USER",
                    title,
                    message,
                    "NEW_RESPONSE",
                    report.getId()
            );

            notificationRepository.save(notification);
            logger.info("Response notification sent to user: {}", recipientId);
        } catch (Exception e) {
            logger.error("Failed to create response notification for user {}: {}", recipientId, e.getMessage(), e);
        }
    }
}
