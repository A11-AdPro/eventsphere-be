package id.ac.ui.cs.advprog.eventsphere.ticket.dto;

import id.ac.ui.cs.advprog.eventsphere.ticket.model.TicketCategory;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequest {
    private String name;
    private double price;
    private int quota;
    private TicketCategory category;
    private Long eventId;
}