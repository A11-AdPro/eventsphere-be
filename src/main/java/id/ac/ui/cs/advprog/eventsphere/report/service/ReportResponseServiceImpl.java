package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.UserRole;
import id.ac.ui.cs.advprog.eventsphere.report.observer.ReportSubject;
import id.ac.ui.cs.advprog.eventsphere.report.repository.ReportRepository;
import id.ac.ui.cs.advprog.eventsphere.report.repository.ReportResponseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service  // ⬅️ INI PENTING
public class ReportResponseServiceImpl implements ReportResponseService {

    private final ReportResponseRepository responseRepository;
    private final ReportRepository reportRepository;
    private final ReportSubject reportSubject;

    public ReportResponseServiceImpl(
            ReportResponseRepository responseRepository,
            ReportRepository reportRepository,
            ReportSubject reportSubject
    ) {
        this.responseRepository = responseRepository;
        this.reportRepository = reportRepository;
        this.reportSubject = reportSubject;
    }

    @Override
    public ReportResponse addResponse(UUID reportId, UUID responderId, UserRole responderRole, String content) {
        Optional<Report> optionalReport = reportRepository.findById(reportId);

        if (optionalReport.isEmpty()) {
            throw new IllegalArgumentException("Report with ID " + reportId + " not found");
        }

        ReportResponse response = new ReportResponse(
                UUID.randomUUID(),
                reportId,
                responderId,
                responderRole,
                content,
                LocalDateTime.now()
        );

        ReportResponse savedResponse = responseRepository.save(response);
        reportSubject.notifyNewResponse(optionalReport.get(), content);

        return savedResponse;
    }

    @Override
    public List<ReportResponse> getResponsesByReportId(UUID reportId) {
        return responseRepository.findByReportId(reportId);
    }
}
