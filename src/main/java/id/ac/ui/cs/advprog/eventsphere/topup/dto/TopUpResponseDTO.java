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
public class TopUpResponseDTO {
    private String transactionId;
    private String userId;
    private int amount;
    private int newBalance;
    private LocalDateTime timestamp;
    private String status;
}