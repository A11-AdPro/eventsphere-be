package id.ac.ui.cs.advprog.eventsphere.topup.strategy;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.UserBalanceService;
import id.ac.ui.cs.advprog.eventsphere.topup.model.FixedTopUp;
import id.ac.ui.cs.advprog.eventsphere.topup.model.TopUp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StandardTopUpStrategyTest {
    
    @Mock
    private UserBalanceService userBalanceService;
    
    private StandardTopUpStrategy topUpStrategy;
    
    @BeforeEach
    public void setUp() {
        topUpStrategy = new StandardTopUpStrategy(userBalanceService);
    }
    
    @Test
    @DisplayName("Should execute top up via UserBalanceService")
    public void testExecuteTopUp() {
        User user = new User();
        TopUp topUp = new FixedTopUp(50000);
        
        topUpStrategy.executeTopUp(user, topUp);
        
        verify(userBalanceService, times(1)).topUp(user, 50000);
    }
}