package id.ac.ui.cs.advprog.eventsphere;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class EventSphereApplicationTest {

    @Test
    void testMain() {
        // Mock SpringApplication.run to prevent the full application context from starting
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(EventSphereApplication.class, new String[]{}))
                  .thenReturn(Mockito.mock(ConfigurableApplicationContext.class));

            EventSphereApplication.main(new String[]{}); //

            // Verify that SpringApplication.run was called
            mocked.verify(() -> SpringApplication.run(EventSphereApplication.class, new String[]{}), times(1));
        }
    }
}