package com.example.demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "tutor_student_assignments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TutorStudentAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tutor_username", nullable = false)
    private String tutorUsername;

    @Column(name = "student_register_number", nullable = false)
    private String studentRegisterNumber;

    public TutorStudentAssignment() {}

    public TutorStudentAssignment(String tutorUsername, String studentRegisterNumber) {
        this.tutorUsername = tutorUsername;
        this.studentRegisterNumber = studentRegisterNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTutorUsername() {
        return tutorUsername;
    }

    public void setTutorUsername(String tutorUsername) {
        this.tutorUsername = tutorUsername;
    }

    public String getStudentRegisterNumber() {
        return studentRegisterNumber;
    }

    public void setStudentRegisterNumber(String studentRegisterNumber) {
        this.studentRegisterNumber = studentRegisterNumber;
    }
}
