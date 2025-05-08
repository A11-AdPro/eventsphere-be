package id.ac.ui.cs.advprog.eventsphere.report.model;

import id.ac.ui.cs.advprog.eventsphere.report.observer.ReportObserver;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportCategory category;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @ElementCollection
    @CollectionTable(name = "report_attachments", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "attachment_url")
    private List<String> attachments = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportResponse> responses = new ArrayList<>();

    @Transient
    private List<ReportObserver> observers = new ArrayList<>();

    public void updateStatus(ReportStatus newStatus) {
        ReportStatus oldStatus = this.status;
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();

        // Notify observers
        for (ReportObserver observer : observers) {
            observer.onStatusChanged(this, oldStatus, newStatus);
        }
    }

    public void addResponse(ReportResponse response) {
        responses.add(response);
        response.setReport(this);

        // Notify observers
        for (ReportObserver observer : observers) {
            observer.onResponseAdded(this, response);
        }
    }
}