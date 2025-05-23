package id.ac.ui.cs.advprog.eventsphere.review.repository;

import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {
    List<ReviewReport> findByReviewId(Long reviewId);
    List<ReviewReport> findByStatus(ReviewReport.ReportStatus status);
    boolean existsByReviewIdAndReporterId(Long reviewId, Long reporterId);
}
