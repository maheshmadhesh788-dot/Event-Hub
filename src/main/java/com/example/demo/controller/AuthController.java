package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.model.Department;
import com.example.demo.model.Student;
import com.example.demo.model.User;
import com.example.demo.model.StaffProfile;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.StaffProfileRepository;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("null")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${eventhub.admin.username:admin}")
    private String adminUsername;

    @Value("${eventhub.admin.password:admin123}")
    private String adminPassword;

    public AuthController(StudentRepository studentRepository,
                          DepartmentRepository departmentRepository,
                          UserRepository userRepository,
                          StaffProfileRepository staffProfileRepository,
                          JwtTokenProvider tokenProvider,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // --- STUDENT REGISTRATION ---
    @PostMapping("/student/register")
    public ResponseEntity<?> registerStudent(@RequestBody Map<String, String> payload) {
        String studentName = payload.get("studentName");
        String rollNumber = payload.get("rollNumber");
        String department = payload.get("department");
        String contactNumber = payload.get("contactNumber");
        String email = payload.get("email");
        String password = payload.get("password");

        if (studentName == null || studentName.trim().isEmpty() ||
            rollNumber == null || rollNumber.trim().isEmpty() ||
            department == null || department.trim().isEmpty() ||
            contactNumber == null || contactNumber.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            
            return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));
        }

        try {
            com.example.demo.util.StudentValidator.validateStudentRegistration(
                studentName, rollNumber, department, departmentRepository, studentRepository
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        Student newStudent = new Student();
        newStudent.setRollNumber(rollNumber.trim().toUpperCase());
        newStudent.setStudentName(studentName.trim());
        newStudent.setDepartment(department.trim());
        newStudent.setContactNumber(contactNumber.trim());
        newStudent.setEmail(email.trim());
        newStudent.setPassword(passwordEncoder.encode(password.trim()));
        
        Student saved = studentRepository.save(newStudent);

        // Sync to users table
        User user = new User(saved.getRollNumber(), saved.getPassword(), "ROLE_STUDENT", saved.getStudentName(), saved.getEmail(), saved.getContactNumber());
        userRepository.save(user);

        try {
            emailService.sendStudentAccountCreated(saved);
        } catch (Exception e) {
            // Log or ignore to prevent blocking registration
        }

        return ResponseEntity.ok(saved);
    }

    // --- STUDENT LOGIN ---
    @PostMapping("/student/login")
    public ResponseEntity<?> loginStudent(@RequestBody Map<String, String> payload) {
        String rollNumber = payload.get("rollNumber");
        String password = payload.get("password");

        if (rollNumber == null || password == null || rollNumber.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Roll number and password are required"));
        }

        return studentRepository.findByRollNumberIgnoreCase(rollNumber.trim())
            .map(student -> {
                if (passwordEncoder.matches(password.trim(), student.getPassword())) {
                    String token = tokenProvider.generateToken(student.getRollNumber(), "ROLE_STUDENT");
                    AuthResponse resp = new AuthResponse(token, student.getRollNumber(), "ROLE_STUDENT", student.getStudentName());
                    resp.setRollNumber(student.getRollNumber());
                    resp.setStudentName(student.getStudentName());
                    resp.setDepartment(student.getDepartment());
                    resp.setContactNumber(student.getContactNumber());
                    resp.setEmail(student.getEmail());
                    return ResponseEntity.ok(resp);
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Wrong Password"));
                }
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "The student roll number does not exist")));
    }

    // --- DEPARTMENT REGISTRATION ---
    @PostMapping("/department/register")
    public ResponseEntity<?> registerDepartment(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String password = payload.get("password");

        if (name == null || password == null || name.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Department name and password are required"));
        }

        String normalizedName = com.example.demo.util.DepartmentNormalizer.normalize(name.trim());
        if (departmentRepository.findByNameIgnoreCase(normalizedName).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Department workspace already exists"));
        }

        String code = generateCode(normalizedName);
        int count = 1;
        String tempCode = code;
        while (departmentRepository.findByCode(tempCode).isPresent()) {
            tempCode = code + count++;
        }
        code = tempCode;

        Department dept = new Department(normalizedName, code, passwordEncoder.encode(password.trim()));
        Department saved = departmentRepository.save(dept);

        // Sync to users table
        User user = new User(saved.getName(), saved.getPassword(), "ROLE_DEPARTMENT", saved.getName());
        userRepository.save(user);

        return ResponseEntity.ok(saved);
    }

    private String generateCode(String name) {
        String[] words = name.trim().split("\\s+");
        if (words.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    sb.append(Character.toUpperCase(word.charAt(0)));
                }
            }
            return sb.toString();
        } else {
            String w = name.trim().toUpperCase();
            return w.length() > 3 ? w.substring(0, 3) : w;
        }
    }

    // --- DEPARTMENT LOGIN ---
    @PostMapping("/department/login")
    public ResponseEntity<?> loginDepartment(@RequestBody Map<String, String> credentials) {
        String name = credentials.get("name");
        String password = credentials.get("password");

        if (name == null || password == null || name.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Department name and password are required"));
        }

        String trimmedName = name.trim();
        String lookupName = trimmedName;
        Optional<Department> deptByCodeOpt = departmentRepository.findByCode(trimmedName.toUpperCase());
        if (deptByCodeOpt.isPresent()) {
            lookupName = deptByCodeOpt.get().getName();
        } else {
            lookupName = com.example.demo.util.DepartmentNormalizer.normalize(trimmedName);
        }

        Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(lookupName);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password.trim(), user.getPassword())) {
                if ("ROLE_DEPARTMENT".equals(user.getRole())) {
                    Optional<ResponseEntity<?>> responseOpt = departmentRepository.findByNameIgnoreCase(user.getUsername())
                        .map(dept -> {
                            String token = tokenProvider.generateToken(dept.getName(), "ROLE_DEPARTMENT");
                            AuthResponse resp = new AuthResponse(token, dept.getName(), "ROLE_DEPARTMENT", dept.getName());
                            resp.setId(dept.getId());
                            resp.setCode(dept.getCode());
                            resp.setDescription(dept.getDescription());
                            resp.setLogoUrl(dept.getLogoUrl());
                            resp.setCoverImageUrl(dept.getCoverImageUrl());
                            resp.setHodName(dept.getHodName());
                            resp.setEmail(dept.getEmail());
                            resp.setContactNumber(dept.getContactNumber());
                            resp.setPassword(dept.getPassword());
                            ResponseEntity<?> okResponse = ResponseEntity.ok(resp);
                            return okResponse;
                        });
                    return responseOpt.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "The department profile does not exist.")));
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access Denied: Only Department role can login to workspace"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Wrong Password"));
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "The department does not exist."));
    }

    // --- ADMIN LOGIN ---
    @PostMapping("/admin/login")
    public ResponseEntity<?> loginAdmin(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(username.trim());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if ("ROLE_ADMIN".equals(user.getRole())) {
                if (passwordEncoder.matches(password.trim(), user.getPassword())) {
                    String token = tokenProvider.generateToken(user.getUsername(), "ROLE_ADMIN");
                    return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), "ROLE_ADMIN", user.getName()));
                }
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid Admin Credentials"));
    }

    // --- UNIFIED LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username/Roll number and password are required"));
        }

        String trimmedUser = username.trim();
        String trimmedPass = password.trim();

        String lookupUsername = trimmedUser;
        // Resolve department code to department name if needed
        Optional<Department> deptByCodeOpt = departmentRepository.findByCode(trimmedUser.toUpperCase());
        if (deptByCodeOpt.isPresent()) {
            lookupUsername = deptByCodeOpt.get().getName();
        } else {
            lookupUsername = com.example.demo.util.DepartmentNormalizer.normalize(trimmedUser);
        }

        Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(lookupUsername);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if ("ROLE_TUTOR".equals(user.getRole()) || "ROLE_HOD".equals(user.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access Denied: Tutor and HOD roles are deprecated. Use Department login instead."));
            }
            if (passwordEncoder.matches(trimmedPass, user.getPassword())) {
                String token = tokenProvider.generateToken(user.getUsername(), user.getRole());
                AuthResponse resp = new AuthResponse(token, user.getUsername(), user.getRole(), user.getName());

                // Populate custom fields for compatibility with frontend expectations
                if ("ROLE_STUDENT".equals(user.getRole())) {
                    studentRepository.findByRollNumberIgnoreCase(user.getUsername()).ifPresent(student -> {
                        resp.setRollNumber(student.getRollNumber());
                        resp.setStudentName(student.getStudentName());
                        resp.setDepartment(student.getDepartment());
                        resp.setContactNumber(student.getContactNumber());
                        resp.setEmail(student.getEmail());
                    });
                } else if ("ROLE_DEPARTMENT".equals(user.getRole())) {
                    departmentRepository.findByNameIgnoreCase(user.getUsername()).ifPresent(dept -> {
                        resp.setId(dept.getId());
                        resp.setCode(dept.getCode());
                        resp.setDescription(dept.getDescription());
                        resp.setLogoUrl(dept.getLogoUrl());
                        resp.setCoverImageUrl(dept.getCoverImageUrl());
                    });
                }
                return ResponseEntity.ok(resp);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Wrong Password"));
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "The account does not exist"));
    }

    @GetMapping("/search/suggestions")
    public ResponseEntity<?> getSuggestions(@RequestParam(required = false, defaultValue = "") String q) {
        String query = q.trim().toLowerCase();
        List<Map<String, String>> suggestions = new ArrayList<>();
        if (query.isEmpty()) {
            return ResponseEntity.ok(suggestions);
        }

        // Search departments
        List<Department> departments = departmentRepository.findAll();
        for (Department dept : departments) {
            String name = dept.getName();
            String code = dept.getCode();
            if (name.toLowerCase().contains(query) || (code != null && code.toLowerCase().contains(query))) {
                Map<String, String> sug = new HashMap<>();
                sug.put("type", "DEPARTMENT");
                sug.put("code", code);
                sug.put("name", name);
                sug.put("display", name + " (" + code + ")");
                if (name.equalsIgnoreCase("B.Sc Artificial Intelligence & Data Science")) {
                    sug.put("value", code);
                } else {
                    sug.put("value", name);
                }
                suggestions.add(sug);
            }
        }

        // Search students
        List<Student> students = studentRepository.findAll();
        for (Student student : students) {
            String name = student.getStudentName();
            String roll = student.getRollNumber();
            if (name.toLowerCase().contains(query) || (roll != null && roll.toLowerCase().contains(query))) {
                Map<String, String> sug = new HashMap<>();
                sug.put("type", "STUDENT");
                sug.put("code", roll);
                sug.put("name", name);
                sug.put("display", name + " (" + roll + ")");
                sug.put("value", roll);
                suggestions.add(sug);
            }
        }

        // Limit results to 10
        if (suggestions.size() > 10) {
            return ResponseEntity.ok(suggestions.subList(0, 10));
        }

        return ResponseEntity.ok(suggestions);
    }
}
