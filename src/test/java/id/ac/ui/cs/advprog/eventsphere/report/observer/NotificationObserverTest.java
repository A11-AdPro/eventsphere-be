package id.ac.ui.cs.advprog.eventsphere.report.observer;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class NotificationObserverTest {

    @Test
    public void testAttendeeNotificationOnStatusChange() {
        NotificationObserver attendeeObserver = Mockito.mock(AttendeeNotificationObserver.class);
        ReportSubject reportSubject = new ReportSubjectImpl();

        reportSubject.registerObserver(attendeeObserver);

        Report report = new Report(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Test Report",
                "Test Description",
                ReportType.PAYMENT,
                ReportStatus.PENDING,
                LocalDateTime.now()
        );

        reportSubject.notifyStatusChange(report, ReportStatus.ON_PROGRESS);

        verify(attendeeObserver, times(1)).onStatusChange(report, ReportStatus.ON_PROGRESS);
    }

    @Test
    public void testAdminNotificationOnNewReport() {
        NotificationObserver adminObserver = Mockito.mock(AdminNotificationObserver.class);
        ReportSubject reportSubject = new ReportSubjectImpl();

        reportSubject.registerObserver(adminObserver);

        Report report = new Report(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "New Report",
                "New Description",
                ReportType.TICKET,
                ReportStatus.PENDING,
                LocalDateTime.now()
        );

        reportSubject.notifyNewReport(report);

        verify(adminObserver, times(1)).onNewReport(report);
    }

    @Test
    public void testRemovingObserver() {
        NotificationObserver observer = Mockito.mock(NotificationObserver.class);
        ReportSubject reportSubject = new ReportSubjectImpl();

        reportSubject.registerObserver(observer);
        reportSubject.removeObserver(observer);

        Report report = new Report(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Test Report",
                "Test Description",
                ReportType.EVENT,
                ReportStatus.PENDING,
                LocalDateTime.now()
        );

        reportSubject.notifyNewReport(report);
        reportSubject.notifyStatusChange(report, ReportStatus.RESOLVED);

        verify(observer, never()).onNewReport(report);
        verify(observer, never()).onStatusChange(report, ReportStatus.RESOLVED);
    }
}
