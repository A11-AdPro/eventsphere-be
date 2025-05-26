package id.ac.ui.cs.advprog.eventsphere.review.event;

import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReviewDeletedEvent extends ApplicationEvent {
    private final Review review;

    public ReviewDeletedEvent(Object source, Review review) {
        super(source);
        this.review = review;
    }
}
