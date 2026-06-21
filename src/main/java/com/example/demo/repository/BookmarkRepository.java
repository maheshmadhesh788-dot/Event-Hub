package com.example.demo.repository;

import com.example.demo.model.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByStudentRollNumberIgnoreCase(String rollNumber);
    Optional<Bookmark> findByStudentRollNumberIgnoreCaseAndEventId(String rollNumber, Long eventId);
    boolean existsByStudentRollNumberIgnoreCaseAndEventId(String rollNumber, Long eventId);
    void deleteByStudentRollNumberIgnoreCaseAndEventId(String rollNumber, Long eventId);
}
