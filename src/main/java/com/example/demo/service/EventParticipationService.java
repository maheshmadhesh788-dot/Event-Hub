package com.example.demo.service;

import com.example.demo.model.EventParticipation;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventParticipationService {
    EventParticipation save(EventParticipation participation);
    List<EventParticipation> getReport(String category, LocalDate startDate, LocalDate endDate);
    List<EventParticipation> getByRollNumber(String rollNumber);
    List<EventParticipation> getAll();
    void delete(Long id);
    Optional<EventParticipation> getById(Long id);
}
