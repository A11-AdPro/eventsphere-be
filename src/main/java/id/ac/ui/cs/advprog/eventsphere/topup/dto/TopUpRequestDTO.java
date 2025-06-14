package id.ac.ui.cs.advprog.eventsphere.topup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopUpRequestDTO {
    private int amount;
    private String topUpType; // FIXED or CUSTOM
    // private String paymentMethod; // STANDARD
}