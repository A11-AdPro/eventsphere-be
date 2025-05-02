package id.ac.ui.cs.advprog.eventsphere.topup.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import id.ac.ui.cs.advprog.eventsphere.topup.entity.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    public enum TransactionType {
        TOP_UP,
        TICKET_PURCHASE
    }

    public enum TransactionStatus {
        SUCCESS,
        FAILED,
        PENDING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private int amount;
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private String description;

    // Additional field to store event ID for ticket purchases
    private String eventId;
}