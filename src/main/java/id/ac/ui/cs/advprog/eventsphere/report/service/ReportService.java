package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.report.dto.request.CreateReportCommentRequest;
import id.ac.ui.cs.advprog.eventsphere.report.dto.request.CreateReportRequest;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportCommentDTO;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportSummaryDTO;
import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.repository.ReportRepository;
import id.ac.ui.cs.advprog.eventsphere.report.repository.ReportResponseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    private final ReportRepository reportRepository;
    private final ReportResponseRepository responseRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Autowired
    public ReportService(
            ReportRepository reportRepository,
            ReportResponseRepository responseRepository,
            NotificationService notificationService,
            UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.responseRepository = responseRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    public ReportResponseDTO createReport(CreateReportRequest createRequest) {
        logger.info("Creating new report for user: {} - THREAD: {}",
                createRequest.getUserId(), Thread.currentThread().getName());

        // Ambil email pengguna dari repository jika tidak disediakan
        String userEmail = createRequest.getUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = userRepository.findById(createRequest.getUserId())
                    .map(User::getEmail)
                    .orElseThrow(() -> new EntityNotFoundException("User email not found for userId: " + createRequest.getUserId()));
        }

        // Membuat entitas Report dari request
        Report report = new Report();
        report.setUserId(createRequest.getUserId());
        report.setUserEmail(userEmail);
        report.setCategory(createRequest.getCategory());
        report.setDescription(createRequest.getDescription());
        report.setStatus(ReportStatus.PENDING);

        // Mendaftarkan observer
        report.getObservers().add(notificationService);

        // Menyimpan laporan
        Report savedReport = reportRepository.save(report);
        logger.info("Report saved with ID: {} - THREAD: {}",
                savedReport.getId(), Thread.currentThread().getName());

        // Memberi tahu admin tentang laporan baru - ASYNC
        logger.info("Starting async admin notification - THREAD: {}", Thread.currentThread().getName());
        notificationService.notifyNewReport(savedReport);

        logger.info("Report creation completed, async processing started - THREAD: {}",
                Thread.currentThread().getName());

        return convertToResponseDTO(savedReport);
    }

    public ReportResponseDTO getReportById(UUID id) {
        Report report = findReportById(id);
        return convertToResponseDTO(report);
    }

    public List<ReportSummaryDTO> getReportsByUserId(Long userId) {
        List<Report> reports = reportRepository.findByUserId(userId);
        return reports.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    public List<ReportSummaryDTO> getReportsByUserEmail(String email) {
        List<Report> reports = reportRepository.findByUserEmail(email);
        return reports.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    public List<ReportSummaryDTO> getReportsByStatus(ReportStatus status) {
        List<Report> reports;
        if (status != null) {
            reports = reportRepository.findByStatus(status);
        } else {
            reports = reportRepository.findAll();
        }
        return reports.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    public ReportResponseDTO updateReportStatus(UUID reportId, ReportStatus newStatus) {
        logger.info("Updating report status to {} for report: {} - THREAD: {}",
                newStatus, reportId, Thread.currentThread().getName());

        Report report = findReportById(reportId);

        // Register observer if not already registered
        if (!report.getObservers().contains(notificationService)) {
            report.getObservers().add(notificationService);
        }

        // Update the status - this will trigger async notification
        report.updateStatus(newStatus);

        // Save the updated report
        Report updatedReport = reportRepository.save(report);
        logger.info("Report status updated successfully - THREAD: {}", Thread.currentThread().getName());

        return convertToResponseDTO(updatedReport);
    }

    public ReportCommentDTO addComment(UUID reportId, CreateReportCommentRequest commentRequest) {
        logger.info("Adding comment to report: {} - THREAD: {}", reportId, Thread.currentThread().getName());

        Report report = findReportById(reportId);

        // Register observer if not already registered
        if (!report.getObservers().contains(notificationService)) {
            report.getObservers().add(notificationService);
        }

        // Create the comment entity
        ReportResponse commentEntity = new ReportResponse();
        commentEntity.setResponderId(commentRequest.getResponderId());
        commentEntity.setResponderEmail(commentRequest.getResponderEmail());
        commentEntity.setResponderRole(commentRequest.getResponderRole());
        commentEntity.setMessage(commentRequest.getMessage());

        // Add the comment to the report - this will trigger async notification
        report.addResponse(commentEntity);

        // Save the comment
        ReportResponse savedComment = responseRepository.save(commentEntity);

        // Auto-update status to ON_PROGRESS if it's currently PENDING
        if (report.getStatus() == ReportStatus.PENDING) {
            report.updateStatus(ReportStatus.ON_PROGRESS);
            reportRepository.save(report);
        }

        logger.info("Comment added successfully - THREAD: {}", Thread.currentThread().getName());
        return convertToCommentDTO(savedComment);
    }

    public void deleteReport(UUID reportId) {
        logger.info("Deleting report: {} - THREAD: {}", reportId, Thread.currentThread().getName());
        Report report = findReportById(reportId);
        reportRepository.delete(report);
        logger.info("Report deleted successfully - THREAD: {}", Thread.currentThread().getName());
    }

    private Report findReportById(UUID id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Report not found with id: " + id));
    }

    // Manual conversion methods
    private ReportResponseDTO convertToResponseDTO(Report report) {
        ReportResponseDTO dto = new ReportResponseDTO();
        dto.setId(report.getId());
        dto.setUserId(report.getUserId());
        dto.setUserEmail(report.getUserEmail());
        dto.setCategory(report.getCategory());
        dto.setDescription(report.getDescription());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setUpdatedAt(report.getUpdatedAt());

        // Convert responses to comment DTOs
        if (report.getResponses() != null) {
            List<ReportCommentDTO> comments = report.getResponses().stream()
                    .map(this::convertToCommentDTO)
                    .collect(Collectors.toList());
            dto.setComments(comments);
        }

        return dto;
    }

    private ReportSummaryDTO convertToSummaryDTO(Report report) {
        ReportSummaryDTO dto = new ReportSummaryDTO();
        dto.setId(report.getId());
        dto.setUserId(report.getUserId());
        dto.setUserEmail(report.getUserEmail());
        dto.setCategory(report.getCategory());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());

        // Create shortened description
        String desc = report.getDescription();
        if (desc != null) {
            if (desc.length() > 50) {
                dto.setShortDescription(desc.substring(0, 47) + "...");
            } else {
                dto.setShortDescription(desc);
            }
        }

        // Set comment count
        dto.setCommentCount(report.getResponses() != null ? report.getResponses().size() : 0);

        return dto;
    }

    private ReportCommentDTO convertToCommentDTO(ReportResponse comment) {
        ReportCommentDTO dto = new ReportCommentDTO();
        dto.setId(comment.getId());
        dto.setReportId(comment.getReport() != null ? comment.getReport().getId() : null);
        dto.setResponderId(comment.getResponderId());
        dto.setResponderEmail(comment.getResponderEmail());
        dto.setResponderRole(comment.getResponderRole());
        dto.setMessage(comment.getMessage());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}