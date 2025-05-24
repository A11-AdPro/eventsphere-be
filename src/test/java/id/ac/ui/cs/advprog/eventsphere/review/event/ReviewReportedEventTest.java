package id.ac.ui.cs.advprog.eventsphere.review.event;

import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewReport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ReviewReportedEventTest {

    @Test
    void testReviewReportedEvent() {
        Object source = new Object();
        ReviewReport mockReport = mock(ReviewReport.class);

        ReviewReportedEvent event = new ReviewReportedEvent(source, mockReport);

        assertEquals(source, event.getSource());
        assertEquals(mockReport, event.getReport());
    }
}

