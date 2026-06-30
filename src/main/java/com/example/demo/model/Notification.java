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
    
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "sender", nullable = true)
    private String sender; // e.g. "College Admin" or "Department of IT"
    
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdAt;

    @Transient
    private Long eventId;

    @Column(name = "image_url", nullable = true)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

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

    public Notification(String title, String content, String sender, LocalDateTime createdAt, Long eventId, String imageUrl) {
        this.title = title;
        this.content = content;
        this.sender = sender;
        this.createdAt = createdAt;
        this.eventId = eventId;
        this.imageUrl = imageUrl;
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
        if (sender != null) {
            return sender;
        }
        if (user != null) {
            if ("ROLE_SUPER_ADMIN".equals(user.getRole()) || "ROLE_ADMIN".equals(user.getRole())) {
                return "College Admin";
            }
            return user.getName();
        }
        return "System Notification";
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
