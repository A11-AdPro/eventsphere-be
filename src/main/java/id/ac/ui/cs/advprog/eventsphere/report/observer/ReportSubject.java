package id.ac.ui.cs.advprog.eventsphere.report.observer;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;

public interface ReportSubject {
    void registerObserver(NotificationObserver observer);
    void removeObserver(NotificationObserver observer);
    void notifyStatusChange(Report report, ReportStatus newStatus);
    void notifyNewReport(Report report);
    void notifyNewResponse(Report report, String response);
}
