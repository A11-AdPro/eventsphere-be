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
    @DisplayName("Membuat laporan baru dengan constructor dasar")
    public void testCreateReport_BasicConstructor() {
        // Arrange & Act
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Payment issue");

        // Assert
        assertEquals(1L, report.getUserId());
        assertEquals("user@example.com", report.getUserEmail());
        assertEquals(ReportCategory.PAYMENT, report.getCategory());
        assertEquals("Payment issue", report.getDescription());
        assertEquals(ReportStatus.PENDING, report.getStatus());
        assertNotNull(report.getCreatedAt());
        assertNull(report.getUpdatedAt());
        assertNull(report.getEventId());
        assertNull(report.getEventTitle());
        assertNotNull(report.getResponses());
        assertTrue(report.getResponses().isEmpty());
        assertNotNull(report.getObservers());
        assertTrue(report.getObservers().isEmpty());
    }

    @Test
    @DisplayName("Membuat laporan baru dengan constructor event")
    public void testCreateReport_EventConstructor() {
        // Arrange & Act
        Report report = new Report(1L, "user@example.com", 10L, "Event Title", ReportCategory.EVENT, "Event issue");

        // Assert
        assertEquals(1L, report.getUserId());
        assertEquals("user@example.com", report.getUserEmail());
        assertEquals(10L, report.getEventId());
        assertEquals("Event Title", report.getEventTitle());
        assertEquals(ReportCategory.EVENT, report.getCategory());
        assertEquals("Event issue", report.getDescription());
        assertEquals(ReportStatus.PENDING, report.getStatus());
        assertNotNull(report.getCreatedAt());
        assertNull(report.getUpdatedAt());
    }

    @Test
    @DisplayName("Memeriksa setter dan getter untuk properti laporan")
    public void testSettersAndGetters() {
        // Arrange
        Report report = new Report();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // Act
        report.setId(id);
        report.setUserId(1L);
        report.setUserEmail("user@example.com");
        report.setEventId(10L);
        report.setEventTitle("Event Title");
        report.setCategory(ReportCategory.EVENT);
        report.setDescription("Test description");
        report.setStatus(ReportStatus.ON_PROGRESS);
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        // Assert
        assertEquals(id, report.getId());
        assertEquals(1L, report.getUserId());
        assertEquals("user@example.com", report.getUserEmail());
        assertEquals(10L, report.getEventId());
        assertEquals("Event Title", report.getEventTitle());
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
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Test");
        report.setStatus(ReportStatus.PENDING);

        ReportObserver mockObserver = mock(ReportObserver.class);
        report.addObserver(mockObserver);

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
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Test");

        ReportResponse response = new ReportResponse();
        response.setResponderId(2L);
        response.setMessage("Admin response");

        ReportObserver mockObserver = mock(ReportObserver.class);
        report.addObserver(mockObserver);

        // Act
        report.addResponse(response);

        // Assert
        assertEquals(1, report.getResponses().size());
        assertEquals(response, report.getResponses().getFirst()); // Fixed: use get(0) instead of getFirst()
        assertEquals(report, response.getReport());
        verify(mockObserver).onResponseAdded(report, response);
    }

    @Test
    @DisplayName("Menambahkan observer ke laporan tanpa duplikasi")
    public void testAddObserver() {
        // Arrange
        Report report = new Report();
        ReportObserver observer1 = mock(ReportObserver.class);
        ReportObserver observer2 = mock(ReportObserver.class);

        // Act & Assert - Add first observer
        report.addObserver(observer1);
        assertEquals(1, report.getObservers().size());
        assertTrue(report.getObservers().contains(observer1));

        // Act & Assert - Add duplicate observer (should not add)
        report.addObserver(observer1);
        assertEquals(1, report.getObservers().size());

        // Act & Assert - Add different observer
        report.addObserver(observer2);
        assertEquals(2, report.getObservers().size());
        assertTrue(report.getObservers().contains(observer2));
    }

    @Test
    @DisplayName("Menghapus observer dari laporan")
    public void testRemoveObserver() {
        // Arrange
        Report report = new Report();
        ReportObserver observer = mock(ReportObserver.class);
        report.addObserver(observer);

        // Assert before
        assertEquals(1, report.getObservers().size());
        assertTrue(report.getObservers().contains(observer));

        // Act
        report.removeObserver(observer);

        // Assert after
        assertEquals(0, report.getObservers().size());
        assertFalse(report.getObservers().contains(observer));
    }
}