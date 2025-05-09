package id.ac.ui.cs.advprog.eventsphere.report.observer;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;

public class AdminNotificationObserver implements NotificationObserver {

    @Override
    public void onStatusChange(Report report, ReportStatus newStatus) {
        // Usually admin initiates status changes, so may not need notification
        System.out.println("Status update recorded in admin logs");
    }

    @Override
    public void onNewReport(Report report) {
        // Implementation to notify admin about new report
        System.out.println("Notification sent to admin: New report received from attendee");
    }

    @Override
    public void onNewResponse(Report report, String response) {
        // Implementation to notify admin about new responses (from organizers)
        System.out.println("Notification sent to admin: New response on report #" + report.getId());
    }
}