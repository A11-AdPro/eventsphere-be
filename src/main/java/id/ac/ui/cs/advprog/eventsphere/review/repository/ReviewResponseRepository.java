package id.ac.ui.cs.advprog.eventsphere.review.repository;

import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewResponseRepository extends JpaRepository<ReviewResponse, Long> {
    List<ReviewResponse> findByReviewId(Long reviewId);
}
