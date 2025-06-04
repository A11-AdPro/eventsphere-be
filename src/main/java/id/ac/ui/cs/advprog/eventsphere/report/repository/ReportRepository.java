package id.ac.ui.cs.advprog.eventsphere.report.repository;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByUserId(Long userId);

    List<Report> findByUserEmail(String email);

    List<Report> findByStatus(ReportStatus status);

    List<Report> findByCategory(ReportCategory category);

    List<Report> findByUserIdAndStatus(Long userId, ReportStatus status);

    List<Report> findByUserEmailAndStatus(String email, ReportStatus status);

    List<Report> findByEventId(Long eventId);

    List<Report> findByEventIdAndStatus(Long eventId, ReportStatus status);
}