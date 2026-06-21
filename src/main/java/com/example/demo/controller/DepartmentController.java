package com.example.demo.controller;

import com.example.demo.model.Department;
import com.example.demo.model.Event;
import com.example.demo.model.Notification;
import com.example.demo.model.Registration;
import com.example.demo.service.DepartmentService;
import com.example.demo.service.EventService;
import com.example.demo.service.RegistrationService;
import com.example.demo.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("null")
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final EventService eventService;
    private final RegistrationService registrationService;
    private final NotificationService notificationService;

    public DepartmentController(DepartmentService departmentService,
                                EventService eventService,
                                RegistrationService registrationService,
                                NotificationService notificationService) {
        this.departmentService = departmentService;
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.notificationService = notificationService;
    }

    // --- List Departments without Password ---
    @GetMapping
    public List<Map<String, Object>> getAllDepartments() {
        return departmentService.getAllDepartments().stream().map(dept -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", dept.getId());
            map.put("name", dept.getName());
            map.put("code", dept.getCode());
            map.put("description", dept.getDescription());
            map.put("logoUrl", dept.getLogoUrl());
            map.put("coverImageUrl", dept.getCoverImageUrl());
            return map;
        }).collect(Collectors.toList());
    }



    // --- Update Department Profile Details ---
    @PutMapping("/{id}/details")
    public ResponseEntity<?> updateDepartmentDetails(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            Department updateData = new Department();
            updateData.setName(payload.get("name"));
            updateData.setCode(payload.get("code"));
            updateData.setPassword(payload.get("password"));
            updateData.setDescription(payload.get("description"));
            updateData.setLogoUrl(payload.get("logoUrl"));
            updateData.setCoverImageUrl(payload.get("coverImageUrl"));

            // Check uniqueness in controller or let service throw
            if (updateData.getName() != null && !updateData.getName().trim().isEmpty()) {
                Optional<Department> existing = departmentService.findByName(updateData.getName());
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Department name already exists"));
                }
            }
            if (updateData.getCode() != null && !updateData.getCode().trim().isEmpty()) {
                Optional<Department> existing = departmentService.findByCode(updateData.getCode());
                if (existing.isPresent() && !existing.get().getId().equals(id)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Department code already exists"));
                }
            }

            Department updated = departmentService.updateDepartment(id, updateData);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // --- Department Event CRUD ---

    @GetMapping("/{id}/events")
    public ResponseEntity<?> getEvents(@PathVariable Long id) {
        if (!departmentService.getDepartmentById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(eventService.getDepartmentEvents(id));
    }

    @PostMapping("/{id}/events")
    public ResponseEntity<?> createEvent(@PathVariable Long id, @RequestBody Event event) {
        try {
            return departmentService.getDepartmentById(id)
                .map(dept -> {
                    event.setType("DEPARTMENT");
                    event.setDepartment(dept);
                    Event saved = eventService.createEvent(event);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/events/{eventId}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @PathVariable Long eventId, @RequestBody Event eventDetails) {
        if (!departmentService.getDepartmentById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        try {
            return eventService.getEventById(eventId)
                .map(event -> {
                    if (event.getDepartment() == null || !event.getDepartment().getId().equals(id)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized: Event does not belong to this department"));
                    }
                    Event updated = eventService.updateEvent(eventId, eventDetails);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/events/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id, @PathVariable Long eventId) {
        if (!departmentService.getDepartmentById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        return eventService.getEventById(eventId)
            .map(event -> {
                if (event.getDepartment() == null || !event.getDepartment().getId().equals(id)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized: Event does not belong to this department"));
                }
                eventService.deleteEvent(eventId);
                return ResponseEntity.ok().build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // --- Department Registration Records ---

    @GetMapping("/{id}/registrations")
    public ResponseEntity<?> getRegistrations(@PathVariable Long id) {
        if (!departmentService.getDepartmentById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        List<Registration> registrations = registrationService.getRegistrationsByDepartmentId(id);
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/{id}/events/{eventId}/registrations")
    public ResponseEntity<?> getEventRegistrations(@PathVariable Long id, @PathVariable Long eventId) {
        if (!departmentService.getDepartmentById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return eventService.getEventById(eventId)
            .map(event -> {
                if (event.getDepartment() == null || !event.getDepartment().getId().equals(id)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                return ResponseEntity.ok(eventService.getEventRegistrations(eventId));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // --- Send Department Notifications ---

    @PostMapping("/{id}/notifications")
    public ResponseEntity<?> createNotification(@PathVariable Long id, @RequestBody Notification notification) {
        return departmentService.getDepartmentById(id)
            .map(dept -> {
                notification.setSender("Department of " + dept.getCode());
                notification.setCreatedAt(LocalDateTime.now());
                Notification saved = notificationService.createNotification(notification);
                return ResponseEntity.ok(saved);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // --- Department QR Check-in Endpoint ---
    @PostMapping("/{id}/checkin/{registrationId}")
    public ResponseEntity<?> checkInParticipant(@PathVariable Long id, @PathVariable Long registrationId) {
        if (!departmentService.getDepartmentById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        try {
            Optional<Registration> regOpt = registrationService.getRegistrationById(registrationId);
            if (regOpt.isPresent()) {
                Registration reg = regOpt.get();
                if (reg.getEvent() != null && reg.getEvent().getDepartment() != null && 
                    reg.getEvent().getDepartment().getId().equals(id)) {
                    Registration updated = registrationService.checkInParticipant(registrationId);
                    return ResponseEntity.ok(updated);
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized: Event does not belong to this department"));
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
