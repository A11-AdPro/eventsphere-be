package id.ac.ui.cs.advprog.eventsphere.topup.model;

public class FixedTopUp implements TopUp {
    private final int amount;
    private final String type;

    public FixedTopUp(int amount) {
        this.amount = amount;
        this.type = "FIXED";
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