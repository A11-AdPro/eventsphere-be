package id.ac.ui.cs.advprog.eventsphere.report.dto.response;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class ReportResponseDTOTest {

    @Test
    @DisplayName("Memeriksa fungsi setter dan getter untuk ReportResponseDTO tanpa event")
    public void testReportResponseDTOSettersAndGetters_WithoutEvent() {
        // Arrange
        UUID id = UUID.randomUUID();
        Long userId = 1L;
        String userEmail = "user@example.com";
        LocalDateTime now = LocalDateTime.now();

        List<ReportCommentDTO> comments = new ArrayList<>();
        ReportCommentDTO comment = new ReportCommentDTO();
        comment.setId(UUID.randomUUID());
        comment.setMessage("Test comment");
        comments.add(comment);

        ReportResponseDTO dto = new ReportResponseDTO();

        // Act
        dto.setId(id);
        dto.setUserId(userId);
        dto.setUserEmail(userEmail);
        dto.setCategory(ReportCategory.PAYMENT);
        dto.setDescription("Test description");
        dto.setStatus(ReportStatus.PENDING);
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);
        dto.setComments(comments);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(userId, dto.getUserId());
        assertEquals(userEmail, dto.getUserEmail());
        assertNull(dto.getEventId());
        assertNull(dto.getEventTitle());
        assertEquals(ReportCategory.PAYMENT, dto.getCategory());
        assertEquals("Test description", dto.getDescription());
        assertEquals(ReportStatus.PENDING, dto.getStatus());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
        assertEquals(comments, dto.getComments());
        assertEquals(1, dto.getComments().size());
        assertEquals("Test comment", dto.getComments().getFirst().getMessage());
    }

    @Test
    @DisplayName("Memeriksa fungsi setter dan getter untuk ReportResponseDTO dengan event")
    public void testReportResponseDTOSettersAndGetters_WithEvent() {
        // Arrange
        UUID id = UUID.randomUUID();
        Long userId = 1L;
        String userEmail = "user@example.com";
        Long eventId = 10L;
        String eventTitle = "Annual Tech Conference 2024";
        LocalDateTime now = LocalDateTime.now();

        ReportResponseDTO dto = new ReportResponseDTO();

        // Act
        dto.setId(id);
        dto.setUserId(userId);
        dto.setUserEmail(userEmail);
        dto.setEventId(eventId);
        dto.setEventTitle(eventTitle);
        dto.setCategory(ReportCategory.EVENT);
        dto.setDescription("Event-related issue");
        dto.setStatus(ReportStatus.ON_PROGRESS);
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);

        // Assert
        assertEquals(id, dto.getId());
        assertEquals(userId, dto.getUserId());
        assertEquals(userEmail, dto.getUserEmail());
        assertEquals(eventId, dto.getEventId());
        assertEquals(eventTitle, dto.getEventTitle());
        assertEquals(ReportCategory.EVENT, dto.getCategory());
        assertEquals("Event-related issue", dto.getDescription());
        assertEquals(ReportStatus.ON_PROGRESS, dto.getStatus());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    @Test
    @DisplayName("Memeriksa ReportResponseDTO dengan multiple comments")
    public void testReportResponseDTO_MultipleComments() {
        // Arrange
        ReportResponseDTO dto = new ReportResponseDTO();

        List<ReportCommentDTO> comments = new ArrayList<>();

        ReportCommentDTO comment1 = new ReportCommentDTO();
        comment1.setId(UUID.randomUUID());
        comment1.setMessage("First comment");

        ReportCommentDTO comment2 = new ReportCommentDTO();
        comment2.setId(UUID.randomUUID());
        comment2.setMessage("Second comment");

        comments.add(comment1);
        comments.add(comment2);

        // Act
        dto.setComments(comments);

        // Assert
        assertEquals(2, dto.getComments().size());
        assertEquals("First comment", dto.getComments().get(0).getMessage());
        assertEquals("Second comment", dto.getComments().get(1).getMessage());
    }

    @Test
    @DisplayName("Memeriksa ReportResponseDTO dengan semua kategori dan status")
    public void testReportResponseDTO_AllCategoriesAndStatuses() {
        // Test all ReportCategory values
        ReportResponseDTO dto = new ReportResponseDTO();

        // Test all categories
        dto.setCategory(ReportCategory.PAYMENT);
        assertEquals(ReportCategory.PAYMENT, dto.getCategory());

        dto.setCategory(ReportCategory.TICKET);
        assertEquals(ReportCategory.TICKET, dto.getCategory());

        dto.setCategory(ReportCategory.EVENT);
        assertEquals(ReportCategory.EVENT, dto.getCategory());

        dto.setCategory(ReportCategory.OTHER);
        assertEquals(ReportCategory.OTHER, dto.getCategory());

        // Test all statuses
        dto.setStatus(ReportStatus.PENDING);
        assertEquals(ReportStatus.PENDING, dto.getStatus());

        dto.setStatus(ReportStatus.ON_PROGRESS);
        assertEquals(ReportStatus.ON_PROGRESS, dto.getStatus());

        dto.setStatus(ReportStatus.RESOLVED);
        assertEquals(ReportStatus.RESOLVED, dto.getStatus());
    }

    @Test
    @DisplayName("Memeriksa ReportResponseDTO dengan nilai null")
    public void testReportResponseDTO_NullValues() {
        // Arrange
        ReportResponseDTO dto = new ReportResponseDTO();

        // Act
        dto.setId(null);
        dto.setUserId(null);
        dto.setUserEmail(null);
        dto.setEventId(null);
        dto.setEventTitle(null);
        dto.setCategory(null);
        dto.setDescription(null);
        dto.setStatus(null);
        dto.setCreatedAt(null);
        dto.setUpdatedAt(null);
        dto.setComments(null);

        // Assert
        assertNull(dto.getId());
        assertNull(dto.getUserId());
        assertNull(dto.getUserEmail());
        assertNull(dto.getEventId());
        assertNull(dto.getEventTitle());
        assertNull(dto.getCategory());
        assertNull(dto.getDescription());
        assertNull(dto.getStatus());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
        assertNull(dto.getComments());
    }
}