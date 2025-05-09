package id.ac.ui.cs.advprog.eventsphere.topup.model;

public class CustomTopUp implements TopUp {
    private final int amount;
    private final String type;
    private final int MIN = 10000;
    private final int MAX = 1000000;

    public CustomTopUp(int amount) {
        if (amount < MIN || amount > MAX) {
            throw new IllegalArgumentException("Custom top-up amount must be between 10,000 and 1,000,000");
        }
        this.amount = amount;
        this.type = "CUSTOM";
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public String getType() {
        return type;
    }
}