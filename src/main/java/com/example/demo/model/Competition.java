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
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String venue;
    
    @Column(nullable = false)
    private LocalDateTime dateTime;
    
    private String inChargeStaffName;
    private String inChargeStaffContact;
    
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
}
