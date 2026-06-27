package com.example.demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

@Entity
@Table(name = "achievements")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "register_number", nullable = false)
    private String registerNumber;

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "tutor_name", nullable = false)
    private String tutorName;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "achievement", nullable = false)
    private String achievement;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "academic_year", nullable = false)
    private String academicYear;

    @Column(name = "created_by_username", nullable = false)
    private String createdByUsername;

    public Achievement() {}

    public Achievement(String studentName, String registerNumber, String department, String tutorName, 
                       String eventName, String eventType, String achievement, LocalDate eventDate, 
                       String academicYear, String createdByUsername) {
        this.studentName = studentName;
        this.registerNumber = registerNumber;
        this.department = department;
        this.tutorName = tutorName;
        this.eventName = eventName;
        this.eventType = eventType;
        this.achievement = achievement;
        this.eventDate = eventDate;
        this.academicYear = academicYear;
        this.createdByUsername = createdByUsername;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getTutorName() {
        return tutorName;
    }

    public void setTutorName(String tutorName) {
        this.tutorName = tutorName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getAchievement() {
        return achievement;
    }

    public void setAchievement(String achievement) {
        this.achievement = achievement;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }
}
