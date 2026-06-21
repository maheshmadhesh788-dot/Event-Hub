package com.example.demo.service;

import com.example.demo.model.Registration;
import java.util.List;
import java.util.Optional;

public interface RegistrationService {
    Registration registerForEvent(Registration registration, List<Long> competitionIds);
    List<Registration> getAllRegistrations();
    Optional<Registration> getRegistrationById(Long id);
    void deleteRegistration(Long id);
    List<Registration> getRegistrationsByDepartmentId(Long deptId);
    List<Registration> getRegistrationsByStudentRollNumber(String rollNumber);
    Registration checkInParticipant(Long id); // QR Check-in feature
}
