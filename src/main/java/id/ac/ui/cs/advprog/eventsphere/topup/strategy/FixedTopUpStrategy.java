package id.ac.ui.cs.advprog.eventsphere.topup.strategy;

import id.ac.ui.cs.advprog.eventsphere.topup.model.FixedTopUp;
import id.ac.ui.cs.advprog.eventsphere.topup.model.TopUp;
import java.util.Arrays;
import java.util.List;

public class FixedTopUpStrategy implements TopUpStrategy {

    private final List<Integer> validAmounts = Arrays.asList(50000, 100000, 150000, 200000);

    @Override
    public TopUp createTopUp(int amount) {
        if (!validAmounts.contains(amount)) {
            throw new IllegalArgumentException("Invalid fixed top-up amount. Valid amounts are: " + validAmounts);
        }
        return new FixedTopUp(amount);
    }
}