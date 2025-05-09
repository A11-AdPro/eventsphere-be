package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportService {
    Report createReport(UUID attendeeId, String title, String description, ReportType type);
    Optional<Report> getReportById(UUID reportId);
    List<Report> getReportsByAttendeeId(UUID attendeeId);
    List<Report> getReportsByStatus(ReportStatus status);
    List<Report> getAllReports();
    boolean updateReportStatus(UUID reportId, ReportStatus newStatus);
    boolean deleteReport(UUID reportId);
}
