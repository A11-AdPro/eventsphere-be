package id.ac.ui.cs.advprog.eventsphere.review.event;

import org.springframework.context.ApplicationEvent;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;

public class ReviewCreatedEvent extends ApplicationEvent {
    private final Review review;

    public ReviewCreatedEvent(Object source, Review review) {
        super(source);
        this.review = review;
    }

    public Review getReview() {
        return review;
    }
}