package id.ac.ui.cs.advprog.eventsphere.review.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewCreatedEvent;

@Component
public class ReviewEventListener {

    @EventListener
    public void handleReviewCreatedEvent(ReviewCreatedEvent event) {
        // This could:
        // 1. Send notifications to event organizers
        // 2. Recalculate average ratings for an event
        // 3. Update analytics
        System.out.println("New review received: " + event.getReview().getComment());
    }
}
