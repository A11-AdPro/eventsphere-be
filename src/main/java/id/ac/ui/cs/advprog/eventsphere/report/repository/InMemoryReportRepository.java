package id.ac.ui.cs.advprog.eventsphere.report.repository;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryReportRepository implements ReportRepository {
    private final Map<UUID, Report> reports = new HashMap<>();

    @Override
    public Report save(Report report) {
        reports.put(report.getId(), report);
        return report;
    }

    @Override
    public Optional<Report> findById(UUID id) {
        return Optional.ofNullable(reports.get(id));
    }

    @Override
    public List<Report> findByAttendeeId(UUID attendeeId) {
        return reports.values().stream()
                .filter(report -> report.getAttendeeId().equals(attendeeId))
                .toList();
    }

    @Override
    public List<Report> findByStatus(ReportStatus status) {
        return reports.values().stream()
                .filter(report -> report.getStatus() == status)
                .toList();
    }

    @Override
    public List<Report> findAll() {
        return new ArrayList<>(reports.values());
    }

    @Override
    public Report update(Report report) {
        reports.put(report.getId(), report);
        return report;
    }

    @Override
    public void delete(UUID id) {
        reports.remove(id);
    }
}
