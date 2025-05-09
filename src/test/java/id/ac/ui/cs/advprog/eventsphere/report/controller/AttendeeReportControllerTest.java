package id.ac.ui.cs.advprog.eventsphere.report.controller;

import id.ac.ui.cs.advprog.eventsphere.report.dto.request.CreateReportCommentRequest;
import id.ac.ui.cs.advprog.eventsphere.report.dto.request.CreateReportRequest;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportCommentDTO;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportResponseDTO;
import id.ac.ui.cs.advprog.eventsphere.report.dto.response.ReportSummaryDTO;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttendeeReportController.class)
@Import(AttendeeReportControllerTest.TestConfig.class)
public class AttendeeReportControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ReportService reportService() {
            return Mockito.mock(ReportService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReportService reportService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateReport() throws Exception {
        // Create test data
        UUID userId = UUID.randomUUID();
        CreateReportRequest createRequest = new CreateReportRequest();
        createRequest.setUserId(userId);
        createRequest.setCategory(ReportCategory.PAYMENT);
        createRequest.setDescription("Test description");

        // Create response DTO
        UUID reportId = UUID.randomUUID();
        ReportResponseDTO responseDTO = new ReportResponseDTO();
        responseDTO.setId(reportId);
        responseDTO.setUserId(userId);
        responseDTO.setCategory(ReportCategory.PAYMENT);
        responseDTO.setDescription("Test description");
        responseDTO.setStatus(ReportStatus.PENDING);

        // Mock service
        when(reportService.createReport(any(CreateReportRequest.class), any())).thenReturn(responseDTO);

        // Create mock multipart files
        MockMultipartFile jsonFile = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(createRequest)
        );

        MockMultipartFile file1 = new MockMultipartFile(
                "attachments",
                "test1.jpg",
                "image/jpeg",
                "test1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "attachments",
                "test2.jpg",
                "image/jpeg",
                "test2".getBytes()
        );

        // Perform request
        mockMvc.perform(multipart("/api/attendee/reports")
                        .file(jsonFile)
                        .file(file1)
                        .file(file2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.category").value("PAYMENT"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    public void testGetReportsByUserId() throws Exception {
        // Create test data
        UUID userId = UUID.randomUUID();
        UUID reportId1 = UUID.randomUUID();
        UUID reportId2 = UUID.randomUUID();

        List<ReportSummaryDTO> summaryDTOs = new ArrayList<>();

        ReportSummaryDTO dto1 = new ReportSummaryDTO();
        dto1.setId(reportId1);
        dto1.setCategory(ReportCategory.PAYMENT);
        dto1.setStatus(ReportStatus.PENDING);
        dto1.setShortDescription("Payment issue...");
        dto1.setCreatedAt(LocalDateTime.now());
        dto1.setCommentCount(0);

        ReportSummaryDTO dto2 = new ReportSummaryDTO();
        dto2.setId(reportId2);
        dto2.setCategory(ReportCategory.TICKET);
        dto2.setStatus(ReportStatus.RESOLVED);
        dto2.setShortDescription("Ticket issue...");
        dto2.setCreatedAt(LocalDateTime.now());
        dto2.setCommentCount(2);

        summaryDTOs.add(dto1);
        summaryDTOs.add(dto2);

        // Mock service
        when(reportService.getReportsByUserId(userId)).thenReturn(summaryDTOs);

        // Perform request
        mockMvc.perform(get("/api/attendee/reports")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reportId1.toString()))
                .andExpect(jsonPath("$[0].category").value("PAYMENT"))
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
        responseDTO.setCategory(ReportCategory.PAYMENT);
        responseDTO.setDescription("Test description");
        responseDTO.setStatus(ReportStatus.PENDING);
        responseDTO.setCreatedAt(LocalDateTime.now());

        // Mock service
        when(reportService.getReportById(reportId)).thenReturn(responseDTO);

        // Perform request
        mockMvc.perform(get("/api/attendee/reports/{id}", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.category").value("PAYMENT"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    public void testAddComment() throws Exception {
        // Create test data
        UUID reportId = UUID.randomUUID();
        UUID responderId = UUID.randomUUID();

        CreateReportCommentRequest commentRequest = new CreateReportCommentRequest();
        commentRequest.setResponderId(responderId);
        commentRequest.setResponderRole("ATTENDEE");
        commentRequest.setMessage("Test comment from attendee");

        UUID commentId = UUID.randomUUID();
        ReportCommentDTO commentDTO = new ReportCommentDTO();
        commentDTO.setId(commentId);
        commentDTO.setReportId(reportId);
        commentDTO.setResponderId(responderId);
        commentDTO.setResponderRole("ATTENDEE");
        commentDTO.setMessage("Test comment from attendee");
        commentDTO.setCreatedAt(LocalDateTime.now());

        // Mock service
        when(reportService.addComment(eq(reportId), any(CreateReportCommentRequest.class))).thenReturn(commentDTO);

        // Perform request
        mockMvc.perform(post("/api/attendee/reports/{reportId}/comments", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.reportId").value(reportId.toString()))
                .andExpect(jsonPath("$.responderRole").value("ATTENDEE"));
    }
}