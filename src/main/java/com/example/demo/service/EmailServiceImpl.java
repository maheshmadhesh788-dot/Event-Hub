package com.example.demo.service;

import com.example.demo.model.Registration;
import com.example.demo.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Override
    public void sendRegistrationConfirmation(Registration registration) {
        String eventName = registration.getEvent().getName();
        String compDetails = registration.getCompetition() != null ? " - " + registration.getCompetition().getName() : "";
        
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================================");
        sb.append("\n[SIMULATED EMAIL SENT]");
        sb.append("\nTo: ").append(registration.getEmail());
        sb.append("\nSubject: Registration Confirmed - ").append(eventName).append(compDetails);
        sb.append("\nDear ").append(registration.getStudentName()).append(",");
        sb.append("\n\nYou have successfully registered for: ").append(eventName).append(compDetails);
        sb.append("\nEvent Venue: ").append(registration.getEvent().getVenue());
        sb.append("\nDate & Time: ").append(registration.getEvent().getDateTime());
        sb.append("\nYour Registration ID is: ").append(registration.getId());
        sb.append("\n\nUse your dashboard to access your QR Entry Pass.");
        sb.append("\nRegards,\nEvent Hub Coordination Team");
        sb.append("\n========================================================");
        
        logger.info(sb.toString());
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
}
