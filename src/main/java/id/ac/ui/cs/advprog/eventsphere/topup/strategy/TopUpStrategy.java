package id.ac.ui.cs.advprog.eventsphere.topup.strategy;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.topup.model.TopUp;

public interface TopUpStrategy {
    void executeTopUp(User user, TopUp topUp);
}