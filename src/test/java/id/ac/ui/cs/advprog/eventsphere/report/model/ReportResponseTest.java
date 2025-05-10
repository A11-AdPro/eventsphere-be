package id.ac.ui.cs.advprog.eventsphere.report.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class ReportResponseTest {

    @Test
    @DisplayName("Memeriksa setter dan getter untuk properti response laporan")
    public void testReportResponseSettersAndGetters() {
        // Arrange
        ReportResponse response = new ReportResponse();
        UUID id = UUID.randomUUID();
        Long responderId = 1L;
        String responderEmail = "admin@example.com";
        Report report = new Report();
        LocalDateTime now = LocalDateTime.now();

        // Act
        response.setId(id);
        response.setReport(report);
        response.setResponderId(responderId);
        response.setResponderEmail(responderEmail);
        response.setResponderRole("ADMIN");
        response.setMessage("Test response message");
        response.setCreatedAt(now);

        // Assert
        assertEquals(id, response.getId());
        assertEquals(report, response.getReport());
        assertEquals(responderId, response.getResponderId());
        assertEquals(responderEmail, response.getResponderEmail());
        assertEquals("ADMIN", response.getResponderRole());
        assertEquals("Test response message", response.getMessage());
        assertEquals(now, response.getCreatedAt());
    }

    @Test
    @DisplayName("Validasi panjang pesan tidak melebihi batas maksimum")
    public void testReportResponseValidation() {
        // Arrange
        ReportResponse response = new ReportResponse();

        // Membuat pesan yang melebihi panjang maksimum
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 501; i++) {
            longMessage.append("a");
        }

        // Set nilai yang valid
        response.setResponderId(1L);
        response.setResponderEmail("admin@example.com");
        response.setResponderRole("ADMIN");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            response.setMessage(longMessage.toString());
        });

        // Test dengan pesan yang valid
        String validMessage = "This is a valid message";
        response.setMessage(validMessage);
        assertEquals(validMessage, response.getMessage());
    }

    @Test
    @DisplayName("Membuat response laporan baru dengan constructor parameter lengkap")
    public void testReportResponseConstructor() {
        // Arrange
        Long responderId = 1L;
        String responderEmail = "admin@example.com";
        String responderRole = "ADMIN";
        String message = "Test message";
        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Test report");

        // Act
        ReportResponse response = new ReportResponse(responderId, responderEmail, responderRole, message, report);

        // Assert
        assertEquals(responderId, response.getResponderId());
        assertEquals(responderEmail, response.getResponderEmail());
        assertEquals(responderRole, response.getResponderRole());
        assertEquals(message, response.getMessage());
        assertEquals(report, response.getReport());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    @DisplayName("Pesan dapat diset sebagai null")
    public void testReportResponseSetMessageNull() {
        // Arrange
        ReportResponse response = new ReportResponse();

        // Act
        response.setMessage(null);

        // Assert
        assertNull(response.getMessage());
    }
}
