package com.example.demo.repository;

import com.example.demo.model.TutorStudentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TutorStudentAssignmentRepository extends JpaRepository<TutorStudentAssignment, Long> {
    List<TutorStudentAssignment> findByTutorUsernameIgnoreCase(String tutorUsername);
    Optional<TutorStudentAssignment> findByTutorUsernameIgnoreCaseAndStudentRegisterNumberIgnoreCase(String tutorUsername, String studentRegisterNumber);
    boolean existsByTutorUsernameIgnoreCaseAndStudentRegisterNumberIgnoreCase(String tutorUsername, String studentRegisterNumber);
}
