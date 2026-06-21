package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.model.Department;
import com.example.demo.model.Student;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@SuppressWarnings("null")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${eventhub.admin.username:admin}")
    private String adminUsername;

    @Value("${eventhub.admin.password:admin123}")
    private String adminPassword;

    public AuthController(StudentRepository studentRepository,
                          DepartmentRepository departmentRepository,
                          JwtTokenProvider tokenProvider,
                          PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
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

        Optional<Student> studentOpt = studentRepository.findByRollNumberIgnoreCase(rollNumber.trim());
        if (studentOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "A student with this roll number is already registered."));
        }

        Student newStudent = new Student();
        newStudent.setRollNumber(rollNumber.trim().toUpperCase());
        newStudent.setStudentName(studentName.trim());
        newStudent.setDepartment(department.trim());
        newStudent.setContactNumber(contactNumber.trim());
        newStudent.setEmail(email.trim());
        newStudent.setPassword(passwordEncoder.encode(password.trim()));
        
        Student saved = studentRepository.save(newStudent);
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

        if (departmentRepository.findByNameIgnoreCase(name.trim()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Department workspace already exists"));
        }

        String code = generateCode(name);
        int count = 1;
        String tempCode = code;
        while (departmentRepository.findByCode(tempCode).isPresent()) {
            tempCode = code + count++;
        }
        code = tempCode;

        Department dept = new Department(name.trim(), code, passwordEncoder.encode(password.trim()));
        Department saved = departmentRepository.save(dept);
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

        return departmentRepository.findByNameIgnoreCase(name.trim())
            .map(dept -> {
                if (passwordEncoder.matches(password.trim(), dept.getPassword())) {
                    String token = tokenProvider.generateToken(dept.getName(), "ROLE_DEPARTMENT");
                    AuthResponse resp = new AuthResponse(token, dept.getName(), "ROLE_DEPARTMENT", dept.getName());
                    resp.setId(dept.getId());
                    resp.setCode(dept.getCode());
                    resp.setDescription(dept.getDescription());
                    resp.setLogoUrl(dept.getLogoUrl());
                    resp.setCoverImageUrl(dept.getCoverImageUrl());
                    return ResponseEntity.ok(resp);
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Wrong Password"));
                }
            })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "The department does not exist.")));
    }

    // --- ADMIN LOGIN ---
    @PostMapping("/admin/login")
    public ResponseEntity<?> loginAdmin(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        if (adminUsername.equals(username.trim()) && adminPassword.equals(password.trim())) {
            String token = tokenProvider.generateToken(username.trim(), "ROLE_ADMIN");
            return ResponseEntity.ok(new AuthResponse(token, username.trim(), "ROLE_ADMIN", "College Admin"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid Admin Credentials"));
        }
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

        // 1. Check Admin
        if (adminUsername.equals(trimmedUser) && adminPassword.equals(trimmedPass)) {
            String token = tokenProvider.generateToken(trimmedUser, "ROLE_ADMIN");
            return ResponseEntity.ok(new AuthResponse(token, trimmedUser, "ROLE_ADMIN", "College Admin"));
        }

        // 2. Check Department (by name or code)
        Optional<Department> deptOpt = departmentRepository.findByNameIgnoreCase(trimmedUser);
        if (deptOpt.isEmpty()) {
            deptOpt = departmentRepository.findByCode(trimmedUser.toUpperCase());
        }
        if (deptOpt.isPresent()) {
            Department dept = deptOpt.get();
            if (passwordEncoder.matches(trimmedPass, dept.getPassword())) {
                String token = tokenProvider.generateToken(dept.getName(), "ROLE_DEPARTMENT");
                AuthResponse resp = new AuthResponse(token, dept.getName(), "ROLE_DEPARTMENT", dept.getName());
                resp.setId(dept.getId());
                resp.setCode(dept.getCode());
                resp.setDescription(dept.getDescription());
                resp.setLogoUrl(dept.getLogoUrl());
                resp.setCoverImageUrl(dept.getCoverImageUrl());
                return ResponseEntity.ok(resp);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Wrong password for department"));
            }
        }

        // 3. Check Student (by roll number)
        Optional<Student> studentOpt = studentRepository.findByRollNumberIgnoreCase(trimmedUser);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            if (passwordEncoder.matches(trimmedPass, student.getPassword())) {
                String token = tokenProvider.generateToken(student.getRollNumber(), "ROLE_STUDENT");
                AuthResponse resp = new AuthResponse(token, student.getRollNumber(), "ROLE_STUDENT", student.getStudentName());
                resp.setRollNumber(student.getRollNumber());
                resp.setStudentName(student.getStudentName());
                resp.setDepartment(student.getDepartment());
                resp.setContactNumber(student.getContactNumber());
                resp.setEmail(student.getEmail());
                return ResponseEntity.ok(resp);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Wrong password for student"));
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "The account does not exist"));
    }
}
