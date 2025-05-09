package id.ac.ui.cs.advprog.eventsphere.report.observer;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ReportSubjectImpl implements ReportSubject {
    // Menggunakan CopyOnWriteArrayList untuk keamanan thread
    private final List<NotificationObserver> observers = new CopyOnWriteArrayList<>();

    @Override
    public void registerObserver(NotificationObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(NotificationObserver observer) {
        if (observer != null) {
            observers.remove(observer);
        }
    }

    @Override
    public void notifyStatusChange(Report report, ReportStatus newStatus) {
        if (report != null && newStatus != null) {
            for (NotificationObserver observer : observers) {
                observer.onStatusChange(report, newStatus);
            }
        }
    }

    @Override
    public void notifyNewReport(Report report) {
        if (report != null) {
            for (NotificationObserver observer : observers) {
                observer.onNewReport(report);
            }
        }
    }

    @Override
    public void notifyNewResponse(Report report, String response) {
        if (report != null && response != null && !response.isEmpty()) {
            for (NotificationObserver observer : observers) {
                observer.onNewResponse(report, response);
            }
        }
    }
}