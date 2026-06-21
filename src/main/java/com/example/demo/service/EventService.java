package com.example.demo.service;

import com.example.demo.model.Event;
import com.example.demo.model.Registration;
import java.util.List;
import java.util.Optional;

public interface EventService {
    List<Event> getAllEvents();
    Optional<Event> getEventById(Long id);
    List<Event> getCollegeEvents();
    List<Event> getDepartmentEvents(Long deptId);
    List<Event> searchEvents(String name);
    List<Event> searchAndFilterEvents(String name, String type, String category, String date, String venue, String sortBy);
    Event createEvent(Event event);
    Event updateEvent(Long id, Event updatedData);
    void deleteEvent(Long id);
    List<Registration> getEventRegistrations(Long eventId);
}
