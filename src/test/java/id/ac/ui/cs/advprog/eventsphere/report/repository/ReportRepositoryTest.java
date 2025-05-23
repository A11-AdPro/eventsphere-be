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
    @DisplayName("Mencari notifikasi berdasarkan userId")
    public void testFindByUserId() {
        // Arrange
        Long userId1 = 1L;
        Long userId2 = 2L;

        Report report1 = new Report(userId1, "user1@example.com", ReportCategory.PAYMENT, "User 1 Report 1");
        Report report2 = new Report(userId1, "user1@example.com", ReportCategory.TICKET, "User 1 Report 2");
        Report report3 = new Report(userId2, "user2@example.com", ReportCategory.EVENT, "User 2 Report");

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        // Act
        List<Report> foundReports = reportRepository.findByUserId(userId1);

        // Assert
        assertEquals(2, foundReports.size());
        assertTrue(foundReports.stream().anyMatch(r -> r.getCategory() == ReportCategory.PAYMENT));
        assertTrue(foundReports.stream().anyMatch(r -> r.getCategory() == ReportCategory.TICKET));
    }

    @Test
    @DisplayName("Mencari notifikasi berdasarkan email pengguna")
    public void testFindByUserEmail() {
        // Arrange
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        Report report1 = new Report(1L, email1, ReportCategory.PAYMENT, "User 1 Report 1");
        Report report2 = new Report(1L, email1, ReportCategory.TICKET, "User 1 Report 2");
        Report report3 = new Report(2L, email2, ReportCategory.EVENT, "User 2 Report");

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        // Act
        List<Report> foundReports = reportRepository.findByUserEmail(email1);

        // Assert
        assertEquals(2, foundReports.size());
        assertTrue(foundReports.stream().anyMatch(r -> r.getCategory() == ReportCategory.PAYMENT));
        assertTrue(foundReports.stream().anyMatch(r -> r.getCategory() == ReportCategory.TICKET));
    }

    @Test
    @DisplayName("Mencari notifikasi berdasarkan status")
    public void testFindByStatus() {
        // Arrange
        Report report1 = new Report(1L, "user1@example.com", ReportCategory.PAYMENT, "Pending Report");
        report1.setStatus(ReportStatus.PENDING);

        Report report2 = new Report(2L, "user2@example.com", ReportCategory.TICKET, "In Progress Report");
        report2.setStatus(ReportStatus.ON_PROGRESS);

        Report report3 = new Report(3L, "user3@example.com", ReportCategory.EVENT, "Resolved Report");
        report3.setStatus(ReportStatus.RESOLVED);

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        // Act
        List<Report> pendingReports = reportRepository.findByStatus(ReportStatus.PENDING);
        List<Report> progressReports = reportRepository.findByStatus(ReportStatus.ON_PROGRESS);
        List<Report> resolvedReports = reportRepository.findByStatus(ReportStatus.RESOLVED);

        // Assert
        assertEquals(1, pendingReports.size());
        assertEquals(1, progressReports.size());
        assertEquals(1, resolvedReports.size());

        assertEquals(ReportCategory.PAYMENT, pendingReports.getFirst().getCategory());
        assertEquals(ReportCategory.TICKET, progressReports.getFirst().getCategory());
        assertEquals(ReportCategory.EVENT, resolvedReports.getFirst().getCategory());
    }

    @Test
    @DisplayName("Mencari notifikasi berdasarkan kategori")
    public void testFindByCategory() {
        // Arrange
        Report report1 = new Report(1L, "user1@example.com", ReportCategory.PAYMENT, "Payment Report 1");
        Report report2 = new Report(2L, "user2@example.com", ReportCategory.PAYMENT, "Payment Report 2");
        Report report3 = new Report(3L, "user3@example.com", ReportCategory.TICKET, "Ticket Report");

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        // Act
        List<Report> paymentReports = reportRepository.findByCategory(ReportCategory.PAYMENT);
        List<Report> ticketReports = reportRepository.findByCategory(ReportCategory.TICKET);
        List<Report> eventReports = reportRepository.findByCategory(ReportCategory.EVENT);

        // Assert
        assertEquals(2, paymentReports.size());
        assertEquals(1, ticketReports.size());
        assertEquals(0, eventReports.size());
    }

    @Test
    @DisplayName("Mencari notifikasi berdasarkan userId dan status")
    public void testFindByUserIdAndStatus() {
        // Arrange
        Long userId = 1L;

        Report report1 = new Report(userId, "user@example.com", ReportCategory.PAYMENT, "Pending Report");
        report1.setStatus(ReportStatus.PENDING);

        Report report2 = new Report(userId, "user@example.com", ReportCategory.TICKET, "Resolved Report");
        report2.setStatus(ReportStatus.RESOLVED);

        Report report3 = new Report(2L, "other@example.com", ReportCategory.EVENT, "Other User Report");
        report3.setStatus(ReportStatus.PENDING);

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        // Act
        List<Report> userPendingReports = reportRepository.findByUserIdAndStatus(userId, ReportStatus.PENDING);
        List<Report> userResolvedReports = reportRepository.findByUserIdAndStatus(userId, ReportStatus.RESOLVED);

        // Assert
        assertEquals(1, userPendingReports.size());
        assertEquals(1, userResolvedReports.size());
        assertEquals(ReportCategory.PAYMENT, userPendingReports.getFirst().getCategory());
        assertEquals(ReportCategory.TICKET, userResolvedReports.getFirst().getCategory());
    }

    @Test
    @DisplayName("Mencari notifikasi berdasarkan userEmail dan status")
    public void testFindByUserEmailAndStatus() {
        // Arrange
        String email = "user@example.com";

        Report report1 = new Report(1L, email, ReportCategory.PAYMENT, "Pending Report");
        report1.setStatus(ReportStatus.PENDING);

        Report report2 = new Report(1L, email, ReportCategory.TICKET, "Resolved Report");
        report2.setStatus(ReportStatus.RESOLVED);

        Report report3 = new Report(2L, "other@example.com", ReportCategory.EVENT, "Other User Report");
        report3.setStatus(ReportStatus.PENDING);

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        // Act
        List<Report> userPendingReports = reportRepository.findByUserEmailAndStatus(email, ReportStatus.PENDING);
        List<Report> userResolvedReports = reportRepository.findByUserEmailAndStatus(email, ReportStatus.RESOLVED);

        // Assert
        assertEquals(1, userPendingReports.size());
        assertEquals(1, userResolvedReports.size());
        assertEquals(ReportCategory.PAYMENT, userPendingReports.getFirst().getCategory());
        assertEquals(ReportCategory.TICKET, userResolvedReports.getFirst().getCategory());
    }
}
