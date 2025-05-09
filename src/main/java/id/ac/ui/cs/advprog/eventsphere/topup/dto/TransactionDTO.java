package id.ac.ui.cs.advprog.eventsphere.topup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private String id;
    private Long userId;
    private String username;
    private int amount;
    private LocalDateTime timestamp;
    private String type;
    private String status;
    private String description;
    private String eventId;
}