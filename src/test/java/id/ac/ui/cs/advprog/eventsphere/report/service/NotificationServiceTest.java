package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportCategory;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    private JavaMailSender mailSender;
    private UserService userService;
    private NotificationService notificationService;

    @BeforeEach
    public void setUp() {
        mailSender = mock(JavaMailSender.class);
        userService = mock(UserService.class);
        notificationService = new NotificationService(mailSender, userService);
    }

    @Test
    public void testOnStatusChanged() {
        // Create a test report
        UUID userId = UUID.randomUUID();
        Report report = new Report(userId, ReportCategory.PAYMENT, "Payment issue");
        report.setId(UUID.randomUUID());

        // Mock userService
        when(userService.getUserEmail(userId)).thenReturn("user@example.com");

        // Call the method being tested
        notificationService.onStatusChanged(report, ReportStatus.PENDING, ReportStatus.ON_PROGRESS);

        // Capture and verify the email sent
        ArgumentCaptor<SimpleMailMessage> emailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(emailCaptor.capture());

        SimpleMailMessage sentEmail = emailCaptor.getValue();
        assertEquals("user@example.com", sentEmail.getTo()[0]);
        assertTrue(sentEmail.getSubject().contains("Report Status Updated"));
        assertTrue(sentEmail.getText().contains("PENDING"));
        assertTrue(sentEmail.getText().contains("ON_PROGRESS"));
    }

    @Test
    public void testOnResponseAdded() {
        // Create a test report
        UUID userId = UUID.randomUUID();
        Report report = new Report(userId, ReportCategory.TICKET, "Ticket issue");
        report.setId(UUID.randomUUID());

        // Create a test response
        ReportResponse response = new ReportResponse(UUID.randomUUID(), "ADMIN", "Admin response", report);

        // Mock userService
        when(userService.getUserEmail(userId)).thenReturn("user@example.com");

        // Call the method being tested
        notificationService.onResponseAdded(report, response);

        // Capture and verify the email sent
        ArgumentCaptor<SimpleMailMessage> emailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(emailCaptor.capture());

        SimpleMailMessage sentEmail = emailCaptor.getValue();
        assertEquals("user@example.com", sentEmail.getTo()[0]);
        assertTrue(sentEmail.getSubject().contains("New Response"));
        assertTrue(sentEmail.getText().contains("Admin response"));
    }

    @Test
    public void testNotifyNewReport() {
        // Create a test report
        Report report = new Report(UUID.randomUUID(), ReportCategory.EVENT, "Event issue");
        report.setId(UUID.randomUUID());

        // Mock userService
        List<String> adminEmails = Arrays.asList("admin1@example.com", "admin2@example.com");
        when(userService.getAdminEmails()).thenReturn(adminEmails);

        // Call the method being tested
        notificationService.notifyNewReport(report);

        // Capture and verify the email sent
        ArgumentCaptor<SimpleMailMessage> emailCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(emailCaptor.capture());

        SimpleMailMessage sentEmail = emailCaptor.getValue();
        assertArrayEquals(new String[]{"admin1@example.com", "admin2@example.com"}, sentEmail.getTo());
        assertTrue(sentEmail.getSubject().contains("New Report"));
        assertTrue(sentEmail.getText().contains("Event issue"));
    }
}