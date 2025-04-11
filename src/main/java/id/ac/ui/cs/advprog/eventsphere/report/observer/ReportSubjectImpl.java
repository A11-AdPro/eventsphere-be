package id.ac.ui.cs.advprog.eventsphere.report.observer;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;

import java.util.ArrayList;
import java.util.List;

public class ReportSubjectImpl implements ReportSubject {
    private final List<NotificationObserver> observers = new ArrayList<>();

    @Override
    public void registerObserver(NotificationObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyStatusChange(Report report, ReportStatus newStatus) {
        for (NotificationObserver observer : observers) {
            observer.onStatusChange(report, newStatus);
        }
    }

    @Override
    public void notifyNewReport(Report report) {
        for (NotificationObserver observer : observers) {
            observer.onNewReport(report);
        }
    }

    @Override
    public void notifyNewResponse(Report report, String response) {
        for (NotificationObserver observer : observers) {
            observer.onNewResponse(report, response);
        }
    }
}
