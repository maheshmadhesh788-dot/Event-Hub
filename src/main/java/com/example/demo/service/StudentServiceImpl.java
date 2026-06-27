package com.example.demo.service;

import com.example.demo.model.Student;
import com.example.demo.model.Registration;
import com.example.demo.model.User;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("null")
@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public StudentServiceImpl(StudentRepository studentRepository,
                              RegistrationRepository registrationRepository,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              EmailService emailService) {
        this.studentRepository = studentRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public Optional<Student> findByRollNumber(String rollNumber) {
        return studentRepository.findByRollNumberIgnoreCase(rollNumber.trim());
    }

    @Override
    public boolean existsByRollNumber(String rollNumber) {
        return studentRepository.findByRollNumberIgnoreCase(rollNumber.trim()).isPresent();
    }

    @Override
    @Transactional
    public Student registerStudentProfile(Student student) {
        student.setRollNumber(student.getRollNumber().trim().toUpperCase());
        student.setStudentName(student.getStudentName().trim());
        student.setDepartment(student.getDepartment().trim());
        student.setContactNumber(student.getContactNumber().trim());
        student.setEmail(student.getEmail().trim());
        student.setPassword(passwordEncoder.encode(student.getPassword().trim()));
        Student saved = studentRepository.saveAndFlush(student);

        // Sync to users table
        User user = new User(saved.getRollNumber(), saved.getPassword(), "ROLE_STUDENT", saved.getStudentName(), saved.getEmail(), saved.getContactNumber());
        userRepository.saveAndFlush(user);

        try {
            emailService.sendStudentAccountCreated(saved);
        } catch (Exception e) {
            // Log or ignore
        }

        return saved;
    }

    @Override
    @Transactional
    public Student updateStudentProfile(String rollNumber, Student updatedStudentData) {
        return studentRepository.findByRollNumberIgnoreCase(rollNumber.trim()).map(student -> {
            String oldRollNumber = student.getRollNumber();
            if (updatedStudentData.getStudentName() != null) {
                student.setStudentName(updatedStudentData.getStudentName().trim());
            }
            if (updatedStudentData.getDepartment() != null) {
                student.setDepartment(updatedStudentData.getDepartment().trim());
            }
            if (updatedStudentData.getContactNumber() != null) {
                student.setContactNumber(updatedStudentData.getContactNumber().trim());
            }
            if (updatedStudentData.getEmail() != null) {
                student.setEmail(updatedStudentData.getEmail().trim());
            }
            if (updatedStudentData.getPassword() != null && !updatedStudentData.getPassword().trim().isEmpty()) {
                student.setPassword(passwordEncoder.encode(updatedStudentData.getPassword().trim()));
            }
            Student saved = studentRepository.saveAndFlush(student);

            // Sync to users table
            userRepository.findByUsernameIgnoreCase(oldRollNumber).ifPresentOrElse(user -> {
                user.setUsername(saved.getRollNumber());
                user.setPassword(saved.getPassword());
                user.setName(saved.getStudentName());
                user.setEmail(saved.getEmail());
                user.setContactNumber(saved.getContactNumber());
                userRepository.saveAndFlush(user);
            }, () -> {
                User user = new User(saved.getRollNumber(), saved.getPassword(), "ROLE_STUDENT", saved.getStudentName(), saved.getEmail(), saved.getContactNumber());
                userRepository.saveAndFlush(user);
            });

            return saved;
        }).orElseThrow(() -> new RuntimeException("Student not found with roll number: " + rollNumber));
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteStudent(String rollNumber) {
        studentRepository.findByRollNumberIgnoreCase(rollNumber.trim()).ifPresent(student -> {
            // Delete student registrations first (Cascade)
            List<Registration> registrations = registrationRepository.findByStudentRollNumberIgnoreCase(student.getRollNumber());
            registrationRepository.deleteAll(registrations);
            studentRepository.delete(student);

            // Sync to users table (remove user record)
            userRepository.findByUsernameIgnoreCase(student.getRollNumber()).ifPresent(userRepository::delete);
        });
    }

    @Override
    public List<Registration> getRegistrationHistory(String rollNumber) {
        return registrationRepository.findByStudentRollNumberIgnoreCase(rollNumber.trim());
    }
}
