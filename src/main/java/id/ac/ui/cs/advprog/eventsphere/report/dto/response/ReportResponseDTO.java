package id.ac.ui.cs.advprog.eventsphere.report.dto.response;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class ReportResponseDTO {
    private UUID id;
    private Long userId;
    private String userEmail;
    private ReportCategory category;
    private String description;
    private ReportStatus status;
    private List<String> attachments = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReportCommentDTO> comments = new ArrayList<>();
}