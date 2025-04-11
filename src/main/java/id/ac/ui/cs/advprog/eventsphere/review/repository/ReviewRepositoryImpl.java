package id.ac.ui.cs.advprog.eventsphere.review.repository;

import org.springframework.stereotype.Repository;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReviewRepositoryImpl implements ReviewRepository {

    private final List<Review> reviews = new ArrayList<>();

    @Override
    public void save(Review review) {
        // Implementation for persisting the review
        // For now, we'll just store in memory
        reviews.add(review);
    }
}
