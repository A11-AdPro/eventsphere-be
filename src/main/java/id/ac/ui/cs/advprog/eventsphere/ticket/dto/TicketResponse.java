package id.ac.ui.cs.advprog.eventsphere.ticket.dto;

import id.ac.ui.cs.advprog.eventsphere.ticket.model.TicketCategory;

public class TicketResponse {
    private Long id;
    private String name;
    private double price;
    private int quota;
    private TicketCategory category;
    private boolean soldOut;
    private Long eventId;

    private TicketResponse(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.price = builder.price;
        this.quota = builder.quota;
        this.category = builder.category;
        this.soldOut = builder.soldOut;
        this.eventId = builder.eventId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private double price;
        private int quota;
        private TicketCategory category;
        private boolean soldOut;
        private Long eventId;


        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder price(double price) {
            this.price = price;
            return this;
        }

        public Builder quota(int quota) {
            this.quota = quota;
            return this;
        }

        public Builder category(TicketCategory category) {
            this.category = category;
            return this;
        }

        public Builder soldOut(boolean soldOut) {
            this.soldOut = soldOut;
            return this;
        }

        public Builder eventId(Long eventId) {
            this.eventId = eventId;
            return this;
        }

        public TicketResponse build() {
            return new TicketResponse(this);
        }
    }



    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuota() {
        return quota;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public boolean isSoldOut() {
        return soldOut;
    }

    public Long getEventId() {
        return eventId;
    }
}






