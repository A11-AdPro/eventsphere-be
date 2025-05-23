package id.ac.ui.cs.advprog.eventsphere.review.listener;

import id.ac.ui.cs.advprog.eventsphere.review.event.ReviewCreatedEvent;
import id.ac.ui.cs.advprog.eventsphere.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener that updates event ratings when reviews are created.
 * Part of the Observer pattern implementation for the review system.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventRatingUpdateListener {

    private final ReviewRepository reviewRepository;

    @Async
    @EventListener
    public void handleReviewCreatedEvent(ReviewCreatedEvent event) {
        Long eventId = event.getReview().getEventId();
        Double avgRating = reviewRepository.calculateAverageRatingForEvent(eventId);

        log.info("Event {} average rating updated to {}", eventId, avgRating);

        // Here we would update the event's rating in the event service
        // This demonstrates loose coupling between review and event modules
        // eventService.updateEventRating(eventId, avgRating);
    }
}
