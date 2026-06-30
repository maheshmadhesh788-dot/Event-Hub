package com.example.demo.controller;

import com.example.demo.model.EventParticipation;
import com.example.demo.service.EventParticipationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/participation")
public class EventParticipationController {

    private final EventParticipationService eventParticipationService;

    public EventParticipationController(EventParticipationService eventParticipationService) {
        this.eventParticipationService = eventParticipationService;
    }

    // ==========================================
    // 1. SAVE / CREATE / UPDATE PARTICIPATION
    // ==========================================
    @PostMapping
    public ResponseEntity<?> createParticipation(@RequestBody EventParticipation participation, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized access"));
        }

        String role = authentication.getAuthorities().iterator().next().getAuthority();
        if ("ROLE_STUDENT".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Students are not allowed to enter participation records."));
        }

        if ("ROLE_DEPARTMENT".equals(role)) {
            String deptName = authentication.getName();
            if (!com.example.demo.util.DepartmentNormalizer.normalize(participation.getDepartment()).equalsIgnoreCase(com.example.demo.util.DepartmentNormalizer.normalize(deptName))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You can only manage participation records for your department (" + deptName + ")."));
            }
        } else if (!"ROLE_ADMIN".equals(role) && !"ROLE_SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access Denied"));
        }

        if (participation.getStudentName() == null || participation.getStudentName().trim().isEmpty() ||
            participation.getRollNumber() == null || participation.getRollNumber().trim().isEmpty() ||
            participation.getDepartment() == null || participation.getDepartment().trim().isEmpty() ||
            participation.getYear() == null || participation.getYear().trim().isEmpty() ||
            participation.getTutorName() == null || participation.getTutorName().trim().isEmpty() ||
            participation.getEventName() == null || participation.getEventName().trim().isEmpty() ||
            participation.getEventCategory() == null || participation.getEventCategory().trim().isEmpty() ||
            participation.getEventDate() == null ||
            participation.getResult() == null || participation.getResult().trim().isEmpty() ||
            participation.getCertificateStatus() == null || participation.getCertificateStatus().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "All fields are required."));
        }

        // Save
        EventParticipation saved = eventParticipationService.save(participation);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateParticipation(@PathVariable Long id, @RequestBody EventParticipation participationDetails, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized access"));
        }

        String role = authentication.getAuthorities().iterator().next().getAuthority();
        if ("ROLE_STUDENT".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Students are not allowed to edit participation records."));
        }

        Optional<EventParticipation> existingOpt = eventParticipationService.getById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Record not found"));
        }

        EventParticipation existing = existingOpt.get();
        if ("ROLE_DEPARTMENT".equals(role)) {
            String deptName = authentication.getName();
            if (!com.example.demo.util.DepartmentNormalizer.normalize(existing.getDepartment()).equalsIgnoreCase(com.example.demo.util.DepartmentNormalizer.normalize(deptName))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access Denied"));
            }
        } else if (!"ROLE_ADMIN".equals(role) && !"ROLE_SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access Denied"));
        }
        existing.setStudentName(participationDetails.getStudentName());
        existing.setRollNumber(participationDetails.getRollNumber());
        existing.setDepartment(participationDetails.getDepartment());
        existing.setYear(participationDetails.getYear());
        existing.setTutorName(participationDetails.getTutorName());
        existing.setEventName(participationDetails.getEventName());
        existing.setEventCategory(participationDetails.getEventCategory());
        existing.setEventDate(participationDetails.getEventDate());
        existing.setResult(participationDetails.getResult());
        existing.setCertificateStatus(participationDetails.getCertificateStatus());

        EventParticipation saved = eventParticipationService.save(existing);
        return ResponseEntity.ok(saved);
    }

    // ==========================================
    // 2. REPORT GENERATION
    // ==========================================
    @GetMapping("/report")
    public ResponseEntity<?> getReport(
            @RequestParam(required = false) String category,
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication authentication) {
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized access"));
        }

        String role = authentication.getAuthorities().iterator().next().getAuthority();
        if ("ROLE_STUDENT".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Students are not allowed to access report generation."));
        }

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<EventParticipation> report = eventParticipationService.getReport(category, start, end);
            if ("ROLE_DEPARTMENT".equals(role)) {
                String deptName = authentication.getName();
                report = report.stream()
                        .filter(ep -> ep.getDepartment() != null && com.example.demo.util.DepartmentNormalizer.normalize(ep.getDepartment()).equalsIgnoreCase(com.example.demo.util.DepartmentNormalizer.normalize(deptName)))
                        .collect(java.util.stream.Collectors.toList());
            }
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid parameters or date formats. Use YYYY-MM-DD."));
        }
    }

    // ==========================================
    // 3. STUDENT VIEW OWN RECORDS
    // ==========================================
    @GetMapping("/my")
    public ResponseEntity<?> getMyParticipations(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized access"));
        }

        String username = authentication.getName(); // For student, this is their roll number
        List<EventParticipation> records = eventParticipationService.getByRollNumber(username);
        return ResponseEntity.ok(records);
    }

    // ==========================================
    // 4. DELETE PARTICIPATION RECORD
    // ==========================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteParticipation(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized access"));
        }

        String role = authentication.getAuthorities().iterator().next().getAuthority();
        if ("ROLE_STUDENT".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Students are not allowed to delete participation records."));
        }

        Optional<EventParticipation> existingOpt = eventParticipationService.getById(id);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Record not found"));
        }

        if ("ROLE_DEPARTMENT".equals(role)) {
            String deptName = authentication.getName();
            if (!com.example.demo.util.DepartmentNormalizer.normalize(existingOpt.get().getDepartment()).equalsIgnoreCase(com.example.demo.util.DepartmentNormalizer.normalize(deptName))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access Denied"));
            }
        } else if (!"ROLE_ADMIN".equals(role) && !"ROLE_SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access Denied"));
        }

        eventParticipationService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Record deleted successfully"));
    }
}
