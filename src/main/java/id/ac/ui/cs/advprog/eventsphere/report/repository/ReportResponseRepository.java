package id.ac.ui.cs.advprog.eventsphere.report.repository;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportResponseRepository extends JpaRepository<ReportResponse, UUID> {

    List<ReportResponse> findByReportId(UUID reportId);

    List<ReportResponse> findByResponderId(Long responderId);

    List<ReportResponse> findByResponderEmail(String email);

    List<ReportResponse> findByResponderRole(String responderRole);
}