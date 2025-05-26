package id.ac.ui.cs.advprog.eventsphere.report.model;

import lombok.Getter;

@Getter
public enum ReportStatus {
    PENDING("Pending"),
    ON_PROGRESS("On Progress"),
    RESOLVED("Resolved");

    private final String displayName;

    ReportStatus(String displayName) {
        this.displayName = displayName;
    }

}
