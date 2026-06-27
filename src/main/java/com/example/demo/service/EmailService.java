package com.example.demo.service;

import com.example.demo.model.Registration;
import com.example.demo.model.Event;
import com.example.demo.model.Student;

public interface EmailService {
    void sendRegistrationConfirmation(Registration registration);
    void sendScheduleUpdate(Event event, String detailChanges);
    void sendEventReminder(Registration registration);
    void sendStudentAccountCreated(Student student);
}
