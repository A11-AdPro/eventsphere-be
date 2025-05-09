package id.ac.ui.cs.advprog.eventsphere.report.observer;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;

public interface ReportObserver {

    void onStatusChanged(Report report, ReportStatus oldStatus, ReportStatus newStatus);

    void onResponseAdded(Report report, ReportResponse response);
}