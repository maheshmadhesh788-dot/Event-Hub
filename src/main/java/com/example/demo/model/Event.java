package com.example.demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Event {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "event_name", nullable = false)
    private String name;
    
    @Column(name = "venue", nullable = false)
    private String venue;
    
    @Column(name = "event_date", nullable = false)
    private LocalDateTime dateTime;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "image")
    private String posterUrl;
    
    @Column(name = "event_type", nullable = false)
    private String type; // "COLLEGE" or "DEPARTMENT"
    
    @Transient
    private String category;

    @Column(name = "max_participants")
    private Integer capacity;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Department department;

    @Column(name = "in_charge_staff_name")
    private String inChargeStaffName;

    @Column(name = "in_charge_staff_contact")
    private String inChargeStaffContact;

    @Column(name = "registration_deadline")
    private LocalDateTime registrationDeadline;

    @Column(name = "rules_guidelines", columnDefinition = "TEXT")
    private String rulesGuidelines;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<Competition> competitions = new ArrayList<>();

    public Event() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getInChargeStaffName() {
        return inChargeStaffName;
    }

    public void setInChargeStaffName(String inChargeStaffName) {
        this.inChargeStaffName = inChargeStaffName;
    }

    public String getInChargeStaffContact() {
        return inChargeStaffContact;
    }

    public void setInChargeStaffContact(String inChargeStaffContact) {
        this.inChargeStaffContact = inChargeStaffContact;
    }

    public List<Competition> getCompetitions() {
        return competitions;
    }

    public void setCompetitions(List<Competition> competitions) {
        this.competitions = competitions;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public LocalDateTime getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(LocalDateTime registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public String getRulesGuidelines() {
        return rulesGuidelines;
    }

    public void setRulesGuidelines(String rulesGuidelines) {
        this.rulesGuidelines = rulesGuidelines;
    }
}
