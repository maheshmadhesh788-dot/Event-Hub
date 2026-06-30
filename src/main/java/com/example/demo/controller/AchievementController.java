package com.example.demo.controller;

import com.example.demo.model.Achievement;
import com.example.demo.model.StaffProfile;
import com.example.demo.model.Student;
import com.example.demo.model.Event;
import com.example.demo.repository.AchievementRepository;
import com.example.demo.repository.StaffProfileRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.model.User;
import com.example.demo.model.Department;
import com.example.demo.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("null")
@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    private final AchievementRepository achievementRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final StudentRepository studentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AchievementController(AchievementRepository achievementRepository,
                                 StaffProfileRepository staffProfileRepository,
                                 StudentRepository studentRepository,
                                 EventRepository eventRepository,
                                 UserRepository userRepository,
                                 DepartmentRepository departmentRepository,
                                 PasswordEncoder passwordEncoder,
                                 EmailService emailService) {
        this.achievementRepository = achievementRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.studentRepository = studentRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // ==========================================
    // 1. AUTO-FILL STUDENT & TUTOR LOOKUP
    // ==========================================
    @GetMapping("/student/{registerNumber}")
    public ResponseEntity<?> getStudentByRegisterNumber(@PathVariable String registerNumber) {
        return studentRepository.findByRollNumberIgnoreCase(registerNumber.trim())
                .map(student -> ResponseEntity.ok(Map.of(
                        "name", student.getStudentName(),
                        "department", student.getDepartment()
                )))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Student not found")));
    }

    @GetMapping("/tutors")
    public ResponseEntity<?> getAllTutors() {
        return ResponseEntity.ok(staffProfileRepository.findAll());
    }

    @PostMapping("/add-account")
    public ResponseEntity<?> addAccount(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");
        String role = payload.get("role"); // ROLE_ADMIN, ROLE_SUPER_ADMIN, ROLE_STUDENT, ROLE_DEPARTMENT
        String name = payload.get("name");
        String email = payload.get("email");
        String contactNumber = payload.get("contactNumber");
        String departmentCode = payload.get("departmentCode"); // only for department/student

        if (username == null || password == null || role == null || name == null ||
            username.trim().isEmpty() || password.trim().isEmpty() || role.trim().isEmpty() || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username, password, role, and name are required."));
        }

        username = username.trim();
        role = role.trim().toUpperCase();
        name = name.trim();

        if (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists in the system."));
        }

        String encodedPassword = passwordEncoder.encode(password.trim());

        // Create specific profiles based on role
        if ("ROLE_STUDENT".equals(role)) {
            try {
                com.example.demo.util.StudentValidator.validateStudentRegistration(
                    name, username, departmentCode != null ? departmentCode : "IT", departmentRepository, studentRepository
                );
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }

            Student student = new Student(username.toUpperCase(), name, departmentCode != null ? departmentCode : "IT",
                    contactNumber != null ? contactNumber : "0000000000", email != null ? email : "student@example.com", encodedPassword);
            Student savedStudent = studentRepository.saveAndFlush(student);
            try {
                emailService.sendStudentAccountCreated(savedStudent);
            } catch (Exception e) {
                // Log or ignore
            }
        }
        String finalUsername = username;
        if ("ROLE_DEPARTMENT".equals(role)) {
            finalUsername = com.example.demo.util.DepartmentNormalizer.normalize(username);
            if (departmentRepository.findByNameIgnoreCase(finalUsername).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Department with this name already exists."));
            }
            String code = departmentCode != null ? departmentCode.trim().toUpperCase() : finalUsername.substring(0, Math.min(finalUsername.length(), 3)).toUpperCase();
            Department dept = new Department(finalUsername, code, encodedPassword);
            dept.setDescription("Workspace created by Achievement Add Account");
            departmentRepository.saveAndFlush(dept);
        }

        User user = new User(finalUsername, encodedPassword, role, name, email, contactNumber);
        User savedUser = userRepository.saveAndFlush(user);

        return ResponseEntity.ok(savedUser);
    }

    // ==========================================
    // 2. ACHIEVEMENT CRUD ENDPOINTS
    // ==========================================
    @PostMapping
    public ResponseEntity<?> createAchievement(@RequestBody Achievement achievement, Authentication authentication) {
        if (achievement.getStudentName() == null || achievement.getStudentName().trim().isEmpty() ||
            achievement.getRegisterNumber() == null || achievement.getRegisterNumber().trim().isEmpty() ||
            achievement.getDepartment() == null || achievement.getDepartment().trim().isEmpty() ||
            achievement.getTutorName() == null || achievement.getTutorName().trim().isEmpty() ||
            achievement.getEventName() == null || achievement.getEventName().trim().isEmpty() ||
            achievement.getEventType() == null || achievement.getEventType().trim().isEmpty() ||
            achievement.getAchievement() == null || achievement.getAchievement().trim().isEmpty() ||
            achievement.getEventDate() == null ||
            achievement.getAcademicYear() == null || achievement.getAcademicYear().trim().isEmpty() ||
            achievement.getCompetition() == null || achievement.getCompetition().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "All fields, including Competition Category, are required."));
        }

        achievement.setStudentName(achievement.getStudentName().trim());
        achievement.setRegisterNumber(achievement.getRegisterNumber().trim().toUpperCase());
        achievement.setDepartment(com.example.demo.util.DepartmentNormalizer.normalize(achievement.getDepartment().trim()));
        achievement.setTutorName(achievement.getTutorName().trim());
        achievement.setEventName(achievement.getEventName().trim());
        achievement.setEventType(achievement.getEventType().trim().toUpperCase());
        achievement.setAchievement(achievement.getAchievement().trim());
        achievement.setAcademicYear(achievement.getAcademicYear().trim());
        achievement.setCompetition(achievement.getCompetition().trim());
        achievement.setCreatedByUsername(authentication.getName());

        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        if ("ROLE_DEPARTMENT".equals(role)) {
            String deptName = username;
            if (!com.example.demo.util.DepartmentNormalizer.normalize(achievement.getDepartment()).equalsIgnoreCase(com.example.demo.util.DepartmentNormalizer.normalize(deptName))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You can only manage achievements for your department (" + deptName + ")."));
            }
        } else if (!"ROLE_ADMIN".equals(role) && !"ROLE_SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access Denied"));
        }

        Achievement saved = achievementRepository.save(achievement);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<?> getAchievements(
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String department,
            Authentication authentication) {

        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        List<Achievement> achievements = new ArrayList<>();

        // Role-based scoping
        if ("ROLE_DEPARTMENT".equals(role)) {
            // Department: achievements belonging to their department
            String deptName = username;
            achievements = achievementRepository.findByDepartmentIgnoreCase(com.example.demo.util.DepartmentNormalizer.normalize(deptName));
        } else if ("ROLE_ADMIN".equals(role) || "ROLE_SUPER_ADMIN".equals(role)) {
            // Admin & Superadmin: all achievements
            achievements = achievementRepository.findAll();
        } else {
            // Student or other roles: Access Denied
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access Denied"));
        }

        // Apply filters
        final String fAcademicYear = academicYear != null ? academicYear.trim() : null;
        final String fEventType = eventType != null ? eventType.trim().toUpperCase() : null;
        final String fDepartment = department != null ? com.example.demo.util.DepartmentNormalizer.normalize(department.trim()) : null;

        LocalDate fFromDate = null;
        LocalDate fToDate = null;
        try {
            if (fromDate != null && !fromDate.trim().isEmpty()) {
                fFromDate = LocalDate.parse(fromDate.trim());
            }
            if (toDate != null && !toDate.trim().isEmpty()) {
                fToDate = LocalDate.parse(toDate.trim());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid Date format. Use YYYY-MM-DD"));
        }

        final LocalDate finalFrom = fFromDate;
        final LocalDate finalTo = fToDate;

        List<Achievement> filtered = achievements.stream()
                .filter(a -> fAcademicYear == null || fAcademicYear.isEmpty() || (a.getAcademicYear() != null && a.getAcademicYear().equalsIgnoreCase(fAcademicYear)))
                .filter(a -> fEventType == null || fEventType.isEmpty() || (a.getEventType() != null && a.getEventType().equalsIgnoreCase(fEventType)))
                .filter(a -> fDepartment == null || fDepartment.isEmpty() || (a.getDepartment() != null && a.getDepartment().equalsIgnoreCase(fDepartment)))
                .filter(a -> finalFrom == null || (a.getEventDate() != null && !a.getEventDate().isBefore(finalFrom)))
                .filter(a -> finalTo == null || (a.getEventDate() != null && !a.getEventDate().isAfter(finalTo)))
                .sorted((a1, a2) -> {
                    if (a1.getEventDate() == null && a2.getEventDate() == null) return 0;
                    if (a1.getEventDate() == null) return 1;
                    if (a2.getEventDate() == null) return -1;
                    return a2.getEventDate().compareTo(a1.getEventDate());
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAchievementById(@PathVariable Long id) {
        return achievementRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAchievement(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        return achievementRepository.findById(id).map(a -> {
            boolean authorized = false;

            if ("ROLE_SUPER_ADMIN".equals(role) || "ROLE_ADMIN".equals(role)) {
                authorized = true;
            } else if ("ROLE_DEPARTMENT".equals(role)) {
                String deptName = username;
                if (com.example.demo.util.DepartmentNormalizer.normalize(a.getDepartment()).equalsIgnoreCase(com.example.demo.util.DepartmentNormalizer.normalize(deptName))) {
                    authorized = true;
                }
            }

            if (authorized) {
                achievementRepository.delete(a);
                return ResponseEntity.ok(Map.of("success", true));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access Denied"));
            }
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Achievement not found")));
    }

    // ==========================================
    // 3. EDIT ACHIEVEMENT ENDPOINT
    // ==========================================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAchievement(@PathVariable Long id, @RequestBody Achievement payload, Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        String username = authentication.getName();

        return achievementRepository.findById(id).map(existing -> {
            boolean authorized = false;
            if ("ROLE_SUPER_ADMIN".equals(role) || "ROLE_ADMIN".equals(role)) {
                authorized = true;
            } else if ("ROLE_DEPARTMENT".equals(role)) {
                String deptName = username;
                if (com.example.demo.util.DepartmentNormalizer.normalize(existing.getDepartment()).equalsIgnoreCase(com.example.demo.util.DepartmentNormalizer.normalize(deptName))) {
                    authorized = true;
                }
            }

            if (!authorized) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access Denied"));
            }

            existing.setStudentName(payload.getStudentName());
            existing.setRegisterNumber(payload.getRegisterNumber());
            existing.setDepartment(com.example.demo.util.DepartmentNormalizer.normalize(payload.getDepartment()));
            existing.setTutorName(payload.getTutorName());
            existing.setEventName(payload.getEventName());
            existing.setCompetition(payload.getCompetition());
            existing.setEventType(payload.getEventType());
            existing.setAchievement(payload.getAchievement());
            existing.setEventDate(payload.getEventDate());
            existing.setAcademicYear(payload.getAcademicYear());

            return ResponseEntity.ok(achievementRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }
}
