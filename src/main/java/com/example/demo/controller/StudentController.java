package com.example.demo.controller;

import com.example.demo.model.Event;
import com.example.demo.model.Registration;
import com.example.demo.model.Student;
import com.example.demo.model.Feedback;
import com.example.demo.model.Bookmark;
import com.example.demo.service.StudentService;
import com.example.demo.service.EventService;
import com.example.demo.service.RegistrationService;
import com.example.demo.service.FeedbackService;
import com.example.demo.service.BookmarkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@SuppressWarnings("null")
@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;
    private final EventService eventService;
    private final RegistrationService registrationService;
    private final FeedbackService feedbackService;
    private final BookmarkService bookmarkService;
    private final PasswordEncoder passwordEncoder;

    public StudentController(StudentService studentService,
                             EventService eventService,
                             RegistrationService registrationService,
                             FeedbackService feedbackService,
                             BookmarkService bookmarkService,
                             PasswordEncoder passwordEncoder) {
        this.studentService = studentService;
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.feedbackService = feedbackService;
        this.bookmarkService = bookmarkService;
        this.passwordEncoder = passwordEncoder;
    }

    // --- View Events ---

    @GetMapping("/events")
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/events/search")
    public List<Event> searchEvents(@RequestParam(required = false) String name,
                                    @RequestParam(required = false) String type,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) String date,
                                    @RequestParam(required = false) String venue,
                                    @RequestParam(required = false) String sortBy) {
        return eventService.searchAndFilterEvents(name, type, category, date, venue, sortBy);
    }

    @GetMapping("/events/college")
    public List<Event> getCollegeEvents() {
        return eventService.getCollegeEvents();
    }

    @GetMapping("/events/department/{deptId}")
    public List<Event> getDepartmentEvents(@PathVariable Long deptId) {
        return eventService.getDepartmentEvents(deptId);
    }

    // --- Register for an Event ---

    @PostMapping("/register")
    public ResponseEntity<?> registerForEvent(@RequestBody Map<String, Object> payload) {
        try {
            String studentName = (String) payload.get("studentName");
            String rollNumber = (String) payload.get("rollNumber");
            String department = (String) payload.get("department");
            String year = (String) payload.get("year");
            String contactNumber = (String) payload.get("contactNumber");
            String email = (String) payload.get("email");
            String password = (String) payload.get("password");
            
            Object eventIdObj = payload.get("eventId");
            if (eventIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Event ID is required"));
            }
            
            Long eventId = Long.valueOf(eventIdObj.toString());

            if (studentName == null || studentName.trim().isEmpty() ||
                rollNumber == null || rollNumber.trim().isEmpty() ||
                department == null || department.trim().isEmpty() ||
                year == null || year.trim().isEmpty() ||
                contactNumber == null || contactNumber.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
                
                return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));
            }

            Optional<Student> studentOpt = studentService.findByRollNumber(rollNumber);
            if (studentOpt.isPresent()) {
                // Verify password matches
                Student existingStudent = studentOpt.get();
                if (!passwordEncoder.matches(password.trim(), existingStudent.getPassword())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Wrong Password"));
                }
            } else {
                // Pre-register the student profile with the correct password provided by the user
                Student newStudent = new Student();
                newStudent.setRollNumber(rollNumber.trim().toUpperCase());
                newStudent.setStudentName(studentName.trim());
                newStudent.setDepartment(department.trim());
                newStudent.setContactNumber(contactNumber.trim());
                newStudent.setEmail(email.trim());
                newStudent.setPassword(password.trim());
                studentService.registerStudentProfile(newStudent);
            }

            // Prepare registration object
            Registration reg = new Registration();
            reg.setStudentName(studentName);
            reg.setRollNumber(rollNumber);
            reg.setDepartment(department);
            reg.setYear(year);
            reg.setContactNumber(contactNumber);
            reg.setEmail(email);
            
            Event eventPlaceholder = new Event();
            eventPlaceholder.setId(eventId);
            reg.setEvent(eventPlaceholder);

            // Retrieve selected competition IDs
            List<?> compIdsRaw = (List<?>) payload.get("competitionIds");
            List<Long> competitionIds = new ArrayList<>();
            if (compIdsRaw != null) {
                for (Object o : compIdsRaw) {
                    competitionIds.add(Long.valueOf(o.toString()));
                }
            }

            Registration saved = registrationService.registerForEvent(reg, competitionIds);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }


    @GetMapping("/check/{rollNumber}")
    public ResponseEntity<?> checkStudentExists(@PathVariable String rollNumber) {
        boolean exists = studentService.existsByRollNumber(rollNumber);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // --- Student Dashboard History ---

    @GetMapping("/registrations/{rollNumber}")
    public ResponseEntity<?> getRegistrationHistory(@PathVariable String rollNumber) {
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Roll number is required"));
        }
        List<Registration> list = studentService.getRegistrationHistory(rollNumber);
        return ResponseEntity.ok(list);
    }


    @PutMapping("/profile/{rollNumber}")
    public ResponseEntity<?> updateStudentProfile(@PathVariable String rollNumber, @RequestBody Map<String, String> payload) {
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // --- FEEDBACK AND RATINGS SYSTEM ---

    @PostMapping("/feedback")
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String, Object> payload) {
        try {
            String rollNumber = (String) payload.get("rollNumber");
            Long eventId = Long.valueOf(payload.get("eventId").toString());
            int rating = Integer.parseInt(payload.get("rating").toString());
            String comment = (String) payload.get("comment");

            Feedback saved = feedbackService.submitFeedback(rollNumber, eventId, rating, comment);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/feedback/check/{rollNumber}/{eventId}")
    public ResponseEntity<?> checkFeedbackSubmitted(@PathVariable String rollNumber, @PathVariable Long eventId) {
        boolean submitted = feedbackService.hasSubmittedFeedback(rollNumber, eventId);
        return ResponseEntity.ok(Map.of("submitted", submitted));
    }

    // --- BOOKMARKS / WISHLIST ---

    @PostMapping("/bookmarks/toggle")
    public ResponseEntity<?> toggleBookmark(@RequestBody Map<String, Object> payload) {
        try {
            String rollNumber = (String) payload.get("rollNumber");
            Long eventId = Long.valueOf(payload.get("eventId").toString());

            if (bookmarkService.isBookmarked(rollNumber, eventId)) {
                bookmarkService.removeBookmark(rollNumber, eventId);
                return ResponseEntity.ok(Map.of("bookmarked", false, "message", "Bookmark removed"));
            } else {
                bookmarkService.addBookmark(rollNumber, eventId);
                return ResponseEntity.ok(Map.of("bookmarked", true, "message", "Bookmark added"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/bookmarks/{rollNumber}")
    public ResponseEntity<?> getBookmarks(@PathVariable String rollNumber) {
        try {
            List<Bookmark> list = bookmarkService.getBookmarksByStudent(rollNumber);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/bookmarks/check/{rollNumber}/{eventId}")
    public ResponseEntity<?> checkBookmark(@PathVariable String rollNumber, @PathVariable Long eventId) {
        try {
            boolean bookmarked = bookmarkService.isBookmarked(rollNumber, eventId);
            return ResponseEntity.ok(Map.of("bookmarked", bookmarked));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
