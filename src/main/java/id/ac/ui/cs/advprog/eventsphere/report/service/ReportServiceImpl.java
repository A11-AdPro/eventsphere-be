package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportType;
import id.ac.ui.cs.advprog.eventsphere.report.observer.ReportSubject;
import id.ac.ui.cs.advprog.eventsphere.report.repository.ReportRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ReportSubject reportSubject;

    public ReportServiceImpl(ReportRepository reportRepository, ReportSubject reportSubject) {
        this.reportRepository = reportRepository;
        this.reportSubject = reportSubject;
    }

    @Override
    public Report createReport(UUID attendeeId, String title, String description, ReportType type) {
        Report report = new Report(
                UUID.randomUUID(),
                attendeeId,
                title,
                description,
                type,
                ReportStatus.PENDING,
                LocalDateTime.now()
        );

        Report savedReport = reportRepository.save(report);
        reportSubject.notifyNewReport(savedReport);

        return savedReport;
    }

    @Override
    public Optional<Report> getReportById(UUID reportId) {
        return reportRepository.findById(reportId);
    }

    @Override
    public List<Report> getReportsByAttendeeId(UUID attendeeId) {
        return reportRepository.findByAttendeeId(attendeeId);
    }

    @Override
    public List<Report> getReportsByStatus(ReportStatus status) {
        return reportRepository.findByStatus(status);
    }

    @Override
    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    @Override
    public boolean updateReportStatus(UUID reportId, ReportStatus newStatus) {
        Optional<Report> optionalReport = reportRepository.findById(reportId);

        if (optionalReport.isPresent()) {
            Report report = optionalReport.get();
            report.setStatus(newStatus);
            reportRepository.update(report);
            reportSubject.notifyStatusChange(report, newStatus);
            return true;
        }

        return false;
    }

    @Override
    public boolean deleteReport(UUID reportId) {
        Optional<Report> optionalReport = reportRepository.findById(reportId);

        if (optionalReport.isPresent()) {
            reportRepository.delete(reportId);
            return true;
        }

        return false;
    }
}
