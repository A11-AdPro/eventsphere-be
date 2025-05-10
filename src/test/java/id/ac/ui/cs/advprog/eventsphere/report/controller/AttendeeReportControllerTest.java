package id.ac.ui.cs.advprog.eventsphere.report.controller;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.AuthService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttendeeReportController.class)
@Import(AttendeeReportControllerTest.TestConfig.class)
@ActiveProfiles("test")
public class AttendeeReportControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ReportService reportService() {
            return Mockito.mock(ReportService.class);
        }

        @Bean
        public AuthService authService() {
            AuthService mockAuthService = Mockito.mock(AuthService.class);
            User mockUser = new User();
            mockUser.setId(1L);
            mockUser.setEmail("attendee@example.com");
            mockUser.setRole(Role.ATTENDEE);
            when(mockAuthService.getCurrentUser()).thenReturn(mockUser);
            return mockAuthService;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReportService reportService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ATTENDEE")
    public void testCreateReport() throws Exception {
        // Create test data
        Long userId = 1L;
        String userEmail = "attendee@example.com";

        CreateReportRequest createRequest = new CreateReportRequest();
        createRequest.setUserId(userId);
        createRequest.setUserEmail(userEmail);
        createRequest.setCategory(ReportCategory.PAYMENT);
        createRequest.setDescription("Test description");

        // Create response DTO
        UUID reportId = UUID.randomUUID();
        ReportResponseDTO responseDTO = new ReportResponseDTO();
        responseDTO.setId(reportId);
        responseDTO.setUserId(userId);
        responseDTO.setUserEmail(userEmail);
        responseDTO.setCategory(ReportCategory.PAYMENT);
        responseDTO.setDescription("Test description");
        responseDTO.setStatus(ReportStatus.PENDING);

        // Mock service
        when(reportService.createReport(any(CreateReportRequest.class))).thenReturn(responseDTO);

        // Perform request
        mockMvc.perform(post("/api/attendee/reports")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.userEmail").value(userEmail))
                .andExpect(jsonPath("$.category").value("PAYMENT"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ATTENDEE")
    public void testGetReportsByUserId() throws Exception {
        // Create test data
        Long userId = 1L;
        String userEmail = "attendee@example.com";
        UUID reportId1 = UUID.randomUUID();
        UUID reportId2 = UUID.randomUUID();

        List<ReportSummaryDTO> summaryDTOs = new ArrayList<>();

        ReportSummaryDTO dto1 = new ReportSummaryDTO();
        dto1.setId(reportId1);
        dto1.setUserId(userId);
        dto1.setUserEmail(userEmail);
        dto1.setCategory(ReportCategory.PAYMENT);
        dto1.setStatus(ReportStatus.PENDING);
        dto1.setShortDescription("Payment issue...");
        dto1.setCreatedAt(LocalDateTime.now());
        dto1.setCommentCount(0);

        ReportSummaryDTO dto2 = new ReportSummaryDTO();
        dto2.setId(reportId2);
        dto2.setUserId(userId);
        dto2.setUserEmail(userEmail);
        dto2.setCategory(ReportCategory.TICKET);
        dto2.setStatus(ReportStatus.RESOLVED);
        dto2.setShortDescription("Ticket issue...");
        dto2.setCreatedAt(LocalDateTime.now());
        dto2.setCommentCount(2);

        summaryDTOs.add(dto1);
        summaryDTOs.add(dto2);

        // Mock service
        when(reportService.getReportsByUserEmail(userEmail)).thenReturn(summaryDTOs);

        // Perform request
        mockMvc.perform(get("/api/attendee/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reportId1.toString()))
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[0].userEmail").value(userEmail))
                .andExpect(jsonPath("$[0].category").value("PAYMENT"))
                .andExpect(jsonPath("$[1].id").value(reportId2.toString()))
                .andExpect(jsonPath("$[1].userId").value(userId))
                .andExpect(jsonPath("$[1].userEmail").value(userEmail))
                .andExpect(jsonPath("$[1].category").value("TICKET"));
    }

    @Test
    @WithMockUser(roles = "ATTENDEE")
    public void testGetReportById() throws Exception {
        // Create test data
        UUID reportId = UUID.randomUUID();
        Long userId = 1L;
        String userEmail = "attendee@example.com";

        ReportResponseDTO responseDTO = new ReportResponseDTO();
        responseDTO.setId(reportId);
        responseDTO.setUserId(userId);
        responseDTO.setUserEmail(userEmail);
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
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.userEmail").value(userEmail))
                .andExpect(jsonPath("$.category").value("PAYMENT"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ATTENDEE")
    public void testAddComment() throws Exception {
        // Create test data
        UUID reportId = UUID.randomUUID();
        Long userId = 1L;
        String userEmail = "attendee@example.com";

        // Create mock report - needed for authorization check
        ReportResponseDTO responseDTO = new ReportResponseDTO();
        responseDTO.setId(reportId);
        responseDTO.setUserId(userId);
        responseDTO.setUserEmail(userEmail);

        when(reportService.getReportById(reportId)).thenReturn(responseDTO);

        CreateReportCommentRequest commentRequest = new CreateReportCommentRequest();
        commentRequest.setMessage("Test comment from attendee");

        // Create expected response
        UUID commentId = UUID.randomUUID();
        ReportCommentDTO commentDTO = new ReportCommentDTO();
        commentDTO.setId(commentId);
        commentDTO.setReportId(reportId);
        commentDTO.setResponderId(userId);
        commentDTO.setResponderEmail(userEmail);
        commentDTO.setResponderRole("ATTENDEE");
        commentDTO.setMessage("Test comment from attendee");
        commentDTO.setCreatedAt(LocalDateTime.now());

        when(reportService.addComment(eq(reportId), any(CreateReportCommentRequest.class))).thenReturn(commentDTO);

        mockMvc.perform(post("/api/attendee/reports/{reportId}/comments", reportId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.reportId").value(reportId.toString()))
                .andExpect(jsonPath("$.responderId").value(userId))
                .andExpect(jsonPath("$.responderEmail").value(userEmail))
                .andExpect(jsonPath("$.responderRole").value("ATTENDEE"));
    }

    @Test
    @WithMockUser(roles = "ATTENDEE")
    public void testGetReportById_Forbidden() throws Exception {
        // Create test data
        UUID reportId = UUID.randomUUID();
        Long userId = 2L;
        String userEmail = "another.attendee@example.com";

        // Create report that belongs to a different user
        ReportResponseDTO responseDTO = new ReportResponseDTO();
        responseDTO.setId(reportId);
        responseDTO.setUserId(userId);
        responseDTO.setUserEmail(userEmail);
        responseDTO.setCategory(ReportCategory.PAYMENT);
        responseDTO.setDescription("Test description");
        responseDTO.setStatus(ReportStatus.PENDING);

        // Mock service
        when(reportService.getReportById(reportId)).thenReturn(responseDTO);

        // Perform request
        mockMvc.perform(get("/api/attendee/reports/{id}", reportId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ATTENDEE")
    public void testAddComment_Forbidden() throws Exception {
        // Create test data
        UUID reportId = UUID.randomUUID();
        Long userId = 2L;
        String userEmail = "another.attendee@example.com";

        // Create report that belongs to a different user
        ReportResponseDTO responseDTO = new ReportResponseDTO();
        responseDTO.setId(reportId);
        responseDTO.setUserId(userId);
        responseDTO.setUserEmail(userEmail);
        responseDTO.setCategory(ReportCategory.PAYMENT);
        responseDTO.setDescription("Test description");
        responseDTO.setStatus(ReportStatus.PENDING);

        // Mock service
        when(reportService.getReportById(reportId)).thenReturn(responseDTO);

        // Create comment request
        CreateReportCommentRequest commentRequest = new CreateReportCommentRequest();
        commentRequest.setMessage("Test comment from attendee");

        // Perform request
        mockMvc.perform(post("/api/attendee/reports/{reportId}/comments", reportId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isForbidden());
    }
}