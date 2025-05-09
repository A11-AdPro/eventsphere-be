package id.ac.ui.cs.advprog.eventsphere.report.service;

import id.ac.ui.cs.advprog.eventsphere.report.model.Report;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportResponse;
import id.ac.ui.cs.advprog.eventsphere.report.model.ReportStatus;
import id.ac.ui.cs.advprog.eventsphere.report.observer.ReportObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService implements ReportObserver {

    private final JavaMailSender mailSender;
    private final UserService userService;

    @Autowired
    public NotificationService(JavaMailSender mailSender, UserService userService) {
        this.mailSender = mailSender;
        this.userService = userService;
    }

    @Override
    public void onStatusChanged(Report report, ReportStatus oldStatus, ReportStatus newStatus) {
        // Get the user's email
        String userEmail = userService.getUserEmail(report.getUserId());

        // Create email message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userEmail);
        message.setSubject("Report Status Updated - #" + report.getId());
        message.setText("Your report status has been updated from " + oldStatus + " to " + newStatus + ".\n\n" +
                "Report Details:\n" +
                "Category: " + report.getCategory().getDisplayName() + "\n" +
                "Description: " + report.getDescription());

        // Send the email
        mailSender.send(message);
    }

    @Override
    public void onResponseAdded(Report report, ReportResponse response) {
        // Get the user's email
        String userEmail = userService.getUserEmail(report.getUserId());

        // Create email message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(userEmail);
        message.setSubject("New Response to Your Report - #" + report.getId());
        message.setText("A new response has been added to your report:\n\n" +
                "From: " + response.getResponderRole() + "\n" +
                "Message: " + response.getMessage() + "\n\n" +
                "Report Details:\n" +
                "Category: " + report.getCategory().getDisplayName() + "\n" +
                "Status: " + report.getStatus().getDisplayName());

        // Send the email
        mailSender.send(message);
    }

    public void notifyNewReport(Report report) {
        // Get admin emails
        List<String> adminEmails = userService.getAdminEmails();

        // Create email message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(adminEmails.toArray(new String[0]));
        message.setSubject("New Report Submitted - #" + report.getId());
        message.setText("A new report has been submitted:\n\n" +
                "Category: " + report.getCategory().getDisplayName() + "\n" +
                "Description: " + report.getDescription() + "\n\n" +
                "Please review this report in the admin dashboard.");

        // Send the email
        mailSender.send(message);
    }

    public void notifyOrganizerOfReport(Report report, UUID eventId) {
        // Get organizer emails for the event
        List<String> organizerEmails = userService.getOrganizerEmails(eventId);

        // Create email message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(organizerEmails.toArray(new String[0]));
        message.setSubject("New Report Related to Your Event - #" + report.getId());
        message.setText("A new report has been submitted for an event you manage:\n\n" +
                "Category: " + report.getCategory().getDisplayName() + "\n" +
                "Description: " + report.getDescription() + "\n\n" +
                "Please review this report in your organizer dashboard.");

        // Send the email
        mailSender.send(message);
    }
}