package com.example.demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;

@Entity
@Table(name = "event_participation")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EventParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Student student;

    @Column(name = "student_name", nullable = false)
    private String studentName;

    @Column(name = "roll_number", nullable = false)
    private String rollNumber;

    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "year", nullable = false)
    private String year;

    @Column(name = "tutor_name", nullable = false)
    private String tutorName;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "event_category", nullable = false)
    private String eventCategory;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "result", nullable = false)
    private String result;

    @Column(name = "certificate_status", nullable = false)
    private String certificateStatus;

    @Column(name = "academic_year")
    private String academicYear;

    public EventParticipation() {}

    public EventParticipation(Student student, String studentName, String rollNumber, String department, 
                            String year, String tutorName, String eventName, String eventCategory, 
                            LocalDate eventDate, String result, String certificateStatus) {
        this.student = student;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.department = department;
        this.year = year;
        this.tutorName = tutorName;
        this.eventName = eventName;
        this.eventCategory = eventCategory;
        this.eventDate = eventDate;
        this.result = result;
        this.certificateStatus = certificateStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getDepartment() {
        return com.example.demo.util.DepartmentNormalizer.normalize(department);
    }

    public void setDepartment(String department) {
        this.department = com.example.demo.util.DepartmentNormalizer.normalize(department);
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
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

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCertificateStatus() {
        return certificateStatus;
    }

    public void setCertificateStatus(String certificateStatus) {
        this.certificateStatus = certificateStatus;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }
}
