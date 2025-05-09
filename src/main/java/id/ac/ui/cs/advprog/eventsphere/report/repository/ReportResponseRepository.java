package id.ac.ui.cs.advprog.eventsphere.report.repository;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportResponseRepository {
    ReportResponse save(ReportResponse response);
    Optional<ReportResponse> findById(UUID id);
    List<ReportResponse> findByReportId(UUID reportId);
    void delete(UUID id);
    void deleteByReportId(UUID reportId);
}
