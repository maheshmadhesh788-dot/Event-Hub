package com.example.demo.service;

import com.example.demo.model.Feedback;
import java.util.List;

public interface FeedbackService {
    Feedback submitFeedback(String rollNumber, Long eventId, int rating, String comment);
    List<Feedback> getFeedbackForEvent(Long eventId);
    List<Feedback> getFeedbackByStudent(String rollNumber);
    boolean hasSubmittedFeedback(String rollNumber, Long eventId);
}
