package com.example.demo.service;

import com.example.demo.model.Event;
import com.example.demo.model.Competition;
import com.example.demo.model.Registration;
import com.example.demo.model.Notification;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("null")
@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public EventServiceImpl(EventRepository eventRepository,
                            RegistrationRepository registrationRepository,
                            NotificationRepository notificationRepository,
                            EmailService emailService) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    @Override
    public List<Event> getCollegeEvents() {
        return eventRepository.findByType("COLLEGE");
    }

    @Override
    public List<Event> getDepartmentEvents(Long deptId) {
        return eventRepository.findByDepartmentId(deptId);
    }

    @Override
    public List<Event> searchEvents(String name) {
        return eventRepository.findByNameContainingIgnoreCase(name.trim());
    }

    @Override
    public List<Event> searchAndFilterEvents(String name, String type, String category, String date, String venue, String sortBy) {
        List<Event> allEvents = eventRepository.findAll();
        java.util.stream.Stream<Event> stream = allEvents.stream();

        if (name != null && !name.trim().isEmpty()) {
            String lowerName = name.trim().toLowerCase();
            stream = stream.filter(e -> e.getName().toLowerCase().contains(lowerName) 
                                     || (e.getDescription() != null && e.getDescription().toLowerCase().contains(lowerName)));
        }

        if (type != null && !type.trim().isEmpty() && !type.trim().equalsIgnoreCase("ALL")) {
            stream = stream.filter(e -> e.getType().equalsIgnoreCase(type.trim()));
        }

        if (category != null && !category.trim().isEmpty()) {
            stream = stream.filter(e -> e.getCategory() != null && e.getCategory().equalsIgnoreCase(category.trim()));
        }

        if (date != null && !date.trim().isEmpty()) {
            stream = stream.filter(e -> e.getDateTime() != null && e.getDateTime().toLocalDate().toString().equals(date.trim()));
        }

        if (venue != null && !venue.trim().isEmpty()) {
            String lowerVenue = venue.trim().toLowerCase();
            stream = stream.filter(e -> e.getVenue().toLowerCase().contains(lowerVenue));
        }

        // Sorting
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            switch (sortBy.trim().toLowerCase()) {
                case "date_asc":
                    stream = stream.sorted(java.util.Comparator.comparing(Event::getDateTime, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
                    break;
                case "date_desc":
                    stream = stream.sorted(java.util.Comparator.comparing(Event::getDateTime, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())));
                    break;
                case "name_asc":
                    stream = stream.sorted(java.util.Comparator.comparing(Event::getName, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
                    break;
                case "name_desc":
                    stream = stream.sorted(java.util.Comparator.comparing(Event::getName, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())));
                    break;
                default:
                    break;
            }
        } else {
            stream = stream.sorted(java.util.Comparator.comparing(Event::getDateTime, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));
        }

        return stream.collect(java.util.stream.Collectors.toList());
    }

    void validateEventDateTime(@org.springframework.lang.Nullable LocalDateTime dateTime, String fieldPrefix, boolean checkPastDate) {
        if (dateTime == null) {
            throw new IllegalArgumentException(fieldPrefix + " date/time is required.");
        }
        
        // 1. Must be current date/time onward (i.e. >= today at 00:00:00)
        if (checkPastDate) {
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            if (dateTime.isBefore(todayStart)) {
                throw new IllegalArgumentException(fieldPrefix + " date must be from the current date onward. Past dates are not allowed.");
            }
        }
        
        // 2. Restricted to 8:00 AM to 3:00 PM (08:00 to 15:00 inclusive)
        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();
        if (hour < 8 || hour > 15 || (hour == 15 && minute > 0)) {
            throw new IllegalArgumentException(fieldPrefix + " time must be scheduled between 8:00 AM and 3:00 PM.");
        }
    }

    @Override
    @Transactional
    public Event createEvent(Event event) {
        if (event.getDateTime() == null) {
            event.setDateTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0));
        }
        validateEventDateTime(event.getDateTime(), "Event", true);
        
        if (event.getCompetitions() != null) {
            for (Competition comp : event.getCompetitions()) {
                validateEventDateTime(comp.getDateTime(), "Competition '" + comp.getName() + "'", true);
                comp.setEvent(event);
            }
        }
        
        Event saved = eventRepository.save(event);

        // Auto-generate notification upon creation
        Notification notice;
        if ("COLLEGE".equalsIgnoreCase(saved.getType())) {
            notice = new Notification(
                "New College Event: " + saved.getName(),
                "A new college-level event '" + saved.getName() + "' has been scheduled at " + saved.getVenue() + ". Check the details and register now!",
                "College Admin",
                LocalDateTime.now(),
                saved.getId()
            );
        } else {
            notice = new Notification(
                "New Event in " + (saved.getDepartment() != null ? saved.getDepartment().getCode() : "DEPT") + ": " + saved.getName(),
                "The Department of " + (saved.getDepartment() != null ? saved.getDepartment().getName() : "DEPT") + " has scheduled a new event '" + saved.getName() + "' at " + saved.getVenue() + ".",
                "Department of " + (saved.getDepartment() != null ? saved.getDepartment().getCode() : "DEPT"),
                LocalDateTime.now(),
                saved.getId()
            );
        }
        notificationRepository.save(notice);

        return saved;
    }

    @Override
    @Transactional
    public Event updateEvent(Long id, Event updatedData) {
        return eventRepository.findById(id).map(event -> {
            event.setName(updatedData.getName().trim());
            event.setVenue(updatedData.getVenue().trim());
            if (updatedData.getDateTime() != null) {
                boolean dateChanged = !updatedData.getDateTime().equals(event.getDateTime());
                validateEventDateTime(updatedData.getDateTime(), "Event", dateChanged);
                event.setDateTime(updatedData.getDateTime());
            }
            event.setDescription(updatedData.getDescription());
            if (updatedData.getPosterUrl() != null && !updatedData.getPosterUrl().trim().isEmpty()) {
                event.setPosterUrl(updatedData.getPosterUrl().trim());
            }
            if (updatedData.getCategory() != null) {
                event.setCategory(updatedData.getCategory());
            }
            if (updatedData.getCapacity() != null) {
                event.setCapacity(updatedData.getCapacity());
            }
            event.setInChargeStaffName(updatedData.getInChargeStaffName());
            event.setInChargeStaffContact(updatedData.getInChargeStaffContact());
            event.setRegistrationDeadline(updatedData.getRegistrationDeadline());
            event.setRulesGuidelines(updatedData.getRulesGuidelines());
            
            // Sync competitions list using in-place merge to avoid orphanRemoval issues
            java.util.Map<Long, Competition> existingComps = new java.util.HashMap<>();
            for (Competition c : event.getCompetitions()) {
                if (c.getId() != null) {
                    existingComps.put(c.getId(), c);
                }
            }
            
            java.util.List<Competition> incomingComps = updatedData.getCompetitions();
            java.util.Set<Long> incomingIds = new java.util.HashSet<>();
            if (incomingComps != null) {
                for (Competition comp : incomingComps) {
                    if (comp.getId() != null) {
                        incomingIds.add(comp.getId());
                    }
                }
            }
            
            // 1. Remove orphaned competitions (present in DB but not in incoming update)
            event.getCompetitions().removeIf(c -> c.getId() != null && !incomingIds.contains(c.getId()));
            
            // 2. Add or update incoming competitions
            if (incomingComps != null) {
                for (Competition comp : incomingComps) {
                    boolean checkPastDate = true;
                    if (comp.getId() != null && existingComps.containsKey(comp.getId())) {
                        // Update existing in-place
                        Competition targetComp = existingComps.get(comp.getId());
                        if (comp.getDateTime() != null && comp.getDateTime().equals(targetComp.getDateTime())) {
                            checkPastDate = false;
                        }
                        validateEventDateTime(comp.getDateTime(), "Competition '" + comp.getName() + "'", checkPastDate);
                        
                        targetComp.setName(comp.getName());
                        targetComp.setVenue(comp.getVenue());
                        targetComp.setDateTime(comp.getDateTime());
                        targetComp.setInChargeStaffName(comp.getInChargeStaffName());
                        targetComp.setInChargeStaffContact(comp.getInChargeStaffContact());
                        targetComp.setCompetitionType(comp.getCompetitionType());
                        targetComp.setMaxParticipants(comp.getMaxParticipants());
                    } else {
                        // Create and add new
                        validateEventDateTime(comp.getDateTime(), "Competition '" + comp.getName() + "'", checkPastDate);
                        Competition newComp = new Competition();
                        newComp.setEvent(event);
                        newComp.setName(comp.getName());
                        newComp.setVenue(comp.getVenue());
                        newComp.setDateTime(comp.getDateTime());
                        newComp.setInChargeStaffName(comp.getInChargeStaffName());
                        newComp.setInChargeStaffContact(comp.getInChargeStaffContact());
                        newComp.setCompetitionType(comp.getCompetitionType());
                        newComp.setMaxParticipants(comp.getMaxParticipants());
                        event.getCompetitions().add(newComp);
                    }
                }
            }
            Event saved = eventRepository.saveAndFlush(event);
            emailService.sendScheduleUpdate(saved, "Event coordinates or schedule updated by coordinator.");
            return saved;
        }).orElseThrow(() -> new RuntimeException("Event not found with ID: " + id));
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        eventRepository.findById(id).ifPresent(event -> {
            // Delete registrations associated with the event
            List<Registration> registrations = registrationRepository.findByEvent(event);
            registrationRepository.deleteAll(registrations);
            eventRepository.delete(event);
        });
    }

    @Override
    public List<Registration> getEventRegistrations(Long eventId) {
        return registrationRepository.findByEventId(eventId);
    }
}
