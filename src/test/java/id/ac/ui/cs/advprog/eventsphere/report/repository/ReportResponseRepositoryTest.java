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
        Report report = new Report(UUID.randomUUID(), ReportCategory.PAYMENT, "Test report");
        entityManager.persist(report);

        // Create test responses
        ReportResponse response1 = new ReportResponse(UUID.randomUUID(), "ADMIN", "Admin response", report);
        ReportResponse response2 = new ReportResponse(UUID.randomUUID(), "ORGANIZER", "Organizer response", report);

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
        Report report1 = new Report(UUID.randomUUID(), ReportCategory.PAYMENT, "Report 1");
        Report report2 = new Report(UUID.randomUUID(), ReportCategory.TICKET, "Report 2");

        entityManager.persist(report1);
        entityManager.persist(report2);

        // Create test responses
        UUID responderId = UUID.randomUUID();

        ReportResponse response1 = new ReportResponse(responderId, "ADMIN", "Response to report 1", report1);
        ReportResponse response2 = new ReportResponse(responderId, "ADMIN", "Response to report 2", report2);
        ReportResponse response3 = new ReportResponse(UUID.randomUUID(), "ORGANIZER", "Different responder", report1);

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
    public void testFindByResponderRole() {
        // Create a test report
        Report report = new Report(UUID.randomUUID(), ReportCategory.PAYMENT, "Test report");
        entityManager.persist(report);

        // Create test responses
        ReportResponse adminResponse1 = new ReportResponse(UUID.randomUUID(), "ADMIN", "Admin response 1", report);
        ReportResponse adminResponse2 = new ReportResponse(UUID.randomUUID(), "ADMIN", "Admin response 2", report);
        ReportResponse organizerResponse = new ReportResponse(UUID.randomUUID(), "ORGANIZER", "Organizer response", report);

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