package id.ac.ui.cs.advprog.eventsphere.review.repository;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.repository.UserRepository;
import id.ac.ui.cs.advprog.eventsphere.review.model.Review;
import id.ac.ui.cs.advprog.eventsphere.review.model.ReviewReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ReviewReportRepositoryTest {

    @Autowired
    private ReviewReportRepository reviewReportRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    private User reporterUser;
    private Review review1;
    private ReviewReport report1;
    private ReviewReport report2;

    @BeforeEach
    void setUp() {
        reviewReportRepository.deleteAll();
        reviewRepository.deleteAll();
        userRepository.deleteAll();

        reporterUser = User.builder()
                .email("reporter@example.com")
                .password("password")
                .fullName("Reporter User") // Combined firstName and lastName
                .role(id.ac.ui.cs.advprog.eventsphere.authentication.model.Role.ATTENDEE)
                .build();
        userRepository.saveAndFlush(reporterUser);

        User reviewAuthor = User.builder()
                .email("author@example.com")
                .password("password")
                .fullName("Author User") // Combined firstName and lastName
                .role(id.ac.ui.cs.advprog.eventsphere.authentication.model.Role.ATTENDEE)
                .build();
        userRepository.saveAndFlush(reviewAuthor);

        review1 = Review.builder()
                .eventId(1L)
                .user(reviewAuthor)
                .content("Review to be reported")
                .rating(1)
                .isReported(false) // Explicitly set non-nullable
                .isVisible(true)  // Explicitly set non-nullable
                .images(new ArrayList<>()) // Initialize collections
                .responses(new ArrayList<>())
                .reports(new ArrayList<>())
                .build();
        reviewRepository.saveAndFlush(review1);

        report1 = ReviewReport.builder().review(review1).reporter(reporterUser).reason("Spam").status(ReviewReport.ReportStatus.PENDING).build();
        report2 = ReviewReport.builder().review(review1).reporter(reporterUser).reason("Hate speech").status(ReviewReport.ReportStatus.APPROVED).build();
        reviewReportRepository.saveAllAndFlush(List.of(report1, report2));
    }

    @Test
    void findByReviewId_shouldReturnReportsForReview() {
        List<ReviewReport> reports = reviewReportRepository.findByReviewId(review1.getId());
        assertThat(reports).hasSize(2);
        assertThat(reports).extracting(ReviewReport::getReason).containsExactlyInAnyOrder("Spam", "Hate speech");
    }

    @Test
    void findByStatus_shouldReturnReportsWithMatchingStatus() {
        List<ReviewReport> pendingReports = reviewReportRepository.findByStatus(ReviewReport.ReportStatus.PENDING);
        assertThat(pendingReports).hasSize(1);
        assertThat(pendingReports.get(0).getReason()).isEqualTo("Spam");

        List<ReviewReport> approvedReports = reviewReportRepository.findByStatus(ReviewReport.ReportStatus.APPROVED);
        assertThat(approvedReports).hasSize(1);
        assertThat(approvedReports.get(0).getReason()).isEqualTo("Hate speech");
    }

    @Test
    void existsByReviewIdAndReporterId_shouldReturnTrueIfReportExists() {
        boolean exists = reviewReportRepository.existsByReviewIdAndReporterId(review1.getId(), reporterUser.getId());
        assertThat(exists).isTrue();
    }

    @Test
    void existsByReviewIdAndReporterId_shouldReturnFalseIfReportDoesNotExist() {
        User anotherUser = User.builder().email("another@example.com").password("password").build();
        userRepository.save(anotherUser);
        boolean exists = reviewReportRepository.existsByReviewIdAndReporterId(review1.getId(), anotherUser.getId());
        assertThat(exists).isFalse();
    }
}

