package id.ac.ui.cs.advprog.eventsphere.report.repository;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class ReportRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReportRepository reportRepository;

    @Test
    @DisplayName("Mencari laporan berdasarkan userId")
    public void testFindByUserId() {
        // Arrange
        Report report1 = new Report(1L, "user1@example.com", ReportCategory.PAYMENT, "Report 1");
        Report report2 = new Report(1L, "user1@example.com", ReportCategory.TICKET, "Report 2");
        Report report3 = new Report(2L, "user2@example.com", ReportCategory.EVENT, "Report 3");

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        // Act
        List<Report> result = reportRepository.findByUserId(1L);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.getUserId().equals(1L)));
    }

    @Test
    @DisplayName("Mencari laporan berdasarkan email pengguna")
    public void testFindByUserEmail() {
        // Arrange
        Report report1 = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Report 1");
        Report report2 = new Report(2L, "user@example.com", ReportCategory.TICKET, "Report 2");
        Report report3 = new Report(3L, "other@example.com", ReportCategory.EVENT, "Report 3");

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        // Act
        List<Report> result = reportRepository.findByUserEmail("user@example.com");

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.getUserEmail().equals("user@example.com")));
    }

    @Test
    @DisplayName("Mencari laporan berdasarkan status")
    public void testFindByStatus() {
        // Arrange
        Report report1 = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Report 1");
        report1.setStatus(ReportStatus.PENDING);

        Report report2 = new Report(2L, "user@example.com", ReportCategory.TICKET, "Report 2");
        report2.setStatus(ReportStatus.ON_PROGRESS);

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.flush();

        // Act
        List<Report> pendingReports = reportRepository.findByStatus(ReportStatus.PENDING);
        List<Report> progressReports = reportRepository.findByStatus(ReportStatus.ON_PROGRESS);

        // Assert
        assertEquals(1, pendingReports.size());
        assertEquals(1, progressReports.size());
        assertEquals(ReportStatus.PENDING, pendingReports.getFirst().getStatus());
        assertEquals(ReportStatus.ON_PROGRESS, progressReports.getFirst().getStatus());
    }

    @Test
    @DisplayName("Mencari laporan berdasarkan kategori")
    public void testFindByCategory() {
        // Arrange
        Report report1 = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Report 1");
        Report report2 = new Report(2L, "user@example.com", ReportCategory.PAYMENT, "Report 2");
        Report report3 = new Report(3L, "user@example.com", ReportCategory.TICKET, "Report 3");

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        // Act
        List<Report> paymentReports = reportRepository.findByCategory(ReportCategory.PAYMENT);
        List<Report> ticketReports = reportRepository.findByCategory(ReportCategory.TICKET);

        // Assert
        assertEquals(2, paymentReports.size());
        assertEquals(1, ticketReports.size());
        assertTrue(paymentReports.stream().allMatch(r -> r.getCategory() == ReportCategory.PAYMENT));
        assertTrue(ticketReports.stream().allMatch(r -> r.getCategory() == ReportCategory.TICKET));
    }

    @Test
    @DisplayName("Mencari laporan berdasarkan userId dan status")
    public void testFindByUserIdAndStatus() {
        // Arrange
        Report report1 = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Report 1");
        report1.setStatus(ReportStatus.PENDING);

        Report report2 = new Report(1L, "user@example.com", ReportCategory.TICKET, "Report 2");
        report2.setStatus(ReportStatus.RESOLVED);

        Report report3 = new Report(2L, "other@example.com", ReportCategory.EVENT, "Report 3");
        report3.setStatus(ReportStatus.PENDING);

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        // Act
        List<Report> result = reportRepository.findByUserIdAndStatus(1L, ReportStatus.PENDING);

        // Assert
        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getUserId());
        assertEquals(ReportStatus.PENDING, result.getFirst().getStatus());
    }

    @Test
    @DisplayName("Mencari laporan berdasarkan userEmail dan status")
    public void testFindByUserEmailAndStatus() {
        // Arrange
        Report report1 = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Report 1");
        report1.setStatus(ReportStatus.PENDING);

        Report report2 = new Report(2L, "user@example.com", ReportCategory.TICKET, "Report 2");
        report2.setStatus(ReportStatus.RESOLVED);

        Report report3 = new Report(3L, "other@example.com", ReportCategory.EVENT, "Report 3");
        report3.setStatus(ReportStatus.PENDING);

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        // Act
        List<Report> result = reportRepository.findByUserEmailAndStatus("user@example.com", ReportStatus.PENDING);

        // Assert
        assertEquals(1, result.size());
        assertEquals("user@example.com", result.getFirst().getUserEmail());
        assertEquals(ReportStatus.PENDING, result.getFirst().getStatus());
    }

    @Test
    @DisplayName("Mencari laporan berdasarkan eventId")
    public void testFindByEventId() {
        // Arrange
        Report report1 = new Report(1L, "user@example.com", 10L, "Event A", ReportCategory.EVENT, "Event Report 1");
        Report report2 = new Report(2L, "user@example.com", 10L, "Event A", ReportCategory.EVENT, "Event Report 2");
        Report report3 = new Report(3L, "user@example.com", 20L, "Event B", ReportCategory.EVENT, "Event Report 3");

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        // Act
        List<Report> result = reportRepository.findByEventId(10L);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.getEventId().equals(10L)));
    }
}