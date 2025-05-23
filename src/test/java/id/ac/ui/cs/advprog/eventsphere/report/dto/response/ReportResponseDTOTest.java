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
    @DisplayName("Memeriksa fungsi setter dan getter untuk ReportResponseDTO")
    public void testReportResponseDTOSettersAndGetters() {
        // Arrange
        UUID id = UUID.randomUUID();
        Long userId = 1L;
        String userEmail = "user@example.com";
        LocalDateTime now = LocalDateTime.now();
        List<String> attachments = new ArrayList<>();
        attachments.add("file1.jpg");

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
        assertEquals(ReportCategory.PAYMENT, dto.getCategory());
        assertEquals("Test description", dto.getDescription());
        assertEquals(ReportStatus.PENDING, dto.getStatus());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
        assertEquals(comments, dto.getComments());
        assertEquals(1, dto.getComments().size());
        assertEquals("Test comment", dto.getComments().getFirst().getMessage());
    }
}