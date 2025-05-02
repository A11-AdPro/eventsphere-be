package id.ac.ui.cs.advprog.eventsphere.review.repository;

import org.springframework.stereotype.Repository;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository {
    void save(Review review);

    List<Review> findByEventId(Long eventId);

    Optional<Review> findById(Long id);

    void update(Review review);

    void delete(Long id);

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    List<Review> findReportedReviews();

    List<Review> findByEventIdAndRatingGreaterThanEqual(Long eventId, Integer minRating);

    List<Review> findByEventIdAndCommentContaining(Long eventId, String keyword);
}
