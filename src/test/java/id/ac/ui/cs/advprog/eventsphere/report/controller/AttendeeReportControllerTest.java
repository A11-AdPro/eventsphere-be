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
public class AttendeeReportControllerTest {

    @Mock
    private ReportService reportService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AttendeeReportController attendeeReportController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User mockUser;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(attendeeReportController).build();

        // Membuat mock user di sini, akan digunakan sesuai kebutuhan masing-masing test
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("attendee@example.com");
        mockUser.setRole(Role.ATTENDEE);
    }

    @Test
    @DisplayName("Membuat laporan baru oleh pengguna ATTENDEE")
    public void testCreateReport() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(mockUser);

        CreateReportRequest createRequest = new CreateReportRequest();
        createRequest.setCategory(ReportCategory.PAYMENT);
        createRequest.setDescription("Test description");

        UUID reportId = UUID.randomUUID();
        ReportResponseDTO responseDTO = new ReportResponseDTO();
        responseDTO.setId(reportId);
        responseDTO.setUserId(1L);
        responseDTO.setUserEmail("attendee@example.com");
        responseDTO.setCategory(ReportCategory.PAYMENT);
        responseDTO.setDescription("Test description");
        responseDTO.setStatus(ReportStatus.PENDING);

        // Mock service
        when(reportService.createReport(any(CreateReportRequest.class))).thenReturn(responseDTO);

        // Act
        mockMvc.perform(post("/api/attendee/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.userEmail").value("attendee@example.com"))
                .andExpect(jsonPath("$.category").value("PAYMENT"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Verify
        verify(authService).getCurrentUser();
        verify(reportService).createReport(any(CreateReportRequest.class));
    }

    @Test
    @DisplayName("Mendapatkan laporan yang dimiliki oleh pengguna ATTENDEE")
    public void testGetMyReports() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(mockUser);

        UUID reportId1 = UUID.randomUUID();
        UUID reportId2 = UUID.randomUUID();

        List<ReportSummaryDTO> summaryDTOs = new ArrayList<>();

        ReportSummaryDTO dto1 = new ReportSummaryDTO();
        dto1.setId(reportId1);
        dto1.setUserId(1L);
        dto1.setUserEmail("attendee@example.com");
        dto1.setCategory(ReportCategory.PAYMENT);
        dto1.setStatus(ReportStatus.PENDING);
        dto1.setShortDescription("Payment issue...");
        dto1.setCreatedAt(LocalDateTime.now());
        dto1.setCommentCount(0);

        ReportSummaryDTO dto2 = new ReportSummaryDTO();
        dto2.setId(reportId2);
        dto2.setUserId(1L);
        dto2.setUserEmail("attendee@example.com");
        dto2.setCategory(ReportCategory.TICKET);
        dto2.setStatus(ReportStatus.RESOLVED);
        dto2.setShortDescription("Ticket issue...");
        dto2.setCreatedAt(LocalDateTime.now());
        dto2.setCommentCount(2);

        summaryDTOs.add(dto1);
        summaryDTOs.add(dto2);

        // Mock service
        when(reportService.getReportsByUserEmail("attendee@example.com")).thenReturn(summaryDTOs);

        // Act
        mockMvc.perform(get("/api/attendee/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reportId1.toString()))
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].userEmail").value("attendee@example.com"))
                .andExpect(jsonPath("$[0].category").value("PAYMENT"))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].id").value(reportId2.toString()))
                .andExpect(jsonPath("$[1].category").value("TICKET"));

        // Verify
        verify(authService).getCurrentUser();
        verify(reportService).getReportsByUserEmail("attendee@example.com");
    }

    @Test
    @DisplayName("Mendapatkan laporan berdasarkan ID milik pengguna ATTENDEE")
    public void testGetReportById() throws Exception {
        // Arrange
        UUID reportId = UUID.randomUUID();

        ReportResponseDTO responseDTO = new ReportResponseDTO();
        responseDTO.setId(reportId);
        responseDTO.setUserId(1L);
        responseDTO.setUserEmail("attendee@example.com");
        responseDTO.setCategory(ReportCategory.PAYMENT);
        responseDTO.setDescription("Test description");
        responseDTO.setStatus(ReportStatus.PENDING);
        responseDTO.setCreatedAt(LocalDateTime.now());

        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(reportService.getReportById(reportId)).thenReturn(responseDTO);

        // Act
        mockMvc.perform(get("/api/attendee/reports/{id}", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.userEmail").value("attendee@example.com"))
                .andExpect(jsonPath("$.category").value("PAYMENT"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Verify
        verify(authService).getCurrentUser();
        verify(reportService).getReportById(reportId);
    }

    @Test
    @DisplayName("Menambahkan komentar pada laporan milik pengguna ATTENDEE")
    public void testAddComment() throws Exception {
        // Arrange
        UUID reportId = UUID.randomUUID();
        Long userId = 1L;
        String userEmail = "attendee@example.com";

        ReportResponseDTO responseDTO = new ReportResponseDTO();
        responseDTO.setId(reportId);
        responseDTO.setUserId(userId);
        responseDTO.setUserEmail(userEmail);

        when(authService.getCurrentUser()).thenReturn(mockUser);
        when(reportService.getReportById(reportId)).thenReturn(responseDTO);

        CreateReportCommentRequest commentRequest = new CreateReportCommentRequest();
        commentRequest.setMessage("Test comment from attendee");

        UUID commentId = UUID.randomUUID();
        ReportCommentDTO commentDTO = new ReportCommentDTO();
        commentDTO.setId(commentId);
        commentDTO.setReportId(reportId);
        commentDTO.setResponderId(userId);
        commentDTO.setResponderEmail(userEmail);
        commentDTO.setResponderRole("ATTENDEE");
        commentDTO.setMessage("Test comment from attendee");
        commentDTO.setCreatedAt(LocalDateTime.now());

        // Act
        when(reportService.addComment(eq(reportId), any(CreateReportCommentRequest.class))).thenReturn(commentDTO);

        mockMvc.perform(post("/api/attendee/reports/{reportId}/comments", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.reportId").value(reportId.toString()))
                .andExpect(jsonPath("$.responderId").value(userId))
                .andExpect(jsonPath("$.responderEmail").value(userEmail))
                .andExpect(jsonPath("$.responderRole").value("ATTENDEE"));

        // Verify
        verify(authService).getCurrentUser();
        verify(reportService).getReportById(reportId);
        verify(reportService).addComment(eq(reportId), any(CreateReportCommentRequest.class));
    }

    @Test
    @DisplayName("Mendapatkan laporan yang tidak dapat diakses oleh pengguna ATTENDEE")
    public void testGetReportById_Forbidden() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(mockUser);

        UUID reportId = UUID.randomUUID();
        Long userId = 2L;
        String userEmail = "another.attendee@example.com";

        ReportResponseDTO responseDTO = new ReportResponseDTO();
        responseDTO.setId(reportId);
        responseDTO.setUserId(userId);
        responseDTO.setUserEmail(userEmail);
        responseDTO.setCategory(ReportCategory.PAYMENT);
        responseDTO.setDescription("Test description");
        responseDTO.setStatus(ReportStatus.PENDING);

        when(reportService.getReportById(reportId)).thenReturn(responseDTO);

        // Act
        mockMvc.perform(get("/api/attendee/reports/{id}", reportId))
                .andExpect(status().isForbidden());

        // Verify
        verify(authService).getCurrentUser();
        verify(reportService).getReportById(reportId);
    }

    @Test
    @DisplayName("Menambahkan komentar yang tidak dapat diakses oleh pengguna ATTENDEE")
    public void testAddComment_Forbidden() throws Exception {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(mockUser);

        UUID reportId = UUID.randomUUID();
        Long userId = 2L;
        String userEmail = "another.attendee@example.com";

        ReportResponseDTO responseDTO = new ReportResponseDTO();
        responseDTO.setId(reportId);
        responseDTO.setUserId(userId);
        responseDTO.setUserEmail(userEmail);
        responseDTO.setCategory(ReportCategory.PAYMENT);
        responseDTO.setDescription("Test description");
        responseDTO.setStatus(ReportStatus.PENDING);

        when(reportService.getReportById(reportId)).thenReturn(responseDTO);

        CreateReportCommentRequest commentRequest = new CreateReportCommentRequest();
        commentRequest.setMessage("Test comment");

        // Act
        mockMvc.perform(post("/api/attendee/reports/{reportId}/comments", reportId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isForbidden());

        // Verify
        verify(authService).getCurrentUser();
        verify(reportService).getReportById(reportId);
        verify(reportService, never()).addComment(any(), any());
    }
}