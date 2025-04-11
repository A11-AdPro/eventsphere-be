package id.ac.ui.cs.advprog.eventsphere.dto;

import id.ac.ui.cs.advprog.eventsphere.model.TicketCategory;

public class TicketRequest {
    public String name;
    public double price;
    public int quota;
    public TicketCategory category;
    public Long eventId;

    public TicketRequest(String name, double price, int quota, TicketCategory category, Long eventId) {
        this.name = name;
        this.price = price;
        this.quota = quota;
        this.category = category;
        this.eventId = eventId;
    }

    public TicketRequest() {} // default constructor
}
