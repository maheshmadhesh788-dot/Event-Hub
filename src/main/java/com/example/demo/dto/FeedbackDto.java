package com.example.demo.dto;

import java.time.LocalDateTime;

public class FeedbackDto {
    private Long id;
    private String studentRollNumber;
    private String studentName;
    private Long eventId;
    private String eventName;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public FeedbackDto() {}

    public FeedbackDto(Long id, String studentRollNumber, String studentName, Long eventId, String eventName, int rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.studentRollNumber = studentRollNumber;
        this.studentName = studentName;
        this.eventId = eventId;
        this.eventName = eventName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentRollNumber() {
        return studentRollNumber;
    }

    public void setStudentRollNumber(String studentRollNumber) {
        this.studentRollNumber = studentRollNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
