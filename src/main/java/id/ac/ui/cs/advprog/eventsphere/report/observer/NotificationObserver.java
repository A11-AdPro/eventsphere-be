package id.ac.ui.cs.advprog.eventsphere.report.observer;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;

public interface NotificationObserver {
    void onStatusChange(Report report, ReportStatus newStatus);
    void onNewReport(Report report);
    void onNewResponse(Report report, String response);
}