package id.ac.ui.cs.advprog.eventsphere.topup.strategy;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.UserBalanceService;
import id.ac.ui.cs.advprog.eventsphere.topup.model.TopUp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StandardTopUpStrategy implements TopUpStrategy {
    private final UserBalanceService userBalanceService;
    
    @Autowired
    public StandardTopUpStrategy(UserBalanceService userBalanceService) {
        this.userBalanceService = userBalanceService;
    }
    
    @Override
    public void executeTopUp(User user, TopUp topUp) {
        userBalanceService.topUp(user, topUp.getAmount());
    }
}