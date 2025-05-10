package id.ac.ui.cs.advprog.eventsphere.report.repository;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
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
public class ReportResponseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReportResponseRepository responseRepository;

    @Test
    public void testFindByReportId() {
        // Create a test report
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Test report");
        entityManager.persist(report);

        // Create test responses
        ReportResponse response1 = new ReportResponse(1L, "admin@example.com", "ADMIN", "Admin response", report);
        ReportResponse response2 = new ReportResponse(2L, "organizer@example.com", "ORGANIZER", "Organizer response", report);

        entityManager.persist(response1);
        entityManager.persist(response2);
        entityManager.flush();

        // Test repository method
        List<ReportResponse> foundResponses = responseRepository.findByReportId(report.getId());

        // Verify results
        assertEquals(2, foundResponses.size());
        assertTrue(foundResponses.stream().anyMatch(r -> r.getResponderRole().equals("ADMIN")));
        assertTrue(foundResponses.stream().anyMatch(r -> r.getResponderRole().equals("ORGANIZER")));
    }

    @Test
    public void testFindByResponderId() {
        // Create test reports
        Report report1 = new Report(1L, "user1@example.com", ReportCategory.PAYMENT, "Report 1");
        Report report2 = new Report(2L, "user2@example.com", ReportCategory.TICKET, "Report 2");

        entityManager.persist(report1);
        entityManager.persist(report2);

        // Create test responses
        Long responderId = 1L;

        ReportResponse response1 = new ReportResponse(responderId, "admin@example.com", "ADMIN", "Response to report 1", report1);
        ReportResponse response2 = new ReportResponse(responderId, "admin@example.com", "ADMIN", "Response to report 2", report2);
        ReportResponse response3 = new ReportResponse(2L, "organizer@example.com", "ORGANIZER", "Different responder", report1);

        entityManager.persist(response1);
        entityManager.persist(response2);
        entityManager.persist(response3);
        entityManager.flush();

        // Test repository method
        List<ReportResponse> responderResponses = responseRepository.findByResponderId(responderId);

        // Verify results
        assertEquals(2, responderResponses.size());
        assertTrue(responderResponses.stream().allMatch(r -> r.getResponderId().equals(responderId)));
    }

    @Test
    public void testFindByResponderEmail() {
        // Create test reports
        Report report1 = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Report 1");
        Report report2 = new Report(2L, "user@example.com", ReportCategory.TICKET, "Report 2");

        entityManager.persist(report1);
        entityManager.persist(report2);

        // Create test responses
        String responderEmail = "admin@example.com";

        ReportResponse response1 = new ReportResponse(1L, responderEmail, "ADMIN", "Response to report 1", report1);
        ReportResponse response2 = new ReportResponse(1L, responderEmail, "ADMIN", "Response to report 2", report2);
        ReportResponse response3 = new ReportResponse(2L, "other@example.com", "ORGANIZER", "Different responder", report1);

        entityManager.persist(response1);
        entityManager.persist(response2);
        entityManager.persist(response3);
        entityManager.flush();

        // Test repository method
        List<ReportResponse> responderResponses = responseRepository.findByResponderEmail(responderEmail);

        // Verify results
        assertEquals(2, responderResponses.size());
        assertTrue(responderResponses.stream().allMatch(r -> r.getResponderEmail().equals(responderEmail)));
    }

    @Test
    public void testFindByResponderRole() {
        // Create a test report
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Test report");
        entityManager.persist(report);

        // Create test responses
        ReportResponse adminResponse1 = new ReportResponse(1L, "admin1@example.com", "ADMIN", "Admin response 1", report);
        ReportResponse adminResponse2 = new ReportResponse(2L, "admin2@example.com", "ADMIN", "Admin response 2", report);
        ReportResponse organizerResponse = new ReportResponse(3L, "organizer@example.com", "ORGANIZER", "Organizer response", report);

        entityManager.persist(adminResponse1);
        entityManager.persist(adminResponse2);
        entityManager.persist(organizerResponse);
        entityManager.flush();

        // Test repository method
        List<ReportResponse> adminResponses = responseRepository.findByResponderRole("ADMIN");
        List<ReportResponse> organizerResponses = responseRepository.findByResponderRole("ORGANIZER");

        // Verify results
        assertEquals(2, adminResponses.size());
        assertEquals(1, organizerResponses.size());
        assertTrue(adminResponses.stream().allMatch(r -> r.getResponderRole().equals("ADMIN")));
        assertTrue(organizerResponses.stream().allMatch(r -> r.getResponderRole().equals("ORGANIZER")));
    }
}