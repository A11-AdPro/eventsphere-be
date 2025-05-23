package id.ac.ui.cs.advprog.eventsphere.report.model;

import id.ac.ui.cs.advprog.eventsphere.report.observer.ReportObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReportTest {

    @Test
    @DisplayName("Membuat laporan baru dengan parameter yang valid")
    public void testCreateReport() {
        // Arrange
        Long userId = 1L;
        String userEmail = "user@example.com";
        ReportCategory category = ReportCategory.PAYMENT;
        String description = "Payment failed but money was deducted";

        // Act
        Report report = new Report(userId, userEmail, category, description);

        // Assert
        assertEquals(userId, report.getUserId());
        assertEquals(userEmail, report.getUserEmail());
        assertEquals(category, report.getCategory());
        assertEquals(description, report.getDescription());
        assertEquals(ReportStatus.PENDING, report.getStatus());
        assertNotNull(report.getCreatedAt());
        assertNull(report.getUpdatedAt());
        assertTrue(report.getResponses().isEmpty());
    }

    @Test
    @DisplayName("Memeriksa setter dan getter untuk properti laporan")
    public void testReportSettersAndGetters() {
        // Arrange
        Report report = new Report();
        UUID id = UUID.randomUUID();
        Long userId = 1L;
        String userEmail = "user@example.com";
        LocalDateTime now = LocalDateTime.now();

        // Act
        report.setId(id);
        report.setUserId(userId);
        report.setUserEmail(userEmail);
        report.setCategory(ReportCategory.EVENT);
        report.setDescription("Test description");
        report.setStatus(ReportStatus.ON_PROGRESS);
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        // Assert
        assertEquals(id, report.getId());
        assertEquals(userId, report.getUserId());
        assertEquals(userEmail, report.getUserEmail());
        assertEquals(ReportCategory.EVENT, report.getCategory());
        assertEquals("Test description", report.getDescription());
        assertEquals(ReportStatus.ON_PROGRESS, report.getStatus());
        assertEquals(now, report.getCreatedAt());
        assertEquals(now, report.getUpdatedAt());
    }

    @Test
    @DisplayName("Update status laporan dan notifikasi observer")
    public void testUpdateStatus() {
        // Arrange
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Test description");
        report.setStatus(ReportStatus.PENDING);

        ReportObserver mockObserver = mock(ReportObserver.class);
        report.getObservers().add(mockObserver);

        // Act
        report.updateStatus(ReportStatus.ON_PROGRESS);

        // Assert
        assertEquals(ReportStatus.ON_PROGRESS, report.getStatus());
        assertNotNull(report.getUpdatedAt());

        verify(mockObserver).onStatusChanged(report, ReportStatus.PENDING, ReportStatus.ON_PROGRESS);
    }

    @Test
    @DisplayName("Menambahkan response ke laporan dan notifikasi observer")
    public void testAddResponse() {
        // Arrange
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Test description");

        ReportResponse response = new ReportResponse();
        response.setResponderId(2L);
        response.setResponderEmail("admin@example.com");
        response.setResponderRole("ADMIN");
        response.setMessage("Admin response");

        ReportObserver mockObserver = mock(ReportObserver.class);
        report.getObservers().add(mockObserver);

        // Act
        report.addResponse(response);

        // Assert
        assertEquals(1, report.getResponses().size());
        assertEquals(response, report.getResponses().getFirst());
        assertEquals(report, response.getReport());

        verify(mockObserver).onResponseAdded(report, response);
    }

    @Test
    @DisplayName("Menghapus observer dari laporan")
    public void testRemoveObserver() {
        // Arrange
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Test description");

        ReportObserver mockObserver = mock(ReportObserver.class);
        report.getObservers().add(mockObserver);

        // Assert before
        assertEquals(1, report.getObservers().size());

        // Act
        report.removeObserver(mockObserver);

        // Assert after
        assertEquals(0, report.getObservers().size());
    }

    @Test
    @DisplayName("Menambahkan observer ke laporan tanpa duplikasi")
    public void testAddObserver() {
        // Arrange
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Test description");
        ReportObserver mockObserver = mock(ReportObserver.class);

        // Act - tambahkan observer
        report.addObserver(mockObserver);

        // Assert
        assertTrue(report.getObservers().contains(mockObserver));

        // Act - coba tambahkan observer yang sama
        report.addObserver(mockObserver);

        // Assert - memastikan tidak ada duplikasi
        assertEquals(1, report.getObservers().size());
    }
}