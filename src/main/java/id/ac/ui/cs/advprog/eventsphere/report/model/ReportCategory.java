package id.ac.ui.cs.advprog.eventsphere.report.model;

import lombok.Getter;

@Getter
public enum ReportCategory {
    PAYMENT("Payment Issue"),
    TICKET("Ticket Issue"),
    EVENT("Event Issue"),
    OTHER("Other Issue");

    private final String displayName;

    ReportCategory(String displayName) {
        this.displayName = displayName;
    }

}