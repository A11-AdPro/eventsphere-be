package id.ac.ui.cs.advprog.eventsphere.topup.strategy;

import id.ac.ui.cs.advprog.eventsphere.topup.model.TopUp;

public interface TopUpStrategy {
    TopUp createTopUp(int amount);
}