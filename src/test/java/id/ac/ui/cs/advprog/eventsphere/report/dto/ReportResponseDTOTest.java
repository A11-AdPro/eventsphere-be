package id.ac.ui.cs.advprog.eventsphere.report.dto.response;

import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class ReportResponseDTOTest {

    @Test
    public void testReportResponseDTOSettersAndGetters() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        List<String> attachments = new ArrayList<>();
        attachments.add("file1.jpg");

        List<ReportCommentDTO> comments = new ArrayList<>();
        ReportCommentDTO comment = new ReportCommentDTO();
        comment.setId(UUID.randomUUID());
        comment.setMessage("Test comment");
        comments.add(comment);

        ReportResponseDTO dto = new ReportResponseDTO();
        dto.setId(id);
        dto.setUserId(userId);
        dto.setCategory(ReportCategory.PAYMENT);
        dto.setDescription("Test description");
        dto.setStatus(ReportStatus.PENDING);
        dto.setAttachments(attachments);
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now);
        dto.setComments(comments);

        assertEquals(id, dto.getId());
        assertEquals(userId, dto.getUserId());
        assertEquals(ReportCategory.PAYMENT, dto.getCategory());
        assertEquals("Test description", dto.getDescription());
        assertEquals(ReportStatus.PENDING, dto.getStatus());
        assertEquals(attachments, dto.getAttachments());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
        assertEquals(comments, dto.getComments());
        assertEquals(1, dto.getComments().size());
        assertEquals("Test comment", dto.getComments().get(0).getMessage());
    }
}