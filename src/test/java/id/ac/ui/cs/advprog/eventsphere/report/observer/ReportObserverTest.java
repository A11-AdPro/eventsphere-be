package id.ac.ui.cs.advprog.eventsphere.report.observer;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.mockito.Mockito.*;

public class ReportObserverTest {

    @Test
    public void testObserverInterface() {
        // Create mock implementation of the interface
        ReportObserver observer = mock(ReportObserver.class);

        // Create test objects
        Report report = new Report(UUID.randomUUID(), ReportCategory.PAYMENT, "Test report");
        report.setStatus(ReportStatus.PENDING);

        ReportResponse response = new ReportResponse();
        response.setResponderId(UUID.randomUUID());
        response.setResponderRole("ADMIN");
        response.setMessage("Test response");

        // Call interface methods
        observer.onStatusChanged(report, ReportStatus.PENDING, ReportStatus.ON_PROGRESS);
        observer.onResponseAdded(report, response);

        // Verify methods were called
        verify(observer).onStatusChanged(report, ReportStatus.PENDING, ReportStatus.ON_PROGRESS);
        verify(observer).onResponseAdded(report, response);
    }
}