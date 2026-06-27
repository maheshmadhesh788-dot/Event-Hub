package com.example.demo.controller;

import com.example.demo.model.Achievement;
import com.example.demo.model.TutorStudentAssignment;
import com.example.demo.model.StaffProfile;
import com.example.demo.model.Student;
import com.example.demo.repository.AchievementRepository;
import com.example.demo.repository.TutorStudentAssignmentRepository;
import com.example.demo.repository.StaffProfileRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("null")
@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    private final AchievementRepository achievementRepository;
    private final TutorStudentAssignmentRepository assignmentRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final StudentRepository studentRepository;

    public AchievementController(AchievementRepository achievementRepository,
                                 TutorStudentAssignmentRepository assignmentRepository,
                                 StaffProfileRepository staffProfileRepository,
                                 StudentRepository studentRepository) {
        this.achievementRepository = achievementRepository;
        this.assignmentRepository = assignmentRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.studentRepository = studentRepository;
    }

    // ==========================================
    // 1. AUTO-FILL STUDENT LOOKUP
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
            achievement.getAcademicYear() == null || achievement.getAcademicYear().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "All fields are required."));
        }

        achievement.setStudentName(achievement.getStudentName().trim());
        achievement.setRegisterNumber(achievement.getRegisterNumber().trim().toUpperCase());
        achievement.setDepartment(achievement.getDepartment().trim());
        achievement.setTutorName(achievement.getTutorName().trim());
        achievement.setEventName(achievement.getEventName().trim());
        achievement.setEventType(achievement.getEventType().trim().toUpperCase());
        achievement.setAchievement(achievement.getAchievement().trim());
        achievement.setAcademicYear(achievement.getAcademicYear().trim());
        achievement.setCreatedByUsername(authentication.getName());

        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        if ("ROLE_TUTOR".equals(role)) {
            Optional<StaffProfile> profileOpt = staffProfileRepository.findByUsernameIgnoreCase(username);
            if (profileOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Tutor profile not found."));
            }
            String tutorDept = profileOpt.get().getDepartment();
            if (!achievement.getDepartment().equalsIgnoreCase(tutorDept)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You can only manage achievements for your department (" + tutorDept + ")."));
            }
            boolean assigned = assignmentRepository.existsByTutorUsernameIgnoreCaseAndStudentRegisterNumberIgnoreCase(username, achievement.getRegisterNumber());
            if (!assigned) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You can only manage achievements for your assigned students."));
            }
        } else if ("ROLE_HOD".equals(role) || "ROLE_DEPARTMENT".equals(role)) {
            String deptName = username;
            Optional<StaffProfile> profileOpt = staffProfileRepository.findByUsernameIgnoreCase(username);
            if (profileOpt.isPresent()) {
                deptName = profileOpt.get().getDepartment();
            }
            if (!achievement.getDepartment().equalsIgnoreCase(deptName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You can only manage achievements for your department (" + deptName + ")."));
            }
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
        if ("ROLE_TUTOR".equals(role)) {
            // Tutor: Only their assigned students' achievements, OR achievements they created
            List<TutorStudentAssignment> assignments = assignmentRepository.findByTutorUsernameIgnoreCase(username);
            List<String> assignedRegs = assignments.stream()
                    .map(a -> a.getStudentRegisterNumber().toUpperCase())
                    .collect(Collectors.toList());

            if (assignedRegs.isEmpty()) {
                achievements = achievementRepository.findByCreatedByUsername(username);
            } else {
                // Find by assigned register numbers
                List<Achievement> byAssigned = achievementRepository.findByRegisterNumberIn(assignedRegs);
                // Find by creator
                List<Achievement> byCreator = achievementRepository.findByCreatedByUsername(username);
                // Combine and de-duplicate
                Set<Long> seenIds = new HashSet<>();
                for (Achievement a : byAssigned) {
                    achievements.add(a);
                    seenIds.add(a.getId());
                }
                for (Achievement a : byCreator) {
                    if (!seenIds.contains(a.getId())) {
                        achievements.add(a);
                    }
                }
            }
        } else if ("ROLE_HOD".equals(role) || "ROLE_DEPARTMENT".equals(role)) {
            // HOD / Department: achievements belonging to their department
            String deptName = username;
            Optional<StaffProfile> profileOpt = staffProfileRepository.findByUsernameIgnoreCase(username);
            if (profileOpt.isPresent()) {
                deptName = profileOpt.get().getDepartment();
            }
            achievements = achievementRepository.findByDepartmentIgnoreCase(deptName);
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
        final String fDepartment = department != null ? department.trim() : null;

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
                .filter(a -> fAcademicYear == null || fAcademicYear.isEmpty() || a.getAcademicYear().equalsIgnoreCase(fAcademicYear))
                .filter(a -> fEventType == null || fEventType.isEmpty() || a.getEventType().equalsIgnoreCase(fEventType))
                .filter(a -> fDepartment == null || fDepartment.isEmpty() || a.getDepartment().equalsIgnoreCase(fDepartment))
                .filter(a -> finalFrom == null || !a.getEventDate().isBefore(finalFrom))
                .filter(a -> finalTo == null || !a.getEventDate().isAfter(finalTo))
                .sorted((a1, a2) -> a2.getEventDate().compareTo(a1.getEventDate())) // Date-wise sorted by default
                .collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAchievement(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        return achievementRepository.findById(id).map(a -> {
            boolean authorized = false;

            if ("ROLE_SUPER_ADMIN".equals(role) || "ROLE_ADMIN".equals(role)) {
                authorized = true;
            } else if ("ROLE_HOD".equals(role) || "ROLE_DEPARTMENT".equals(role)) {
                String deptName = username;
                Optional<StaffProfile> profileOpt = staffProfileRepository.findByUsernameIgnoreCase(username);
                if (profileOpt.isPresent()) {
                    deptName = profileOpt.get().getDepartment();
                }
                if (a.getDepartment().equalsIgnoreCase(deptName)) {
                    authorized = true;
                }
            } else if ("ROLE_TUTOR".equals(role)) {
                if (a.getCreatedByUsername().equalsIgnoreCase(username) ||
                    assignmentRepository.existsByTutorUsernameIgnoreCaseAndStudentRegisterNumberIgnoreCase(username, a.getRegisterNumber())) {
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
    // 3. TUTOR-STUDENT ASSIGNMENTS ENDPOINTS
    // ==========================================
    @GetMapping("/assignments")
    public ResponseEntity<?> getAssignments(Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        List<TutorStudentAssignment> list;
        if ("ROLE_TUTOR".equals(role)) {
            list = assignmentRepository.findByTutorUsernameIgnoreCase(username);
        } else if ("ROLE_HOD".equals(role) || "ROLE_DEPARTMENT".equals(role)) {
            String deptName = username;
            Optional<StaffProfile> profileOpt = staffProfileRepository.findByUsernameIgnoreCase(username);
            if (profileOpt.isPresent()) {
                deptName = profileOpt.get().getDepartment();
            }
            final String finalDept = deptName;
            
            // Find all profiles in the same department
            List<String> usernames = staffProfileRepository.findByDepartmentIgnoreCase(finalDept).stream()
                    .map(StaffProfile::getUsername)
                    .collect(Collectors.toList());
            
            // Also include department user accounts just in case
            usernames.add(username);
            
            list = assignmentRepository.findAll().stream()
                    .filter(a -> usernames.contains(a.getTutorUsername()))
                    .collect(Collectors.toList());
        } else {
            // Admin and Super Admin see all
            list = assignmentRepository.findAll();
        }

        // Map list to DTO with student name for convenience
        List<Map<String, Object>> responseList = new ArrayList<>();
        for (TutorStudentAssignment assign : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", assign.getId());
            map.put("tutorUsername", assign.getTutorUsername());
            map.put("studentRegisterNumber", assign.getStudentRegisterNumber());
            
            // Look up student details
            studentRepository.findByRollNumberIgnoreCase(assign.getStudentRegisterNumber())
                    .ifPresentOrElse(student -> {
                        map.put("studentName", student.getStudentName());
                        map.put("department", student.getDepartment());
                    }, () -> {
                        map.put("studentName", "Unknown Student");
                        map.put("department", "Unknown");
                    });
            responseList.add(map);
        }

        return ResponseEntity.ok(responseList);
    }

    @PostMapping("/assignments")
    public ResponseEntity<?> createAssignment(@RequestBody Map<String, String> payload, Authentication authentication) {
        String rollNumber = payload.get("studentRegisterNumber");
        String tutorUsername = payload.get("tutorUsername");

        String loggedInUser = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Student register number is required."));
        }

        String studentReg = rollNumber.trim().toUpperCase();

        if (tutorUsername == null || tutorUsername.trim().isEmpty()) {
            tutorUsername = loggedInUser;
        } else {
            tutorUsername = tutorUsername.trim();
        }

        // Access check
        if ("ROLE_TUTOR".equals(role)) {
            // Tutor can only self-assign
            tutorUsername = loggedInUser;
        }

        if (assignmentRepository.existsByTutorUsernameIgnoreCaseAndStudentRegisterNumberIgnoreCase(tutorUsername, studentReg)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Assignment already exists."));
        }

        // Fetch tutor profile to check department
        Optional<StaffProfile> tutorProfileOpt = staffProfileRepository.findByUsernameIgnoreCase(tutorUsername);
        if (tutorProfileOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Tutor profile not found."));
        }
        StaffProfile tutorProfile = tutorProfileOpt.get();

        if ("ROLE_HOD".equals(role) || "ROLE_DEPARTMENT".equals(role)) {
            String deptName = loggedInUser;
            Optional<StaffProfile> profileOpt = staffProfileRepository.findByUsernameIgnoreCase(loggedInUser);
            if (profileOpt.isPresent()) {
                deptName = profileOpt.get().getDepartment();
            }
            if (!tutorProfile.getDepartment().equalsIgnoreCase(deptName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Tutor does not belong to your department (" + deptName + ")."));
            }
            Optional<Student> studentOpt = studentRepository.findByRollNumberIgnoreCase(studentReg);
            if (studentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Student not found."));
            }
            if (!studentOpt.get().getDepartment().equalsIgnoreCase(deptName)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Student does not belong to your department (" + deptName + ")."));
            }
        } else if ("ROLE_TUTOR".equals(role)) {
            Optional<Student> studentOpt = studentRepository.findByRollNumberIgnoreCase(studentReg);
            if (studentOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Student not found."));
            }
            if (!studentOpt.get().getDepartment().equalsIgnoreCase(tutorProfile.getDepartment())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Student does not belong to your department (" + tutorProfile.getDepartment() + ")."));
            }
        }

        TutorStudentAssignment assignment = new TutorStudentAssignment(tutorUsername, studentReg);
        TutorStudentAssignment saved = assignmentRepository.save(assignment);
        
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/assignments/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        return assignmentRepository.findById(id).map(assign -> {
            boolean authorized = false;

            if ("ROLE_SUPER_ADMIN".equals(role) || "ROLE_ADMIN".equals(role)) {
                authorized = true;
            } else if ("ROLE_TUTOR".equals(role) && assign.getTutorUsername().equalsIgnoreCase(username)) {
                authorized = true;
            } else if ("ROLE_HOD".equals(role) || "ROLE_DEPARTMENT".equals(role)) {
                // If the tutor belongs to HOD's department
                String deptName = username;
                Optional<StaffProfile> profileOpt = staffProfileRepository.findByUsernameIgnoreCase(username);
                if (profileOpt.isPresent()) {
                    deptName = profileOpt.get().getDepartment();
                }
                
                final String finalDept = deptName;
                Optional<StaffProfile> tutorProfileOpt = staffProfileRepository.findByUsernameIgnoreCase(assign.getTutorUsername());
                if (tutorProfileOpt.isPresent() && tutorProfileOpt.get().getDepartment().equalsIgnoreCase(finalDept)) {
                    authorized = true;
                }
            }

            if (authorized) {
                assignmentRepository.delete(assign);
                return ResponseEntity.ok(Map.of("success", true));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access Denied"));
            }
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Assignment not found")));
    }
}
