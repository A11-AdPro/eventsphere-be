package id.ac.ui.cs.advprog.eventsphere.review.repository;

import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByEventId(Long eventId);

    Page<Review> findByEventIdOrderByCreatedAtDesc(Long eventId, Pageable pageable);

    Page<Review> findByEventIdOrderByRatingDesc(Long eventId, Pageable pageable);

    Page<Review> findByEventIdOrderByRatingAsc(Long eventId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.eventId = :eventId AND r.content LIKE %:keyword%")
    Page<Review> findByEventIdAndContentContaining(Long eventId, String keyword, Pageable pageable);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    boolean existsByTicketId(Long ticketId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.eventId = :eventId")
    Double calculateAverageRatingForEvent(Long eventId);

    List<Review> findByIsReportedTrue();

    Page<Review> findByIsVisibleTrue(Pageable pageable);
}
