package id.ac.ui.cs.advprog.eventsphere.topup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestDTO {
    private String userId;
    private String eventId;
    private int amount;
    private String description;
}