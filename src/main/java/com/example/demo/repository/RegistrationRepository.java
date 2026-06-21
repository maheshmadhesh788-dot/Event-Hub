package com.example.demo.repository;

import com.example.demo.model.Registration;
import com.example.demo.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByRollNumberIgnoreCase(String rollNumber);
    List<Registration> findByEvent(Event event);
    List<Registration> findByEventId(Long eventId);
    List<Registration> findByEvent_Type(String eventType);
    List<Registration> findByEvent_DepartmentId(Long departmentId);
    List<Registration> findByCompetitionId(Long competitionId);
    List<Registration> findByRollNumberIgnoreCaseAndCompetitionId(String rollNumber, Long competitionId);
}
