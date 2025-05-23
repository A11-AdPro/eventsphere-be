package id.ac.ui.cs.advprog.eventsphere.review.repository;

import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findByReviewId(Long reviewId);
    int countByReviewId(Long reviewId);
}
