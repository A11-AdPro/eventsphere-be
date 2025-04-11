package id.ac.ui.cs.advprog.eventsphere.topup.model;

public class FixedTopUp implements TopUp {
    private final int amount;

    public FixedTopUp(int amount) {
        this.amount = amount;
    }

    @Override
    public int getAmount() {
        return amount;
    }
}
