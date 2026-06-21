package com.example.demo.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookmarks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_roll_number", "event_id"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Bookmark {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_roll_number", nullable = false)
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private Student student;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnoreProperties({"competitions", "hibernateLazyInitializer", "handler"})
    private Event event;
    
    @Column(nullable = false)
    private LocalDateTime bookmarkedAt;

    public Bookmark() {}

    public Bookmark(Student student, Event event, LocalDateTime bookmarkedAt) {
        this.student = student;
        this.event = event;
        this.bookmarkedAt = bookmarkedAt;
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

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public LocalDateTime getBookmarkedAt() {
        return bookmarkedAt;
    }

    public void setBookmarkedAt(LocalDateTime bookmarkedAt) {
        this.bookmarkedAt = bookmarkedAt;
    }
}
