package com.example.demo.repository;

import com.example.demo.model.EventParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface EventParticipationRepository extends JpaRepository<EventParticipation, Long> {
    List<EventParticipation> findByRollNumberIgnoreCase(String rollNumber);
    List<EventParticipation> findByEventCategoryAndEventDateBetween(String eventCategory, LocalDate startDate, LocalDate endDate);
    List<EventParticipation> findByEventDateBetween(LocalDate startDate, LocalDate endDate);
}
