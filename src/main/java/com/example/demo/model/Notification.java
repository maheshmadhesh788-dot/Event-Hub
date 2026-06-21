package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(nullable = false)
    private String sender; // e.g. "College Admin" or "Department of IT"
    
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private Long eventId;

    public Notification() {}

    public Notification(String title, String content, String sender, LocalDateTime createdAt) {
        this.title = title;
        this.content = content;
        this.sender = sender;
        this.createdAt = createdAt;
    }

    public Notification(String title, String content, String sender, LocalDateTime createdAt, Long eventId) {
        this.title = title;
        this.content = content;
        this.sender = sender;
        this.createdAt = createdAt;
        this.eventId = eventId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}
