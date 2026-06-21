package com.example.demo.service;

import com.example.demo.model.Bookmark;
import com.example.demo.model.Student;
import com.example.demo.model.Event;
import com.example.demo.repository.BookmarkRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("null")
@Service
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final StudentRepository studentRepository;
    private final EventRepository eventRepository;

    public BookmarkServiceImpl(BookmarkRepository bookmarkRepository,
                               StudentRepository studentRepository,
                               EventRepository eventRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.studentRepository = studentRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public Bookmark addBookmark(String rollNumber, Long eventId) {
        Student student = studentRepository.findByRollNumberIgnoreCase(rollNumber.trim())
                .orElseThrow(() -> new RuntimeException("Student not found with roll number: " + rollNumber));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));

        if (bookmarkRepository.existsByStudentRollNumberIgnoreCaseAndEventId(rollNumber, eventId)) {
            throw new RuntimeException("This event is already bookmarked!");
        }

        Bookmark bookmark = new Bookmark(student, event, LocalDateTime.now());
        return bookmarkRepository.save(bookmark);
    }

    @Override
    @Transactional
    public void removeBookmark(String rollNumber, Long eventId) {
        if (!bookmarkRepository.existsByStudentRollNumberIgnoreCaseAndEventId(rollNumber, eventId)) {
            throw new RuntimeException("Bookmark not found for this event!");
        }
        bookmarkRepository.deleteByStudentRollNumberIgnoreCaseAndEventId(rollNumber, eventId);
    }

    @Override
    public List<Bookmark> getBookmarksByStudent(String rollNumber) {
        return bookmarkRepository.findByStudentRollNumberIgnoreCase(rollNumber.trim());
    }

    @Override
    public boolean isBookmarked(String rollNumber, Long eventId) {
        return bookmarkRepository.existsByStudentRollNumberIgnoreCaseAndEventId(rollNumber.trim(), eventId);
    }
}
