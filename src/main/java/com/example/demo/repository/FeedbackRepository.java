package com.example.demo.repository;

import com.example.demo.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByEventId(Long eventId);
    List<Feedback> findByStudentRollNumberIgnoreCase(String rollNumber);
    boolean existsByStudentRollNumberIgnoreCaseAndEventId(String rollNumber, Long eventId);

    @org.springframework.data.jpa.repository.Query("SELECT f FROM Feedback f WHERE f.event.department.id = :deptId")
    List<Feedback> findByDepartmentId(@org.springframework.data.repository.query.Param("deptId") Long deptId);

    @org.springframework.data.jpa.repository.Query("SELECT f FROM Feedback f WHERE f.event.type = 'COLLEGE'")
    List<Feedback> findByCollegeEvents();
}
