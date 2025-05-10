package id.ac.ui.cs.advprog.eventsphere.report.controller;

import id.ac.ui.cs.advprog.eventsphere.authentication.model.Role;
import id.ac.ui.cs.advprog.eventsphere.authentication.model.User;
import id.ac.ui.cs.advprog.eventsphere.authentication.service.AuthService;
import id.ac.ui.cs.advprog.eventsphere.report.dto.request.CreateReportCommentRequest;
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
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(OrganizerReportController.class)
@Import(OrganizerReportControllerTest.TestConfig.class)
public class OrganizerReportControllerTest {

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
            mockUser.setEmail("organizer@example.com");
            mockUser.setRole(Role.ORGANIZER);
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
    @WithMockUser(roles = "ORGANIZER")
    public void testGetReportsByStatus() throws Exception {
        // Create test data
        UUID reportId1 = UUID.randomUUID();
        UUID reportId2 = UUID.randomUUID();

        List<ReportSummaryDTO> summaryDTOs = new ArrayList<>();

        ReportSummaryDTO dto1 = new ReportSummaryDTO();
        dto1.setId(reportId1);
        dto1.setUserId(1L);
        dto1.setUserEmail("user1@example.com");
        dto1.setCategory(ReportCategory.EVENT);
        dto1.setStatus(ReportStatus.PENDING);
        dto1.setShortDescription("Event issue...");
        dto1.setCreatedAt(LocalDateTime.now());
        dto1.setCommentCount(0);

        ReportSummaryDTO dto2 = new ReportSummaryDTO();
        dto2.setId(reportId2);
        dto2.setUserId(2L);
        dto2.setUserEmail("user2@example.com");
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
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].userEmail").value("user1@example.com"))
                .andExpect(jsonPath("$[0].category").value("EVENT"))
                .andExpect(jsonPath("$[1].id").value(reportId2.toString()))
                .andExpect(jsonPath("$[1].userId").value(2))
                .andExpect(jsonPath("$[1].userEmail").value("user2@example.com"))
                .andExpect(jsonPath("$[1].category").value("TICKET"));
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    public void testGetReportById() throws Exception {
        // Create test data
        UUID reportId = UUID.randomUUID();
        Long userId = 1L;
        String userEmail = "user@example.com";

        ReportResponseDTO responseDTO = new ReportResponseDTO();
        responseDTO.setId(reportId);
        responseDTO.setUserId(userId);
        responseDTO.setUserEmail(userEmail);
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
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.userEmail").value(userEmail))
                .andExpect(jsonPath("$.category").value("EVENT"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    public void testAddComment() throws Exception {
        // Create test data
        UUID reportId = UUID.randomUUID();
        Long responderId = 1L;
        String responderEmail = "organizer@example.com";

        // Create simple comment request - controller will set fields
        CreateReportCommentRequest commentRequest = new CreateReportCommentRequest();
        commentRequest.setMessage("Response from organizer");

        // Create expected response
        UUID commentId = UUID.randomUUID();
        ReportCommentDTO commentDTO = new ReportCommentDTO();
        commentDTO.setId(commentId);
        commentDTO.setReportId(reportId);
        commentDTO.setResponderId(responderId);
        commentDTO.setResponderEmail(responderEmail);
        commentDTO.setResponderRole("ORGANIZER");
        commentDTO.setMessage("Response from organizer");
        commentDTO.setCreatedAt(LocalDateTime.now());

        // Mock service
        when(reportService.addComment(eq(reportId), any(CreateReportCommentRequest.class))).thenReturn(commentDTO);

        // Perform request with CSRF token
        mockMvc.perform(post("/api/organizer/reports/{reportId}/comments", reportId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.reportId").value(reportId.toString()))
                .andExpect(jsonPath("$.responderId").value(responderId))
                .andExpect(jsonPath("$.responderEmail").value(responderEmail))
                .andExpect(jsonPath("$.responderRole").value("ORGANIZER"));
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    public void testUpdateReportStatus() throws Exception {
        // Create test data
        UUID reportId = UUID.randomUUID();

        ReportResponseDTO updatedReportDTO = new ReportResponseDTO();
        updatedReportDTO.setId(reportId);
        updatedReportDTO.setStatus(ReportStatus.ON_PROGRESS);

        // Mock service
        when(reportService.updateReportStatus(eq(reportId), eq(ReportStatus.ON_PROGRESS))).thenReturn(updatedReportDTO);

        // Perform request with CSRF token
        mockMvc.perform(patch("/api/organizer/reports/{id}/status", reportId)
                        .with(csrf())
                        .param("status", "ON_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.status").value("ON_PROGRESS"));
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    public void testGetReportsByStatus_DefaultPending() throws Exception {
        // Create test data
        UUID reportId = UUID.randomUUID();

        ReportSummaryDTO dto = new ReportSummaryDTO();
        dto.setId(reportId);
        dto.setUserId(1L);
        dto.setUserEmail("user@example.com");
        dto.setCategory(ReportCategory.EVENT);
        dto.setStatus(ReportStatus.PENDING);
        dto.setShortDescription("No status param issue");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setCommentCount(2);

        List<ReportSummaryDTO> summaryDTOs = List.of(dto);

        // Mock service
        when(reportService.getReportsByStatus(ReportStatus.PENDING)).thenReturn(summaryDTOs);

        // Perform request without 'status' param
        mockMvc.perform(get("/api/organizer/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reportId.toString()))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].userEmail").value("user@example.com"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].category").value("EVENT"));
    }
}