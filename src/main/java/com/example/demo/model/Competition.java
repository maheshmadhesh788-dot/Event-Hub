package com.example.demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "competitions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Competition {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "competition_name", nullable = false)
    private String name;
    
    @Column(name = "venue")
    private String venue;
    
    @Column(name = "date_time")
    private LocalDateTime dateTime;
    
    @Column(name = "in_charge_staff_name")
    private String inChargeStaffName;

    @Column(name = "in_charge_staff_contact")
    private String inChargeStaffContact;
    
    @Column(name = "competition_type")
    private String competitionType;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonBackReference
    private Event event;

    public Competition() {}

    public Competition(String name, String venue, LocalDateTime dateTime, String inChargeStaffName, String inChargeStaffContact, Event event) {
        this.name = name;
        this.venue = venue;
        this.dateTime = dateTime;
        this.inChargeStaffName = inChargeStaffName;
        this.inChargeStaffContact = inChargeStaffContact;
        this.event = event;
    }

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

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getCompetitionType() {
        return competitionType;
    }

    public void setCompetitionType(String competitionType) {
        this.competitionType = competitionType;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }
}
