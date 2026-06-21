package com.example.demo.repository;

import com.example.demo.model.Competition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    List<Competition> findByEventId(Long eventId);
}
