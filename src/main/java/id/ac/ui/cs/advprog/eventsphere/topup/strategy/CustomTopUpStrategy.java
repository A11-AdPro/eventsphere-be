package id.ac.ui.cs.advprog.eventsphere.topup.strategy;

import id.ac.ui.cs.advprog.eventsphere.topup.model.CustomTopUp;
import id.ac.ui.cs.advprog.eventsphere.topup.model.TopUp;

public class CustomTopUpStrategy implements TopUpStrategy {

    private static final int MIN_AMOUNT = 10000;
    private static final int MAX_AMOUNT = 1000000;

    @Override
    public TopUp createTopUp(int amount) {
        return new CustomTopUp(amount);
    }
}