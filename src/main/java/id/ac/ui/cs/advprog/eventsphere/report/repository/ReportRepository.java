package id.ac.ui.cs.advprog.eventsphere.report.repository;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportRepository {
    void save(Report report);
    Optional<Report> findById(UUID id);
    List<Report> findByAttendeeId(UUID attendeeId);
    List<Report> findByStatus(ReportStatus status);
    void update(Report report);
    void delete(UUID id);
}