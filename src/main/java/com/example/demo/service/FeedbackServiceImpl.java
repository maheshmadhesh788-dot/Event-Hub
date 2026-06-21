package com.example.demo.service;

import com.example.demo.model.Feedback;
import com.example.demo.model.Student;
import com.example.demo.model.Event;
import com.example.demo.repository.FeedbackRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("null")
@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final StudentRepository studentRepository;
    private final EventRepository eventRepository;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepository,
                               StudentRepository studentRepository,
                               EventRepository eventRepository) {
        this.feedbackRepository = feedbackRepository;
        this.studentRepository = studentRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public Feedback submitFeedback(String rollNumber, Long eventId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Student student = studentRepository.findByRollNumberIgnoreCase(rollNumber.trim())
                .orElseThrow(() -> new RuntimeException("Student not found with roll number: " + rollNumber));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));

        if (feedbackRepository.existsByStudentRollNumberIgnoreCaseAndEventId(rollNumber, eventId)) {
            throw new RuntimeException("You have already submitted feedback for this event!");
        }

        Feedback feedback = new Feedback(student, event, rating, comment, LocalDateTime.now());
        return feedbackRepository.save(feedback);
    }

    @Override
    public List<Feedback> getFeedbackForEvent(Long eventId) {
        return feedbackRepository.findByEventId(eventId);
    }

    @Override
    public List<Feedback> getFeedbackByStudent(String rollNumber) {
        return feedbackRepository.findByStudentRollNumberIgnoreCase(rollNumber.trim());
    }

    @Override
    public boolean hasSubmittedFeedback(String rollNumber, Long eventId) {
        return feedbackRepository.existsByStudentRollNumberIgnoreCaseAndEventId(rollNumber.trim(), eventId);
    }
}
