package id.ac.ui.cs.advprog.eventsphere.topup.strategy;

import id.ac.ui.cs.advprog.eventsphere.topup.model.CustomTopUp;
import id.ac.ui.cs.advprog.eventsphere.topup.model.FixedTopUp;
import id.ac.ui.cs.advprog.eventsphere.topup.model.TopUp;
import org.springframework.stereotype.Component;

@Component
public class TopUpFactory {

    public static final int SMALL_FIXED = 50000;
    public static final int MEDIUM_FIXED = 100000;
    public static final int LARGE_FIXED = 500000;

    public TopUp createTopUp(String type, int amount) {
        return switch (type) {
            case "CUSTOM" -> new CustomTopUp(amount);
            case "FIXED" -> new FixedTopUp(amount);
            default -> throw new IllegalArgumentException("Invalid top-up type");
        };
    }

    public TopUp createSmallTopUp() {
        return new FixedTopUp(SMALL_FIXED);
    }

    public TopUp createMediumTopUp() {
        return new FixedTopUp(MEDIUM_FIXED);
    }

    public TopUp createLargeTopUp() {
        return new FixedTopUp(LARGE_FIXED);
    }
}