package id.ac.ui.cs.advprog.eventsphere.report.controller;

import id.ac.ui.cs.advprog.eventsphere.report.dto.request.CreateReportCommentRequest;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportCommentDTO;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportSummaryDTO;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrganizerReportController.class)
public class OrganizerReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetReportsByStatus() throws Exception {
        // Create test data
        UUID reportId1 = UUID.randomUUID();
        UUID reportId2 = UUID.randomUUID();

        List<ReportSummaryDTO> summaryDTOs = new ArrayList<>();

        ReportSummaryDTO dto1 = new ReportSummaryDTO();
        dto1.setId(reportId1);
        dto1.setCategory(ReportCategory.EVENT);
        dto1.setStatus(ReportStatus.PENDING);
        dto1.setShortDescription("Event issue...");
        dto1.setCreatedAt(LocalDateTime.now());
        dto1.setCommentCount(0);

        ReportSummaryDTO dto2 = new ReportSummaryDTO();
        dto2.setId(reportId2);
        dto2.setCategory(ReportCategory.TICKET);
        dto2.setStatus(ReportStatus.PENDING);
        dto2.setShortDescription("Ticket issue...");
        dto2.setCreatedAt(LocalDateTime.now());
        dto2.setCommentCount(1);

        summaryDTOs.add(dto1);
        summaryDTOs.add(dto2);

        // Mock service
        when(reportService.getReportsByStatus(ReportStatus.PENDING)).thenReturn(summaryDTOs);

        // Perform request
        mockMvc.perform(get("/api/organizer/reports")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reportId1.toString()))
                .andExpect(jsonPath("$[0].category").value("EVENT"))
                .andExpect(jsonPath("$[1].id").value(reportId2.toString()))
                .andExpect(jsonPath("$[1].category").value("TICKET"));
    }

    @Test
    public void testGetReportById() throws Exception {
        // Create test data
        UUID reportId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ReportResponseDTO responseDTO = new ReportResponseDTO();
        responseDTO.setId(reportId);
        responseDTO.setUserId(userId);
        responseDTO.setCategory(ReportCategory.EVENT);
        responseDTO.setDescription("Event issue description");
        responseDTO.setStatus(ReportStatus.PENDING);
        responseDTO.setCreatedAt(LocalDateTime.now());

        // Mock service
        when(reportService.getReportById(reportId)).thenReturn(responseDTO);

        // Perform request
        mockMvc.perform(get("/api/organizer/reports/{id}", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.category").value("EVENT"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    public void testAddComment() throws Exception {
        // Create test data
        UUID reportId = UUID.randomUUID();
        UUID responderId = UUID.randomUUID();

        CreateReportCommentRequest commentRequest = new CreateReportCommentRequest();
        commentRequest.setResponderId(responderId);
        commentRequest.setResponderRole("ORGANIZER");
        commentRequest.setMessage("Response from organizer");

        UUID commentId = UUID.randomUUID();
        ReportCommentDTO commentDTO = new ReportCommentDTO();
        commentDTO.setId(commentId);
        commentDTO.setReportId(reportId);
        commentDTO.setResponderId(responderId);
        commentDTO.setResponderRole("ORGANIZER");
        commentDTO.setMessage("Response from organizer");
        commentDTO.setCreatedAt(LocalDateTime.now());

        // Mock service
        when(reportService.addComment(eq(reportId), any(CreateReportCommentRequest.class))).thenReturn(commentDTO);

        // Perform request
        mockMvc.perform(post("/api/organizer/reports/{reportId}/comments", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.reportId").value(reportId.toString()))
                .andExpect(jsonPath("$.responderRole").value("ORGANIZER"));
    }

    @Test
    public void testUpdateReportStatus() throws Exception {
        // Create test data
        UUID reportId = UUID.randomUUID();

        ReportResponseDTO updatedReportDTO = new ReportResponseDTO();
        updatedReportDTO.setId(reportId);
        updatedReportDTO.setStatus(ReportStatus.ON_PROGRESS);

        // Mock service
        when(reportService.updateReportStatus(eq(reportId), eq(ReportStatus.ON_PROGRESS))).thenReturn(updatedReportDTO);

        // Perform request
        mockMvc.perform(patch("/api/organizer/reports/{id}/status", reportId)
                        .param("status", "ON_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.status").value("ON_PROGRESS"));
    }
}