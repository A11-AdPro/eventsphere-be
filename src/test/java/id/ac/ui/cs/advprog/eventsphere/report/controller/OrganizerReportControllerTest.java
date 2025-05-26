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
public class OrganizerReportControllerTest {

    @Mock
    private ReportService reportService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private OrganizerReportController organizerReportController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User mockUser;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(organizerReportController).build();

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("organizer@example.com");
        mockUser.setRole(Role.ORGANIZER);
    }

    @Test
    @DisplayName("Mendapatkan laporan berdasarkan status")
    public void testGetReportsByStatus() throws Exception {
        // Arrange
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

        when(reportService.getReportsByStatus(ReportStatus.PENDING)).thenReturn(summaryDTOs);

        // Act
        mockMvc.perform(get("/api/organizer/reports")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reportId1.toString()))
                .andExpect(jsonPath("$[1].id").value(reportId2.toString()));

        // Assert
        verify(reportService).getReportsByStatus(ReportStatus.PENDING);
    }

    @Test
    @DisplayName("Mendapatkan laporan berdasarkan ID")
    public void testGetReportById() throws Exception {
        // Arrange
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

        when(reportService.getReportById(reportId)).thenReturn(responseDTO);

        // Act
        mockMvc.perform(get("/api/organizer/reports/{id}", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.userEmail").value(userEmail));

        // Assert
        verify(reportService).getReportById(reportId);
    }

    @Test
    @DisplayName("Menambahkan komentar pada laporan")
    public void testAddComment() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(mockUser);

        UUID reportId = UUID.randomUUID();
        Long responderId = 1L;
        String responderEmail = "organizer@example.com";

        CreateReportCommentRequest commentRequest = new CreateReportCommentRequest();
        commentRequest.setMessage("Response from organizer");

        UUID commentId = UUID.randomUUID();
        ReportCommentDTO commentDTO = new ReportCommentDTO();
        commentDTO.setId(commentId);
        commentDTO.setReportId(reportId);
        commentDTO.setResponderId(responderId);
        commentDTO.setResponderEmail(responderEmail);
        commentDTO.setResponderRole("ORGANIZER");
        commentDTO.setMessage("Response from organizer");
        commentDTO.setCreatedAt(LocalDateTime.now());

        when(reportService.addComment(eq(reportId), any(CreateReportCommentRequest.class))).thenReturn(commentDTO);

        // Act
        mockMvc.perform(post("/api/organizer/reports/{reportId}/comments", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.reportId").value(reportId.toString()));

        // Assert
        verify(authService).getCurrentUser();
        verify(reportService).addComment(eq(reportId), any(CreateReportCommentRequest.class));
    }

    @Test
    @DisplayName("Memperbarui status laporan")
    public void testUpdateReportStatus() throws Exception {
        // Arrange
        UUID reportId = UUID.randomUUID();

        ReportResponseDTO updatedReportDTO = new ReportResponseDTO();
        updatedReportDTO.setId(reportId);
        updatedReportDTO.setStatus(ReportStatus.ON_PROGRESS);

        when(reportService.updateReportStatus(eq(reportId), eq(ReportStatus.ON_PROGRESS))).thenReturn(updatedReportDTO);

        // Act
        mockMvc.perform(patch("/api/organizer/reports/{id}/status", reportId)
                        .param("status", "ON_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.status").value("ON_PROGRESS"));

        // Assert
        verify(reportService).updateReportStatus(reportId, ReportStatus.ON_PROGRESS);
    }

    @Test
    @DisplayName("Mendapatkan laporan dengan status PENDING jika parameter status tidak diberikan")
    public void testGetReportsByStatus_DefaultPending() throws Exception {
        // Arrange
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

        // Mocking service
        when(reportService.getReportsByStatus(ReportStatus.PENDING)).thenReturn(summaryDTOs);

        // Act
        mockMvc.perform(get("/api/organizer/reports"))
                .andExpect(status().isOk())  // Memastikan status HTTP 200 (OK)
                .andExpect(jsonPath("$[0].id").value(reportId.toString()))  // Memastikan ID laporan sesuai
                .andExpect(jsonPath("$[0].userId").value(1))  // Memastikan ID pengguna sesuai
                .andExpect(jsonPath("$[0].userEmail").value("user@example.com"))  // Memastikan email pengguna sesuai
                .andExpect(jsonPath("$[0].status").value("PENDING"))  // Memastikan status laporan sesuai dengan PENDING
                .andExpect(jsonPath("$[0].category").value("EVENT"));  // Memastikan kategori laporan sesuai dengan EVENT

        // VerifyING sebagai default
        verify(reportService).getReportsByStatus(ReportStatus.PENDING);
    }

}