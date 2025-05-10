package id.ac.ui.cs.advprog.eventsphere.report.repository;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ReportRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReportRepository reportRepository;

    @Test
    public void testFindByUserId() {
        // Create test data
        Long userId1 = 1L;
        Long userId2 = 2L;

        Report report1 = new Report(userId1, "user1@example.com", ReportCategory.PAYMENT, "User 1 Report 1");
        Report report2 = new Report(userId1, "user1@example.com", ReportCategory.TICKET, "User 1 Report 2");
        Report report3 = new Report(userId2, "user2@example.com", ReportCategory.EVENT, "User 2 Report");

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        // Test repository method
        List<Report> foundReports = reportRepository.findByUserId(userId1);

        // Verify results
        assertEquals(2, foundReports.size());
        assertTrue(foundReports.stream().anyMatch(r -> r.getCategory() == ReportCategory.PAYMENT));
        assertTrue(foundReports.stream().anyMatch(r -> r.getCategory() == ReportCategory.TICKET));
    }

    @Test
    public void testFindByUserEmail() {
        // Create test data
        String email1 = "user1@example.com";
        String email2 = "user2@example.com";

        Report report1 = new Report(1L, email1, ReportCategory.PAYMENT, "User 1 Report 1");
        Report report2 = new Report(1L, email1, ReportCategory.TICKET, "User 1 Report 2");
        Report report3 = new Report(2L, email2, ReportCategory.EVENT, "User 2 Report");

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        // Test repository method
        List<Report> foundReports = reportRepository.findByUserEmail(email1);

        // Verify results
        assertEquals(2, foundReports.size());
        assertTrue(foundReports.stream().anyMatch(r -> r.getCategory() == ReportCategory.PAYMENT));
        assertTrue(foundReports.stream().anyMatch(r -> r.getCategory() == ReportCategory.TICKET));
    }

    @Test
    public void testFindByStatus() {
        // Create test data
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

        // Test repository methods
        List<Report> pendingReports = reportRepository.findByStatus(ReportStatus.PENDING);
        List<Report> progressReports = reportRepository.findByStatus(ReportStatus.ON_PROGRESS);
        List<Report> resolvedReports = reportRepository.findByStatus(ReportStatus.RESOLVED);

        // Verify results
        assertEquals(1, pendingReports.size());
        assertEquals(1, progressReports.size());
        assertEquals(1, resolvedReports.size());

        assertEquals(ReportCategory.PAYMENT, pendingReports.get(0).getCategory());
        assertEquals(ReportCategory.TICKET, progressReports.get(0).getCategory());
        assertEquals(ReportCategory.EVENT, resolvedReports.get(0).getCategory());
    }

    @Test
    public void testFindByCategory() {
        // Create test data
        Report report1 = new Report(1L, "user1@example.com", ReportCategory.PAYMENT, "Payment Report 1");
        Report report2 = new Report(2L, "user2@example.com", ReportCategory.PAYMENT, "Payment Report 2");
        Report report3 = new Report(3L, "user3@example.com", ReportCategory.TICKET, "Ticket Report");

        entityManager.persist(report1);
        entityManager.persist(report2);
        entityManager.persist(report3);
        entityManager.flush();

        // Test repository methods
        List<Report> paymentReports = reportRepository.findByCategory(ReportCategory.PAYMENT);
        List<Report> ticketReports = reportRepository.findByCategory(ReportCategory.TICKET);
        List<Report> eventReports = reportRepository.findByCategory(ReportCategory.EVENT);

        // Verify results
        assertEquals(2, paymentReports.size());
        assertEquals(1, ticketReports.size());
        assertEquals(0, eventReports.size());
    }

    @Test
    public void testFindByUserIdAndStatus() {
        // Create test data
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

        // Test repository method
        List<Report> userPendingReports = reportRepository.findByUserIdAndStatus(userId, ReportStatus.PENDING);
        List<Report> userResolvedReports = reportRepository.findByUserIdAndStatus(userId, ReportStatus.RESOLVED);

        // Verify results
        assertEquals(1, userPendingReports.size());
        assertEquals(1, userResolvedReports.size());
        assertEquals(ReportCategory.PAYMENT, userPendingReports.get(0).getCategory());
        assertEquals(ReportCategory.TICKET, userResolvedReports.get(0).getCategory());
    }

    @Test
    public void testFindByUserEmailAndStatus() {
        // Create test data
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

        // Test repository method
        List<Report> userPendingReports = reportRepository.findByUserEmailAndStatus(email, ReportStatus.PENDING);
        List<Report> userResolvedReports = reportRepository.findByUserEmailAndStatus(email, ReportStatus.RESOLVED);

        // Verify results
        assertEquals(1, userPendingReports.size());
        assertEquals(1, userResolvedReports.size());
        assertEquals(ReportCategory.PAYMENT, userPendingReports.get(0).getCategory());
        assertEquals(ReportCategory.TICKET, userResolvedReports.get(0).getCategory());
    }
}