package id.ac.ui.cs.advprog.eventsphere.model;

public class Ticket {

    private Long id;
    private String name;
    private double price;
    private int quota;
    private int sold;
    private boolean deleted;
    private Long eventId;
    private TicketCategory category;

    // Constructor sesuai dengan penggunaan di TicketServiceImpl
    public Ticket(Long id, String name, double price, int quota, TicketCategory category, Long eventId) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quota = quota;
        this.category = category;
        this.eventId = eventId;
        this.sold = 0;
        this.deleted = false;
    }

    public boolean isSoldOut() {
        return sold >= quota;
    }

    public void updateDetails(double price, int quota) {
        this.price = price;
        this.quota = quota;
    }

    public void purchase() {
        if (isSoldOut()) throw new IllegalStateException("Ticket is sold out");
        this.sold++;
    }

    public void markDeleted() {
        this.deleted = true;
    }

    public boolean isDeleted() {
        return deleted;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuota() { return quota; }
    public void setQuota(int quota) { this.quota = quota; }

    public int getSold() { return sold; }
    public void setSold(int sold) { this.sold = sold; }

    public TicketCategory getCategory() { return category; }
    public void setCategory(TicketCategory category) { this.category = category; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
}

