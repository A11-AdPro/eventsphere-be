package id.ac.ui.cs.advprog.eventsphere.report.repository;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ReportResponseRepositoryTest {

    private ReportResponseRepository responseRepository;
    private UUID reportId;
    private UUID responseId;
    private ReportResponse testResponse;

    @BeforeEach
    public void setUp() {
        responseRepository = new InMemoryReportResponseRepository();
        reportId = UUID.randomUUID();
        responseId = UUID.randomUUID();
        testResponse = new ReportResponse(
                responseId,
                reportId,
                UUID.randomUUID(),
                UserRole.ADMIN,
                "We are looking into your issue",
                LocalDateTime.now()
        );
    }

    @Test
    public void testSaveAndFindById() {
        responseRepository.save(testResponse);
        Optional<ReportResponse> foundResponse = responseRepository.findById(responseId);

        assertTrue(foundResponse.isPresent());
        assertEquals(responseId, foundResponse.get().getId());
        assertEquals(reportId, foundResponse.get().getReportId());
    }

    @Test
    public void testFindByReportId() {
        responseRepository.save(testResponse);

        List<ReportResponse> responses = responseRepository.findByReportId(reportId);

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals(reportId, responses.getFirst().getReportId());
    }

    @Test
    public void testDeleteByReportId() {
        responseRepository.save(testResponse);
        responseRepository.deleteByReportId(reportId);

        List<ReportResponse> responses = responseRepository.findByReportId(reportId);
        assertTrue(responses.isEmpty());
    }
}
