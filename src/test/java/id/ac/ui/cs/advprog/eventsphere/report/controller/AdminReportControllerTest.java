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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AdminReportControllerTest {

    @Mock
    private ReportService reportService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AdminReportController adminReportController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(adminReportController).build();
    }

    @Test
    @DisplayName("Mengambil semua laporan")
    public void testGetAllReports() throws Exception {
        // Arrange
        UUID reportId1 = UUID.randomUUID();
        UUID reportId2 = UUID.randomUUID();
        List<ReportSummaryDTO> summaryDTOs = new ArrayList<>();

        ReportSummaryDTO dto1 = new ReportSummaryDTO();
        dto1.setId(reportId1);
        dto1.setUserId(1L);
        dto1.setUserEmail("user1@example.com");
        dto1.setCategory(ReportCategory.PAYMENT);
        dto1.setStatus(ReportStatus.PENDING);
        dto1.setShortDescription("Payment issue...");
        dto1.setCreatedAt(LocalDateTime.now());
        dto1.setCommentCount(0);

        ReportSummaryDTO dto2 = new ReportSummaryDTO();
        dto2.setId(reportId2);
        dto2.setUserId(2L);
        dto2.setUserEmail("user2@example.com");
        dto2.setCategory(ReportCategory.TICKET);
        dto2.setStatus(ReportStatus.ON_PROGRESS);
        dto2.setShortDescription("Ticket issue...");
        dto2.setCreatedAt(LocalDateTime.now());
        dto2.setCommentCount(2);

        summaryDTOs.add(dto1);
        summaryDTOs.add(dto2);

        // Mock service
        when(reportService.getReportsByStatus(null)).thenReturn(summaryDTOs);

        // Act
        mockMvc.perform(get("/api/admin/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reportId1.toString()))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].userEmail").value("user1@example.com"))
                .andExpect(jsonPath("$[0].category").value("PAYMENT"))
                .andExpect(jsonPath("$[1].id").value(reportId2.toString()))
                .andExpect(jsonPath("$[1].userId").value(2))
                .andExpect(jsonPath("$[1].userEmail").value("user2@example.com"))
                .andExpect(jsonPath("$[1].category").value("TICKET"));
    }

    @Test
    @DisplayName("Mengambil laporan berdasarkan status")
    public void testGetReportsByStatus() throws Exception {
        // Arrange
        UUID reportId1 = UUID.randomUUID();
        UUID reportId2 = UUID.randomUUID();

        List<ReportSummaryDTO> summaryDTOs = new ArrayList<>();

        ReportSummaryDTO dto1 = new ReportSummaryDTO();
        dto1.setId(reportId1);
        dto1.setUserId(1L);
        dto1.setUserEmail("user1@example.com");
        dto1.setCategory(ReportCategory.PAYMENT);
        dto1.setStatus(ReportStatus.RESOLVED);
        dto1.setShortDescription("Payment issue...");
        dto1.setCreatedAt(LocalDateTime.now());
        dto1.setCommentCount(3);

        ReportSummaryDTO dto2 = new ReportSummaryDTO();
        dto2.setId(reportId2);
        dto2.setUserId(2L);
        dto2.setUserEmail("user2@example.com");
        dto2.setCategory(ReportCategory.EVENT);
        dto2.setStatus(ReportStatus.RESOLVED);
        dto2.setShortDescription("Event issue...");
        dto2.setCreatedAt(LocalDateTime.now());
        dto2.setCommentCount(1);

        summaryDTOs.add(dto1);
        summaryDTOs.add(dto2);

        // Mock service
        when(reportService.getReportsByStatus(ReportStatus.RESOLVED)).thenReturn(summaryDTOs);

        // Act
        mockMvc.perform(get("/api/admin/reports")
                        .param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reportId1.toString()))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].userEmail").value("user1@example.com"))
                .andExpect(jsonPath("$[0].status").value("RESOLVED"))
                .andExpect(jsonPath("$[1].id").value(reportId2.toString()))
                .andExpect(jsonPath("$[1].userId").value(2))
                .andExpect(jsonPath("$[1].userEmail").value("user2@example.com"))
                .andExpect(jsonPath("$[1].status").value("RESOLVED"));
    }

    @Test
    @DisplayName("Mengambil laporan berdasarkan ID")
    public void testGetReportById() throws Exception {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Long userId = 1L;
        String userEmail = "user@example.com";

        ReportResponseDTO responseDTO = new ReportResponseDTO();
        responseDTO.setId(reportId);
        responseDTO.setUserId(userId);
        responseDTO.setUserEmail(userEmail);
        responseDTO.setCategory(ReportCategory.PAYMENT);
        responseDTO.setDescription("Payment issue description");
        responseDTO.setStatus(ReportStatus.PENDING);
        responseDTO.setCreatedAt(LocalDateTime.now());

        // Mock service
        when(reportService.getReportById(reportId)).thenReturn(responseDTO);

        // Act
        mockMvc.perform(get("/api/admin/reports/{id}", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.userEmail").value(userEmail))
                .andExpect(jsonPath("$.category").value("PAYMENT"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("Menambahkan komentar pada laporan")
    public void testAddComment() throws Exception {
        // Arrange
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("admin@example.com");
        mockUser.setRole(Role.ADMIN);
        when(authService.getCurrentUser()).thenReturn(mockUser);

        UUID reportId = UUID.randomUUID();
        Long responderId = 1L;
        String responderEmail = "admin@example.com";

        CreateReportCommentRequest commentRequest = new CreateReportCommentRequest();
        commentRequest.setMessage("Response from admin");

        UUID commentId = UUID.randomUUID();
        ReportCommentDTO commentDTO = new ReportCommentDTO();
        commentDTO.setId(commentId);
        commentDTO.setReportId(reportId);
        commentDTO.setResponderId(responderId);
        commentDTO.setResponderEmail(responderEmail);
        commentDTO.setResponderRole("ADMIN");
        commentDTO.setMessage("Response from admin");
        commentDTO.setCreatedAt(LocalDateTime.now());

        // Mock service
        when(reportService.addComment(eq(reportId), any(CreateReportCommentRequest.class))).thenReturn(commentDTO);

        // Act
        mockMvc.perform(post("/api/admin/reports/{reportId}/comments", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.reportId").value(reportId.toString()))
                .andExpect(jsonPath("$.responderId").value(responderId))
                .andExpect(jsonPath("$.responderEmail").value(responderEmail))
                .andExpect(jsonPath("$.responderRole").value("ADMIN"));

        // Verify that authService.getCurrentUser() was called
        verify(authService).getCurrentUser();

        // Verify commentRequest was modified with the correct user information
        verify(reportService).addComment(eq(reportId), any(CreateReportCommentRequest.class));
    }

    @Test
    @DisplayName("Memperbarui status laporan")
    public void testUpdateReportStatus() throws Exception {
        // Arrange
        UUID reportId = UUID.randomUUID();

        ReportResponseDTO updatedReportDTO = new ReportResponseDTO();
        updatedReportDTO.setId(reportId);
        updatedReportDTO.setStatus(ReportStatus.RESOLVED);

        // Mock service
        when(reportService.updateReportStatus(eq(reportId), eq(ReportStatus.RESOLVED))).thenReturn(updatedReportDTO);

        // Act
        mockMvc.perform(patch("/api/admin/reports/{id}/status", reportId)
                        .param("status", "RESOLVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        // Verify the updateReportStatus was called with status change
        verify(reportService).updateReportStatus(eq(reportId), eq(ReportStatus.RESOLVED));
    }

    @Test
    @DisplayName("Menghapus laporan")
    public void testDeleteReport() throws Exception {
        // Arrange
        UUID reportId = UUID.randomUUID();

        // Mock service
        doNothing().when(reportService).deleteReport(reportId);

        // Act
        mockMvc.perform(delete("/api/admin/reports/{id}", reportId))
                .andExpect(status().isNoContent());

        // Verify that reportService.deleteReport() was called with the correct ID
        verify(reportService).deleteReport(reportId);
    }
}
