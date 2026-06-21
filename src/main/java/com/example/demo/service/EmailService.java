package com.example.demo.service;

import com.example.demo.model.Registration;
import com.example.demo.model.Event;

public interface EmailService {
    void sendRegistrationConfirmation(Registration registration);
    void sendScheduleUpdate(Event event, String detailChanges);
    void sendEventReminder(Registration registration);
}
