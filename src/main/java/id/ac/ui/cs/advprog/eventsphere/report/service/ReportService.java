package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.event.service.EventService;
import id.ac.ui.cs.advprog.eventsphere.event.dto.EventResponseDTO;
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
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportResponseRepository responseRepository;
    private final NotificationService notificationService;
    private final AsyncNotificationService asyncNotificationService;
    private final UserRepository userRepository;
    private final EventService eventService;

    @Autowired
    public ReportService(
            ReportRepository reportRepository,
            ReportResponseRepository responseRepository,
            NotificationService notificationService,
            AsyncNotificationService asyncNotificationService,
            UserRepository userRepository,
            EventService eventService) {
        this.reportRepository = reportRepository;
        this.responseRepository = responseRepository;
        this.notificationService = notificationService;
        this.asyncNotificationService = asyncNotificationService;
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    @Transactional
    public ReportResponseDTO createReport(CreateReportRequest createRequest) {
        String userEmail = createRequest.getUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = userRepository.findById(createRequest.getUserId())
                    .map(User::getEmail)
                    .orElseThrow(() -> new EntityNotFoundException("User email not found for userId: " + createRequest.getUserId()));
        }

        Report report = new Report();
        report.setUserId(createRequest.getUserId());
        report.setUserEmail(userEmail);
        report.setEventId(createRequest.getEventId());
        report.setEventTitle(createRequest.getEventTitle());
        report.setCategory(createRequest.getCategory());
        report.setDescription(createRequest.getDescription());
        report.setStatus(ReportStatus.PENDING);

        // Add notification service as observer
        // report.getObservers().add(notificationService);

        Report savedReport = reportRepository.save(report);

        // Process notifications asynchronously
        asyncNotificationService.processNewReportNotificationsAsync(savedReport);

        // notificationService.notifyNewReportSync(savedReport);

        if (savedReport.getEventId() != null) {
            try {
                EventResponseDTO event = eventService.getActiveEventById(savedReport.getEventId());
                notifyEventOrganizer(savedReport, event.getOrganizerId());
            } catch (Exception e) {
                System.out.println("Could not find event for notification: " + e.getMessage());
            }
        }

        return convertToResponseDTO(savedReport);
    }


    private void notifyEventOrganizer(Report report, Long organizerId) {
        try {
            String organizerEmail = userRepository.findById(organizerId)
                    .map(User::getEmail)
                    .orElse(null);

            if (organizerEmail != null) {
                // Create a simple notification for the organizer
                // You can expand this based on your NotificationService implementation
                System.out.println("Notifying organizer " + organizerEmail + " about new report for event " + report.getEventId());
            }
        } catch (Exception e) {
            System.out.println("Could not notify organizer: " + e.getMessage());
        }
    }

    public boolean isReportFromOrganizerEvent(UUID reportId, Long organizerId) {
        Report report = findReportById(reportId);
        if (report.getEventId() == null) {
            return false; // General reports can't be accessed by organizers
        }

        try {
            EventResponseDTO event = eventService.getActiveEventById(report.getEventId());
            return event.getOrganizerId().equals(organizerId);
        } catch (Exception e) {
            return false;
        }
    }

    public List<ReportSummaryDTO> getReportsByOrganizerEventsAndStatus(Long organizerId, ReportStatus status) {
        List<EventResponseDTO> organizerEvents = eventService.getActiveEventsByOrganizer(
                userRepository.findById(organizerId)
                        .orElseThrow(() -> new EntityNotFoundException("Organizer not found"))
        );

        List<Long> eventIds = organizerEvents.stream()
                .map(EventResponseDTO::getId)
                .collect(Collectors.toList());

        List<Report> reports;
        if (status != null) {
            reports = eventIds.stream()
                    .flatMap(eventId -> reportRepository.findByEventIdAndStatus(eventId, status).stream())
                    .collect(Collectors.toList());
        } else {
            reports = eventIds.stream()
                    .flatMap(eventId -> reportRepository.findByEventId(eventId).stream())
                    .collect(Collectors.toList());
        }

        return reports.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    public List<ReportSummaryDTO> getReportsByEventId(Long eventId) {
        List<Report> reports = reportRepository.findByEventId(eventId);
        return reports.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
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

    @Transactional
    public ReportResponseDTO updateReportStatus(UUID reportId, ReportStatus newStatus) {
        Report report = findReportById(reportId);

        // Store old status for async notification
        ReportStatus oldStatus = report.getStatus();

        report.setStatus(newStatus);
        report.setUpdatedAt(LocalDateTime.now());

        Report updatedReport = reportRepository.save(report);

        // Process status change notifications asynchronously
        asyncNotificationService.processStatusChangeNotificationsAsync(updatedReport, oldStatus, newStatus);

        return convertToResponseDTO(updatedReport);
    }

    public ReportCommentDTO addComment(UUID reportId, CreateReportCommentRequest commentRequest) {
        Report report = findReportById(reportId);

        // Ensure notification service is added as observer
        if (!report.getObservers().contains(notificationService)) {
            report.getObservers().add(notificationService);
        }

        ReportResponse commentEntity = new ReportResponse();
        commentEntity.setResponderId(commentRequest.getResponderId());
        commentEntity.setResponderEmail(commentRequest.getResponderEmail());
        commentEntity.setResponderRole(commentRequest.getResponderRole());
        commentEntity.setMessage(commentRequest.getMessage());

        boolean isReportOwner = report.getUserEmail().equals(commentRequest.getResponderEmail()) ||
                report.getUserId().equals(commentRequest.getResponderId());

        // Add response to report - this will trigger the observer pattern
        // report.addResponse(commentEntity);

        report.getResponses().add(commentEntity);
        commentEntity.setReport(report);

        report.setUpdatedAt(LocalDateTime.now());

        ReportResponse savedComment = responseRepository.save(commentEntity);

        // Process response notifications asynchronously
        asyncNotificationService.processResponseNotificationsAsync(report, savedComment);

        // Auto-update status if needed
        if (report.getStatus() == ReportStatus.PENDING && !isReportOwner) {
            ReportStatus oldStatus = report.getStatus();

            report.setStatus(ReportStatus.ON_PROGRESS);
            report.setUpdatedAt(LocalDateTime.now());

            // Process status change asynchronously
            asyncNotificationService.processStatusChangeNotificationsAsync(report, oldStatus, ReportStatus.ON_PROGRESS);
        }

        reportRepository.save(report);
        return convertToCommentDTO(savedComment);
    }

    public void deleteReport(UUID reportId) {
        Report report = findReportById(reportId);
        reportRepository.delete(report);
    }

    private Report findReportById(UUID id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Report not found with id: " + id));
    }

    private ReportResponseDTO convertToResponseDTO(Report report) {
        ReportResponseDTO dto = new ReportResponseDTO();
        dto.setId(report.getId());
        dto.setUserId(report.getUserId());
        dto.setUserEmail(report.getUserEmail());
        dto.setEventId(report.getEventId());
        dto.setEventTitle(report.getEventTitle());
        dto.setCategory(report.getCategory());
        dto.setDescription(report.getDescription());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setUpdatedAt(report.getUpdatedAt());

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
        dto.setEventId(report.getEventId());
        dto.setEventTitle(report.getEventTitle());
        dto.setCategory(report.getCategory());
        dto.setStatus(report.getStatus());
        dto.setCreatedAt(report.getCreatedAt());

        String desc = report.getDescription();
        if (desc != null) {
            if (desc.length() > 50) {
                dto.setShortDescription(desc.substring(0, 47) + "...");
            } else {
                dto.setShortDescription(desc);
            }
        }

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