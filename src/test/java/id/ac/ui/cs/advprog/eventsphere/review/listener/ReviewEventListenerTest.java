package id.ac.ui.cs.advprog.eventsphere.review.listener;

import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewCreatedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewEventListenerTest {

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final PrintStream originalSystemOut = System.out;

    @Mock
    private ReviewCreatedEvent event;

    @Mock
    private Review review;

    @InjectMocks
    private ReviewEventListener listener;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalSystemOut);
    }

    @Test
    void testHandleReviewCreatedEvent() {
        // Given
        String comment = "Excellent workshop";
        when(event.getReview()).thenReturn(review);
        when(review.getComment()).thenReturn(comment);

        // When
        listener.handleReviewCreatedEvent(event);

        // Then
        assertTrue(outputStreamCaptor.toString().trim().contains("New review received: " + comment));
    }
}