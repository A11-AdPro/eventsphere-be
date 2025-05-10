package id.ac.ui.cs.advprog.eventsphere.report.observer;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.UUID;
import static org.mockito.Mockito.*;

public class ReportObserverTest {

    @Test
    @DisplayName("Memeriksa fungsionalitas interface observer")
    public void testObserverInterface() {
        // Arrange
        ReportObserver observer = mock(ReportObserver.class);

        Report report = new Report(1L, "user@example.com", ReportCategory.PAYMENT, "Test report");
        report.setStatus(ReportStatus.PENDING);

        ReportResponse response = new ReportResponse();
        response.setResponderId(1L);
        response.setResponderEmail("admin@example.com");
        response.setResponderRole("ADMIN");
        response.setMessage("Test response");

        // Act
        observer.onStatusChanged(report, ReportStatus.PENDING, ReportStatus.ON_PROGRESS);
        observer.onResponseAdded(report, response);

        // Assert
        verify(observer).onStatusChanged(report, ReportStatus.PENDING, ReportStatus.ON_PROGRESS);
        verify(observer).onResponseAdded(report, response);
    }
}