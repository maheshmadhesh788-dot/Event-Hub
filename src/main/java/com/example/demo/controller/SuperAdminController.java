package com.example.demo.controller;

import com.example.demo.model.Department;
import com.example.demo.model.Student;
import com.example.demo.model.User;
import com.example.demo.model.Event;
import com.example.demo.model.Registration;
import com.example.demo.model.Notification;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.StaffProfileRepository;
import com.example.demo.model.StaffProfile;
import com.example.demo.service.DepartmentService;
import com.example.demo.service.StudentService;
import com.example.demo.service.EventService;
import com.example.demo.service.RegistrationService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@SuppressWarnings("null")
@RestController
@RequestMapping("/api/superadmin")
public class SuperAdminController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final NotificationRepository notificationRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final StudentService studentService;
    private final DepartmentService departmentService;
    private final EventService eventService;
    private final RegistrationService registrationService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public SuperAdminController(UserRepository userRepository,
                                StudentRepository studentRepository,
                                DepartmentRepository departmentRepository,
                                NotificationRepository notificationRepository,
                                StaffProfileRepository staffProfileRepository,
                                StudentService studentService,
                                DepartmentService departmentService,
                                EventService eventService,
                                RegistrationService registrationService,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.notificationRepository = notificationRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.studentService = studentService;
        this.departmentService = departmentService;
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // ==========================================
    // 1. USER MANAGEMENT CRUD
    // ==========================================

    @GetMapping("/users")
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Student> students = studentRepository.findAll();
        List<Department> departments = departmentRepository.findAll();

        Map<String, Student> studentMap = new HashMap<>();
        for (Student s : students) {
            if (s.getRollNumber() != null) {
                studentMap.put(s.getRollNumber().trim().toLowerCase(), s);
            }
        }

        Map<String, Department> deptMap = new HashMap<>();
        for (Department d : departments) {
            if (d.getName() != null) {
                deptMap.put(d.getName().trim().toLowerCase(), d);
            }
        }

        for (User u : users) {
            if (u.getUsername() == null) continue;
            String usernameKey = u.getUsername().trim().toLowerCase();
            if ("ROLE_STUDENT".equalsIgnoreCase(u.getRole())) {
                Student s = studentMap.get(usernameKey);
                if (s != null) {
                    u.setName(s.getStudentName());
                    u.setDepartment(s.getDepartment());
                    if (u.getEmail() == null || u.getEmail().trim().isEmpty()) {
                        u.setEmail(s.getEmail());
                    }
                    if (u.getContactNumber() == null || u.getContactNumber().trim().isEmpty()) {
                        u.setContactNumber(s.getContactNumber());
                    }
                } else {
                    u.setName(u.getUsername());
                    u.setDepartment("IT");
                    if (u.getEmail() == null || u.getEmail().trim().isEmpty()) {
                        u.setEmail("student@kasc.ac.in");
                    }
                    if (u.getContactNumber() == null || u.getContactNumber().trim().isEmpty()) {
                        u.setContactNumber("N/A");
                    }
                }
            } else if ("ROLE_DEPARTMENT".equalsIgnoreCase(u.getRole())) {
                Department d = deptMap.get(usernameKey);
                if (d != null) {
                    u.setName(d.getName());
                    u.setDepartment(d.getName());
                    if (u.getContactNumber() == null || u.getContactNumber().trim().isEmpty()) {
                        u.setContactNumber(d.getCode());
                    }
                    if (u.getEmail() == null || u.getEmail().trim().isEmpty()) {
                        u.setEmail(d.getCode().toLowerCase() + "@kasc.ac.in");
                    }
                } else {
                    u.setName(u.getUsername());
                    u.setDepartment(u.getUsername());
                    if (u.getContactNumber() == null || u.getContactNumber().trim().isEmpty()) {
                        u.setContactNumber("N/A");
                    }
                    if (u.getEmail() == null || u.getEmail().trim().isEmpty()) {
                        u.setEmail(u.getUsername().toLowerCase().replaceAll("\\s+", "") + "@kasc.ac.in");
                    }
                }
            } else if ("ROLE_ADMIN".equalsIgnoreCase(u.getRole())) {
                u.setName("College Admin");
                u.setDepartment("All");
                if (u.getEmail() == null || u.getEmail().trim().isEmpty()) {
                    u.setEmail("admin@kasc.ac.in");
                }
                if (u.getContactNumber() == null || u.getContactNumber().trim().isEmpty()) {
                    u.setContactNumber("9876543210");
                }
            } else if ("ROLE_SUPER_ADMIN".equalsIgnoreCase(u.getRole())) {
                u.setName("Super Admin");
                u.setDepartment("All");
                if (u.getEmail() == null || u.getEmail().trim().isEmpty()) {
                    u.setEmail("superadmin@kasc.ac.in");
                }
                if (u.getContactNumber() == null || u.getContactNumber().trim().isEmpty()) {
                    u.setContactNumber("9876543211");
                }
            }
        }
        return users;
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");
        String role = payload.get("role"); // ROLE_ADMIN, ROLE_SUPER_ADMIN, ROLE_STUDENT, ROLE_DEPARTMENT
        String name = payload.get("name");
        String email = payload.get("email");
        String contactNumber = payload.get("contactNumber");
        String departmentCode = payload.get("departmentCode"); // only for department

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
            dept.setDescription("Workspace created by Super Admin");
            departmentRepository.saveAndFlush(dept);
        }

        User user = new User(finalUsername, encodedPassword, role, name, email, contactNumber);
        User savedUser = userRepository.saveAndFlush(user);

        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return userRepository.findById(id).map(user -> {
            String oldUsername = user.getUsername();
            String oldRole = user.getRole();

            String username = payload.get("username");
            String role = payload.get("role");
            String name = payload.get("name");
            String email = payload.get("email");
            String contactNumber = payload.get("contactNumber");
            String password = payload.get("password");

            if (username != null && !username.trim().isEmpty()) {
                if ("ROLE_DEPARTMENT".equals(role) || (role == null && "ROLE_DEPARTMENT".equals(user.getRole()))) {
                    user.setUsername(com.example.demo.util.DepartmentNormalizer.normalize(username.trim()));
                } else {
                    user.setUsername(username.trim());
                }
            }
            if (role != null && !role.trim().isEmpty()) {
                user.setRole(role.trim().toUpperCase());
            }
            if (name != null && !name.trim().isEmpty()) {
                user.setName(name.trim());
            }
            if (email != null) {
                user.setEmail(email.trim());
            }
            if (contactNumber != null) {
                user.setContactNumber(contactNumber.trim());
            }
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(password.trim()));
            }

            User savedUser = userRepository.saveAndFlush(user);

            // Sync to specific profile if role matches or changed
            if ("ROLE_STUDENT".equals(savedUser.getRole())) {
                studentRepository.findByRollNumberIgnoreCase(oldUsername).ifPresentOrElse(student -> {
                    student.setRollNumber(savedUser.getUsername().toUpperCase());
                    student.setStudentName(savedUser.getName());
                    student.setEmail(savedUser.getEmail());
                    student.setContactNumber(savedUser.getContactNumber());
                    student.setPassword(savedUser.getPassword());
                    studentRepository.saveAndFlush(student);
                }, () -> {
                    // Create if not exists
                    Student student = new Student(savedUser.getUsername().toUpperCase(), savedUser.getName(), "IT",
                            savedUser.getContactNumber(), savedUser.getEmail(), savedUser.getPassword());
                    Student savedStudent = studentRepository.saveAndFlush(student);
                    try {
                        emailService.sendStudentAccountCreated(savedStudent);
                    } catch (Exception e) {
                        // Log or ignore
                    }
                });
            } else if ("ROLE_DEPARTMENT".equals(savedUser.getRole())) {
                departmentRepository.findByNameIgnoreCase(oldUsername).ifPresentOrElse(dept -> {
                    dept.setName(savedUser.getUsername());
                    dept.setPassword(savedUser.getPassword());
                    departmentRepository.saveAndFlush(dept);
                }, () -> {
                    // Create if not exists
                    String code = savedUser.getUsername().substring(0, Math.min(savedUser.getUsername().length(), 3)).toUpperCase();
                    Department dept = new Department(savedUser.getUsername(), code, savedUser.getPassword());
                    departmentRepository.saveAndFlush(dept);
                });
            }

            // Cleanup old role if role changed
            if (!oldRole.equals(savedUser.getRole())) {
                if ("ROLE_STUDENT".equals(oldRole)) {
                    studentRepository.findByRollNumberIgnoreCase(oldUsername).ifPresent(studentRepository::delete);
                } else if ("ROLE_DEPARTMENT".equals(oldRole)) {
                    departmentRepository.findByNameIgnoreCase(oldUsername).ifPresent(departmentRepository::delete);
                }
            }

            return ResponseEntity.ok(savedUser);
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            String username = user.getUsername();
            String role = user.getRole();

            userRepository.delete(user);

            // Clean up role-specific profile
            if ("ROLE_STUDENT".equals(role)) {
                studentRepository.findByRollNumberIgnoreCase(username).ifPresent(studentRepository::delete);
            } else if ("ROLE_DEPARTMENT".equals(role)) {
                departmentRepository.findByNameIgnoreCase(username).ifPresent(departmentRepository::delete);
            }

            return ResponseEntity.ok().build();
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // ==========================================
    // 2. DETAILED ANALYTICS
    // ==========================================

    @GetMapping("/analytics")
    public ResponseEntity<?> getSuperAdminAnalytics() {
        List<User> users = userRepository.findAll();
        List<Event> events = eventService.getAllEvents();
        List<Registration> registrations = registrationService.getAllRegistrations();
        List<Student> students = studentService.getAllStudents();
        List<Department> departments = departmentService.getAllDepartments();
        List<Notification> notifications = notificationRepository.findAll();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", users.size());
        stats.put("totalEvents", events.size());
        stats.put("totalRegistrations", registrations.size());
        stats.put("totalStudents", students.size());
        stats.put("totalDepartments", departments.size());
        stats.put("totalNotifications", notifications.size());

        // Count users by role
        Map<String, Integer> usersByRole = new HashMap<>();
        for (User u : users) {
            usersByRole.put(u.getRole(), usersByRole.getOrDefault(u.getRole(), 0) + 1);
        }
        stats.put("usersByRole", usersByRole);

        // Count events by type
        Map<String, Integer> eventsByType = new HashMap<>();
        for (Event e : events) {
            eventsByType.put(e.getType(), eventsByType.getOrDefault(e.getType(), 0) + 1);
        }
        stats.put("eventsByType", eventsByType);

        // Registrations per Event
        Map<String, Integer> regPerEvent = new HashMap<>();
        for (Registration r : registrations) {
            String eventName = r.getEvent().getName();
            regPerEvent.put(eventName, regPerEvent.getOrDefault(eventName, 0) + 1);
        }
        stats.put("registrationsPerEvent", regPerEvent);

        return ResponseEntity.ok(stats);
    }
}
