package com.example.demo.service;

import com.example.demo.model.Student;
import com.example.demo.model.Registration;
import java.util.List;
import java.util.Optional;

public interface StudentService {
    Optional<Student> findByRollNumber(String rollNumber);
    boolean existsByRollNumber(String rollNumber);
    Student registerStudentProfile(Student student);
    Student updateStudentProfile(String rollNumber, Student updatedStudentData);
    List<Student> getAllStudents();
    void deleteStudent(String rollNumber);
    List<Registration> getRegistrationHistory(String rollNumber);
}
