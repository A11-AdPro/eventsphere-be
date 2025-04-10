package id.ac.ui.cs.advprog.eventsphere.topup.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TopUpTransaction {
    private Long id;
    private Long attendeeId;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String status; // PENDING, SUCCESS, FAILED

    // Default constructor
    public TopUpTransaction() {
        this.timestamp = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Parameterized constructor
    public TopUpTransaction(Long attendeeId, BigDecimal amount) {
        this();
        this.attendeeId = attendeeId;
        this.amount = amount;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAttendeeId() {
        return attendeeId;
    }

    public void setAttendeeId(Long attendeeId) {
        this.attendeeId = attendeeId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}