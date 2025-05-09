package id.ac.ui.cs.advprog.eventsphere.report.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportResponseRepository responseRepository;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;

    @Autowired
    public ReportService(
            ReportRepository reportRepository,
            ReportResponseRepository responseRepository,
            NotificationService notificationService,
            FileStorageService fileStorageService) {
        this.reportRepository = reportRepository;
        this.responseRepository = responseRepository;
        this.notificationService = notificationService;
        this.fileStorageService = fileStorageService;
    }

    public ReportResponseDTO createReport(CreateReportRequest createRequest, List<MultipartFile> attachments) throws IOException {
        // Create new Report entity from request
        Report report = new Report();
        report.setUserId(createRequest.getUserId());
        report.setCategory(createRequest.getCategory());
        report.setDescription(createRequest.getDescription());
        report.setStatus(ReportStatus.PENDING);

        // Register observer
        report.getObservers().add(notificationService);

        // Process attachments
        if (attachments != null && !attachments.isEmpty()) {
            for (MultipartFile file : attachments) {
                if (!file.isEmpty()) {
                    String storedFileName = fileStorageService.storeFile(file);
                    report.getAttachments().add(storedFileName);
                }
            }
        }

        // Save the report
        Report savedReport = reportRepository.save(report);

        // Notify admins about the new report
        notificationService.notifyNewReport(savedReport);

        // Convert entity to response DTO
        return convertToResponseDTO(savedReport);
    }

    public ReportResponseDTO getReportById(UUID id) {
        Report report = findReportById(id);
        return convertToResponseDTO(report);
    }

    public List<ReportSummaryDTO> getReportsByUserId(UUID userId) {
        List<Report> reports = reportRepository.findByUserId(userId);
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
        Report report = findReportById(reportId);

        // Register observer if not already registered
        if (!report.getObservers().contains(notificationService)) {
            report.getObservers().add(notificationService);
        }

        // Update the status
        report.updateStatus(newStatus);

        // Save the updated report
        Report updatedReport = reportRepository.save(report);

        // Convert entity to response DTO
        return convertToResponseDTO(updatedReport);
    }

    public ReportCommentDTO addComment(UUID reportId, CreateReportCommentRequest commentRequest) {
        Report report = findReportById(reportId);

        // Register observer if not already registered
        if (!report.getObservers().contains(notificationService)) {
            report.getObservers().add(notificationService);
        }

        // Create the comment entity
        ReportResponse commentEntity = new ReportResponse();
        commentEntity.setResponderId(commentRequest.getResponderId());
        commentEntity.setResponderRole(commentRequest.getResponderRole());
        commentEntity.setMessage(commentRequest.getMessage());

        // Add the comment to the report
        report.addResponse(commentEntity);

        // Save the comment
        ReportResponse savedComment = responseRepository.save(commentEntity);

        // Auto-update status to ON_PROGRESS if it's currently PENDING
        if (report.getStatus() == ReportStatus.PENDING) {
            report.updateStatus(ReportStatus.ON_PROGRESS);
            reportRepository.save(report);
        }

        // Convert entity to DTO
        return convertToCommentDTO(savedComment);
    }

    public void deleteReport(UUID reportId) throws IOException {
        Report report = findReportById(reportId);

        // Delete attachments
        for (String attachmentPath : report.getAttachments()) {
            fileStorageService.deleteFile(attachmentPath);
        }

        // Delete the report (this will cascade delete responses as well)
        reportRepository.delete(report);
    }

    private Report findReportById(UUID id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Report not found with id: " + id));
    }

    // Manual conversion methods instead of using Mappers

    private ReportResponseDTO convertToResponseDTO(Report report) {
        ReportResponseDTO dto = new ReportResponseDTO();
        dto.setId(report.getId());
        dto.setUserId(report.getUserId());
        dto.setCategory(report.getCategory());
        dto.setDescription(report.getDescription());
        dto.setStatus(report.getStatus());
        dto.setAttachments(new ArrayList<>(report.getAttachments()));
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
        dto.setResponderRole(comment.getResponderRole());
        dto.setMessage(comment.getMessage());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}