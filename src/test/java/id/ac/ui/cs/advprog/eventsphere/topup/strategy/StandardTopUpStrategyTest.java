package id.ac.ui.cs.advprog.eventsphere.topup.strategy;

import id.ac.ui.cs.advprog.eventsphere.topup.entity.User;
import id.ac.ui.cs.advprog.eventsphere.topup.model.TopUp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StandardTopUpStrategyTest {

    @Test
    void testExecuteTopUpShouldAddBalance() {
        User user = new User();
        user.setBalance(10000);

        TopUp topUp = mock(TopUp.class);
        when(topUp.getAmount()).thenReturn(5000);

        StandardTopUpStrategy strategy = new StandardTopUpStrategy();
        strategy.executeTopUp(user, topUp);

        assertEquals(15000, user.getBalance());
    }
}
