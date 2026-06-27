package com.example.demo.controller;

import com.example.demo.model.Department;
import com.example.demo.model.Event;
import com.example.demo.model.Notification;
import com.example.demo.model.Registration;
import com.example.demo.model.Student;
import com.example.demo.service.DepartmentService;
import com.example.demo.service.EventService;
import com.example.demo.service.RegistrationService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.StudentService;
import com.example.demo.service.FeedbackService;
import com.example.demo.model.Feedback;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final NotificationService notificationService;
    private final StudentService studentService;
    private final DepartmentService departmentService;
    private final FeedbackService feedbackService;

    public AdminController(EventService eventService,
                           RegistrationService registrationService,
                           NotificationService notificationService,
                           StudentService studentService,
                           DepartmentService departmentService,
                           FeedbackService feedbackService) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.notificationService = notificationService;
        this.studentService = studentService;
        this.departmentService = departmentService;
        this.feedbackService = feedbackService;
    }

    // ==========================================
    // 1. EVENTS CRUD (College & Department Level)
    // ==========================================

    @GetMapping("/events")
    public List<Event> getCollegeEvents() {
        return eventService.getCollegeEvents();
    }

    @GetMapping("/all-events")
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @PostMapping("/events")
    public ResponseEntity<?> createCollegeEvent(@RequestBody Event event) {
        try {
            event.setType("COLLEGE");
            event.setDepartment(null);
            Event saved = eventService.createEvent(event);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/events/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody Event eventDetails) {
        try {
            Event updated = eventService.updateEvent(id, eventDetails);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==========================================
    // 2. REGISTRATIONS MANAGEMENT
    // ==========================================

    @GetMapping("/registrations")
    public List<Registration> getCollegeRegistrations() {
        // Find registrations where event type is COLLEGE
        List<Registration> all = registrationService.getAllRegistrations();
        List<Registration> collegeRegs = new ArrayList<>();
        for (Registration r : all) {
            if ("COLLEGE".equalsIgnoreCase(r.getEvent().getType())) {
                collegeRegs.add(r);
            }
        }
        return collegeRegs;
    }

    @GetMapping("/all-registrations")
    public List<Registration> getAllRegistrations() {
        return registrationService.getAllRegistrations();
    }

    @GetMapping("/events/{eventId}/registrations")
    public List<Registration> getEventRegistrations(@PathVariable Long eventId) {
        return eventService.getEventRegistrations(eventId);
    }

    @DeleteMapping("/registrations/{id}")
    public ResponseEntity<?> deleteRegistration(@PathVariable Long id) {
        try {
            registrationService.deleteRegistration(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==========================================
    // 3. STUDENT PROFILES CRUD
    // ==========================================

    @GetMapping("/students")
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    @PutMapping("/students/{rollNumber}")
    public ResponseEntity<?> updateStudent(@PathVariable String rollNumber, @RequestBody Map<String, String> payload) {
        try {
            Student updateData = new Student();
            updateData.setStudentName(payload.get("studentName"));
            updateData.setDepartment(payload.get("department"));
            updateData.setContactNumber(payload.get("contactNumber"));
            updateData.setEmail(payload.get("email"));
            updateData.setPassword(payload.get("password"));

            Student updated = studentService.updateStudentProfile(rollNumber, updateData);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/students/{rollNumber}")
    public ResponseEntity<?> deleteStudent(@PathVariable String rollNumber) {
        try {
            studentService.deleteStudent(rollNumber);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==========================================
    // 4. DEPARTMENT WORKSPACES CRUD
    // ==========================================

    @GetMapping("/departments")
    public List<Department> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    @PostMapping("/departments")
    public ResponseEntity<?> createDepartment(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String password = payload.get("password");

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        }

        if (departmentService.findByName(name).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Department already exists"));
        }

        Department dept = new Department();
        dept.setName(name.trim());
        dept.setPassword(password != null ? password.trim() : "");
        dept.setHodName(payload.get("hodName"));
        dept.setContactNumber(payload.get("contactNumber"));
        dept.setEmail(payload.get("email"));
        dept.setDescription(payload.get("description"));
        dept.setLogoUrl(payload.get("logoUrl"));
        dept.setCoverImageUrl(payload.get("coverImageUrl"));
        
        // Generate temporary code, service overrides/uniquifies it
        String[] words = name.trim().split("\\s+");
        String code = "";
        if (words.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (String w : words) {
                if (!w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0)));
            }
            code = sb.toString();
        } else {
            String w = name.trim().toUpperCase();
            code = w.length() > 3 ? w.substring(0, 3) : w;
        }
        dept.setCode(code);

        Department saved = departmentService.createDepartment(dept);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            Department updateData = new Department();
            updateData.setName(payload.get("name"));
            updateData.setCode(payload.get("code"));
            updateData.setPassword(payload.get("password"));
            updateData.setDescription(payload.get("description"));
            updateData.setLogoUrl(payload.get("logoUrl"));
            updateData.setCoverImageUrl(payload.get("coverImageUrl"));
            updateData.setHodName(payload.get("hodName"));
            updateData.setContactNumber(payload.get("contactNumber"));
            updateData.setEmail(payload.get("email"));

            Department updated = departmentService.updateDepartment(id, updateData);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        try {
            departmentService.deleteDepartment(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==========================================
    // 5. NOTIFICATION MANAGEMENT
    // ==========================================

    @PostMapping("/notifications")
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        notification.setSender("College Admin");
        notification.setCreatedAt(LocalDateTime.now());
        Notification saved = notificationService.createNotification(notification);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==========================================
    // 6. ANALYTICS & REPORTS (PREMIUM)
    // ==========================================

    @GetMapping("/analytics")
    public ResponseEntity<?> getDashboardAnalytics() {
        List<Event> events = eventService.getAllEvents();
        List<Registration> registrations = registrationService.getAllRegistrations();
        List<Student> students = studentService.getAllStudents();
        List<Department> departments = departmentService.getAllDepartments();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEvents", events.size());
        stats.put("totalRegistrations", registrations.size());
        stats.put("totalStudents", students.size());
        stats.put("totalDepartments", departments.size());

        // Registrations per Event map
        Map<String, Integer> regPerEvent = new HashMap<>();
        for (Registration r : registrations) {
            String eventName = r.getEvent().getName();
            regPerEvent.put(eventName, regPerEvent.getOrDefault(eventName, 0) + 1);
        }
        stats.put("registrationsPerEvent", regPerEvent);

        // Registrations per Student Department map
        Map<String, Integer> regPerDept = new HashMap<>();
        for (Registration r : registrations) {
            String deptName = r.getDepartment();
            regPerDept.put(deptName, regPerDept.getOrDefault(deptName, 0) + 1);
        }
        stats.put("registrationsPerDept", regPerDept);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/reports/csv")
    public ResponseEntity<byte[]> downloadRegistrationsCsv() {
        try {
            List<Registration> list = registrationService.getAllRegistrations();
            StringWriter writer = new StringWriter();
            
            // CSV Header
            writer.write("ID,Student Name,Roll Number,Student Dept,Event Name,Event Level,Venue,Registration Date,Status\n");
            
            for (Registration r : list) {
                writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        r.getId(),
                        r.getStudentName().replace(",", " "),
                        r.getRollNumber(),
                        r.getDepartment().replace(",", " "),
                        r.getEvent().getName().replace(",", " "),
                        r.getEvent().getType(),
                        r.getEvent().getVenue().replace(",", " "),
                        r.getRegistrationDate().toString(),
                        r.getStatus()
                ));
            }
            
            byte[] csvBytes = writer.toString().getBytes("UTF-8");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "event_registrations_report.csv");
            
            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/checkin/{registrationId}")
    public ResponseEntity<?> checkInParticipant(@PathVariable Long registrationId) {
        try {
            Registration updated = registrationService.checkInParticipant(registrationId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- Get feedback for college events ---
    @GetMapping("/feedback")
    public ResponseEntity<?> getCollegeFeedback() {
        return ResponseEntity.ok(feedbackService.getFeedbackForCollege());
    }
}
