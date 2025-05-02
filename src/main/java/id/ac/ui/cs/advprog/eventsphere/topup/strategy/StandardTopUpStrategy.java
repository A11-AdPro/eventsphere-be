package id.ac.ui.cs.advprog.eventsphere.topup.strategy;

import id.ac.ui.cs.advprog.eventsphere.topup.entity.User;
import id.ac.ui.cs.advprog.eventsphere.topup.model.TopUp;

import org.springframework.stereotype.Component;

@Component
public class StandardTopUpStrategy implements TopUpStrategy {
    @Override
    public void executeTopUp(User user, TopUp topUp) {
        user.topUp(topUp.getAmount());
    }
}