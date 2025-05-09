package id.ac.ui.cs.advprog.eventsphere.report.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Report {
    private final UUID id;
    private final UUID attendeeId;
    private final String title;
    private final String description;
    private final ReportType type;
    private final LocalDateTime createdAt;

    @Setter
    private ReportStatus status;

    public Report(UUID id, UUID attendeeId, String title, String description, ReportType type, ReportStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.attendeeId = attendeeId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
    }
}

