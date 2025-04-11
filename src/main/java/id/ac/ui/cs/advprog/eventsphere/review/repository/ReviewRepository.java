package id.ac.ui.cs.advprog.eventsphere.review.repository;

import org.springframework.stereotype.Repository;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;

@Repository
public interface ReviewRepository {
    void save(Review review);
}
