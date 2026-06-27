package com.example.demo.service;

import com.example.demo.model.Registration;
import com.example.demo.model.Event;
import com.example.demo.model.Student;
import com.example.demo.model.Notification;
import com.example.demo.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final Set<Long> sentRegistrationIds = ConcurrentHashMap.newKeySet();
    private final NotificationRepository notificationRepository;

    public EmailServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void sendRegistrationConfirmation(Registration registration) {
        sendEmailDirect(registration);
    }

    private void sendEmailDirect(Registration registration) {
        try {
            if (registration == null) {
                throw new IllegalArgumentException("Registration record is null");
            }
            if (registration.getId() != null) {
                if (sentRegistrationIds.contains(registration.getId())) {
                    logger.info("Duplicate registration email check: Email already sent for registration ID " + registration.getId());
                    return;
                }
            }

            // 1. Verify valid registered email address
            String email = registration.getEmail();
            if (email == null || !email.trim().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
                throw new IllegalArgumentException("Invalid email address: " + email);
            }

            String eventName = registration.getEvent() != null ? registration.getEvent().getName() : "Unknown Event";
            String compDetails = registration.getCompetition() != null ? " - " + registration.getCompetition().getName() : "";
            
            StringBuilder sb = new StringBuilder();
            sb.append("\n========================================================");
            sb.append("\n[SIMULATED EMAIL SENT]");
            sb.append("\nTo: ").append(email.trim());
            sb.append("\nSubject: Event Registration Successful");
            sb.append("\nDear ").append(registration.getStudentName()).append(",");
            sb.append("\n\nWe are pleased to inform you that your registration for the event has been completed successfully.");
            sb.append("\n\nRegistration Details:");
            sb.append("\n- Student Name: ").append(registration.getStudentName());
            sb.append("\n- Register Number / Roll Number: ").append(registration.getRollNumber());
            sb.append("\n- Email Address: ").append(email.trim());
            sb.append("\n- Department: ").append(registration.getDepartment());
            sb.append("\n- Event Name: ").append(eventName).append(compDetails);
            if (registration.getEvent() != null && registration.getEvent().getDateTime() != null) {
                sb.append("\n- Event Date: ").append(registration.getEvent().getDateTime());
            }
            if (registration.getId() != null) {
                sb.append("\n- Registration ID: ").append(registration.getId());
            }
            sb.append("\n- Registration Status: Successfully Registered");
            sb.append("\n\nRegards,\nEvent Hub Coordination Team");
            sb.append("\n========================================================");
            
            logger.info(sb.toString());

            // Track sent registration to prevent duplicates
            if (registration.getId() != null) {
                sentRegistrationIds.add(registration.getId());
            }
        } catch (Exception e) {
            logger.error("Failed to send event registration confirmation email", e);
            // Notify the administrator without affecting the registration process
            try {
                String content = "Failed to send event registration confirmation email. Error details: " + e.getMessage() + 
                                 (registration != null ? " | Registration ID: " + registration.getId() + " | Email: " + registration.getEmail() : "");
                Notification notif = new Notification("Email Delivery Failure Warning", content, "System", LocalDateTime.now());
                notificationRepository.save(notif);
            } catch (Exception ex) {
                logger.error("Failed to save admin notification for email failure", ex);
            }
        }
    }

    @Override
    public void sendScheduleUpdate(Event event, String detailChanges) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================================");
        sb.append("\n[SIMULATED BROADCAST EMAIL SENT]");
        sb.append("\nTo: [All Registered Students of event: ").append(event.getName()).append("]");
        sb.append("\nSubject: IMPORTANT: Schedule Update for ").append(event.getName());
        sb.append("\nDear Student,");
        sb.append("\n\nThe schedule or details for the event '").append(event.getName()).append("' has been updated.");
        sb.append("\n\nNew Venue: ").append(event.getVenue());
        sb.append("\nNew Date & Time: ").append(event.getDateTime());
        sb.append("\nUpdates Detail: ").append(detailChanges);
        sb.append("\n\nRegards,\nEvent Hub Coordination Team");
        sb.append("\n========================================================");
        
        logger.info(sb.toString());
    }

    @Override
    public void sendEventReminder(Registration registration) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================================");
        sb.append("\n[SIMULATED EMAIL REMINDER SENT]");
        sb.append("\nTo: ").append(registration.getEmail());
        sb.append("\nSubject: Reminder: Upcoming Event ").append(registration.getEvent().getName());
        sb.append("\nDear ").append(registration.getStudentName()).append(",");
        sb.append("\n\nThis is a reminder that the event '").append(registration.getEvent().getName()).append("' will start soon.");
        sb.append("\nVenue: ").append(registration.getEvent().getVenue());
        sb.append("\nTime: ").append(registration.getEvent().getDateTime());
        sb.append("\n\nDon't forget to present your QR entry pass at check-in.");
        sb.append("\nRegards,\nEvent Hub Coordination Team");
        sb.append("\n========================================================");
        
        logger.info(sb.toString());
    }

    @Override
    public void sendStudentAccountCreated(Student student) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================================");
        sb.append("\n[SIMULATED EMAIL SENT]");
        sb.append("\nTo: ").append(student.getEmail());
        sb.append("\nSubject: Welcome to Event Hub! - Account Created");
        sb.append("\nDear ").append(student.getStudentName()).append(",");
        sb.append("\n\nYour student account has been successfully created.");
        sb.append("\nUsername / Roll Number: ").append(student.getRollNumber());
        sb.append("\nDepartment: ").append(student.getDepartment());
        sb.append("\nContact: ").append(student.getContactNumber());
        sb.append("\n\nYou can now log in to the Student Dashboard to view and register for competitions.");
        sb.append("\nRegards,\nEvent Hub Coordination Team");
        sb.append("\n========================================================");
        
        logger.info(sb.toString());
    }
}
