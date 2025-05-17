package id.ac.ui.cs.advprog.eventsphere.review.listener;

import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewCreatedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener that sends notifications when reviews are created.
 * Another part of the Observer pattern implementation.
 */
@Slf4j
@Component
public class ReviewNotificationListener {

    @Async
    @EventListener
    public void handleReviewCreatedEvent(ReviewCreatedEvent event) {
        Review review = event.getReview();

        log.info("Sending notification for new review on event {} by user {}",
                review.getEventId(), review.getUser().getUsername());

        // Here we would use a notification service to send alerts
        // This could be to the event organizer, admin, etc.
        // notificationService.sendReviewNotification(review);
    }
}
