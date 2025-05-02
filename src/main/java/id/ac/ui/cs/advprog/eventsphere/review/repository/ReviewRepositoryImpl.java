package id.ac.ui.cs.advprog.eventsphere.review.repository;

import org.springframework.stereotype.Repository;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ReviewRepositoryImpl implements ReviewRepository {

    private final List<Review> reviews = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public void save(Review review) {
        if (review.getId() == null) {
            review.setId(idGenerator.getAndIncrement());
        }
        reviews.add(review);
    }

    @Override
    public List<Review> findByEventId(Long eventId) {
        return reviews.stream()
                .filter(review -> review.getEventId().equals(eventId))
                .filter(review -> !review.isDeleted())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Review> findById(Long id) {
        return reviews.stream()
                .filter(review -> review.getId().equals(id))
                .findFirst();
    }

    @Override
    public void update(Review updatedReview) {
        for (int i = 0; i < reviews.size(); i++) {
            Review review = reviews.get(i);
            if (review.getId().equals(updatedReview.getId())) {
                reviews.set(i, updatedReview);
                break;
            }
        }
    }

    @Override
    public void delete(Long id) {
        Optional<Review> reviewOpt = findById(id);
        reviewOpt.ifPresent(review -> {
            review.setDeleted(true);
            update(review);
        });
    }

    @Override
    public boolean existsByEventIdAndUserId(Long eventId, Long userId) {
        return reviews.stream()
                .anyMatch(review -> review.getEventId().equals(eventId) &&
                        review.getUserId().equals(userId) &&
                        !review.isDeleted());
    }

    @Override
    public List<Review> findReportedReviews() {
        return reviews.stream()
                .filter(Review::isReported)
                .filter(review -> !review.isDeleted())
                .collect(Collectors.toList());
    }

    @Override
    public List<Review> findByEventIdAndRatingGreaterThanEqual(Long eventId, Integer minRating) {
        return reviews.stream()
                .filter(review -> review.getEventId().equals(eventId))
                .filter(review -> review.getRating() >= minRating)
                .filter(review -> !review.isDeleted())
                .collect(Collectors.toList());
    }

    @Override
    public List<Review> findByEventIdAndCommentContaining(Long eventId, String keyword) {
        return reviews.stream()
                .filter(review -> review.getEventId().equals(eventId))
                .filter(review -> review.getComment() != null &&
                        review.getComment().toLowerCase().contains(keyword.toLowerCase()))
                .filter(review -> !review.isDeleted())
                .collect(Collectors.toList());
    }
}
