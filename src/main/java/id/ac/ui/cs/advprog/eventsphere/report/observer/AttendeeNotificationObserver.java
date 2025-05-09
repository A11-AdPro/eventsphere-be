package id.ac.ui.cs.advprog.eventsphere.report.observer;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;

public class AttendeeNotificationObserver implements NotificationObserver {

    @Override
    public void onStatusChange(Report report, ReportStatus newStatus) {
        // Implementation to send notification to attendee about status change
        System.out.println("Notification sent to attendee: Report status changed to " + newStatus);
    }

    @Override
    public void onNewReport(Report report) {
        // Implementation to confirm receipt of new report to attendee
        System.out.println("Notification sent to attendee: Report successfully submitted");
    }

    @Override
    public void onNewResponse(Report report, String response) {
        // Implementation to notify attendee about new response
        System.out.println("Notification sent to attendee: New response received on your report");
    }
}
