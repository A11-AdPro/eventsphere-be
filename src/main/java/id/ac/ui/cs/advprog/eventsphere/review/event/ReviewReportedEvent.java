package id.ac.ui.cs.advprog.eventsphere.review.event;

import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewReport;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReviewReportedEvent extends ApplicationEvent {
    private final ReviewReport report;

    public ReviewReportedEvent(Object source, ReviewReport report) {
        super(source);
        this.report = report;
    }
}
