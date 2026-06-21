package com.example.demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "students")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Student {
    
    @Id
    @Column(nullable = false, unique = true)
    private String rollNumber;
    
    @Column(nullable = false)
    private String studentName;
    
    @Column(nullable = false)
    private String department;
    
    @Column(nullable = false)
    private String contactNumber;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password; // hashed password

    public Student() {}

    public Student(String rollNumber, String studentName, String department, String contactNumber, String email, String password) {
        this.rollNumber = rollNumber;
        this.studentName = studentName;
        this.department = department;
        this.contactNumber = contactNumber;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
