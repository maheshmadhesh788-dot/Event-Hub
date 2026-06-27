package com.example.demo.service;

import com.example.demo.model.Registration;
import com.example.demo.model.Event;
import com.example.demo.model.Competition;
import com.example.demo.model.Student;
import com.example.demo.model.User;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.CompetitionRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("null")
@Service
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final CompetitionRepository competitionRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public RegistrationServiceImpl(RegistrationRepository registrationRepository,
                                   EventRepository eventRepository,
                                   CompetitionRepository competitionRepository,
                                   StudentRepository studentRepository,
                                   UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   EmailService emailService) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.competitionRepository = competitionRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public Registration registerForEvent(Registration regData, List<Long> competitionIds) {
        String rollNumber = regData.getRollNumber().trim().toUpperCase();
        
        // 1. Fetch Event
        Event event = eventRepository.findById(regData.getEvent().getId())
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + regData.getEvent().getId()));

        // Event capacity limit check (distinct roll numbers)
        if (event.getCapacity() != null && event.getCapacity() > 0) {
            long distinctStudents = registrationRepository.findByEventId(event.getId()).stream()
                    .map(r -> r.getRollNumber().toLowerCase())
                    .distinct()
                    .count();
            if (distinctStudents >= event.getCapacity()) {
                throw new RuntimeException("Sorry, this event has reached its maximum capacity of " + event.getCapacity() + " participants.");
            }
        }

        // 2. Validate/Create Student profile
        Optional<Student> studentOpt = studentRepository.findByRollNumberIgnoreCase(rollNumber);
        Student studentEntity;
        if (studentOpt.isEmpty()) {
            // Auto create student
            Student newStudent = new Student();
            newStudent.setRollNumber(rollNumber);
            newStudent.setStudentName(regData.getStudentName().trim());
            newStudent.setDepartment(regData.getDepartment().trim());
            newStudent.setContactNumber(regData.getContactNumber().trim());
            newStudent.setEmail(regData.getEmail().trim());
            newStudent.setYear(regData.getYear() != null ? regData.getYear().trim() : "1st");
            
            // Password fallback or default
            int atIndex = regData.getEmail().indexOf("@");
            String pwdPrefix = atIndex != -1 ? regData.getEmail().substring(0, Math.min(atIndex, 6)) : "stud";
            String pwd = pwdPrefix + "123";
            newStudent.setPassword(passwordEncoder.encode(pwd));
            studentEntity = studentRepository.save(newStudent);

            // Sync to users table
            User user = new User(studentEntity.getRollNumber(), studentEntity.getPassword(), "ROLE_STUDENT", studentEntity.getStudentName(), studentEntity.getEmail(), studentEntity.getContactNumber());
            userRepository.save(user);
        } else {
            studentEntity = studentOpt.get();
        }

        List<Registration> savedList = new ArrayList<>();

        if (competitionIds == null || competitionIds.isEmpty()) {
            // Event level registration duplicate check
            List<Registration> existingRegs = registrationRepository.findByStudentRollNumberIgnoreCase(rollNumber);
            boolean alreadyRegistered = existingRegs.stream()
                    .anyMatch(r -> r.getEvent().getId().equals(event.getId()) && r.getCompetition() == null);
            if (alreadyRegistered) {
                throw new RuntimeException("You have already registered for this event!");
            }

            Registration reg = new Registration();
            reg.setStudent(studentEntity);
            reg.setStudentName(regData.getStudentName().trim());
            reg.setRollNumber(rollNumber);
            reg.setDepartment(regData.getDepartment().trim());
            reg.setYear(regData.getYear().trim());
            reg.setContactNumber(regData.getContactNumber().trim());
            reg.setEmail(regData.getEmail().trim());
            reg.setEvent(event);
            reg.setCompetition(null);
            reg.setRegistrationDate(LocalDateTime.now());
            reg.setStatus("Successfully Registered");
            
            Registration saved = registrationRepository.save(reg);
            registrationRepository.flush();
            emailService.sendRegistrationConfirmation(saved);
            savedList.add(saved);
        } else {
            // Competition level registration check
            for (Long compId : competitionIds) {
                if (!registrationRepository.findByStudentRollNumberIgnoreCaseAndCompetitionId(rollNumber, compId).isEmpty()) {
                    throw new RuntimeException("You have already registered for one of the selected competitions.");
                }
            }

            for (Long compId : competitionIds) {
                Competition comp = competitionRepository.findById(compId)
                        .orElseThrow(() -> new RuntimeException("Competition not found with ID: " + compId));

                Registration reg = new Registration();
                reg.setStudent(studentEntity);
                reg.setStudentName(regData.getStudentName().trim());
                reg.setRollNumber(rollNumber);
                reg.setDepartment(regData.getDepartment().trim());
                reg.setYear(regData.getYear().trim());
                reg.setContactNumber(regData.getContactNumber().trim());
                reg.setEmail(regData.getEmail().trim());
                reg.setEvent(event);
                reg.setCompetition(comp);
                reg.setRegistrationDate(LocalDateTime.now());
                reg.setStatus("Successfully Registered");
                
                Registration saved = registrationRepository.save(reg);
                registrationRepository.flush();
                emailService.sendRegistrationConfirmation(saved);
                savedList.add(saved);
            }
        }

        return savedList.isEmpty() ? null : savedList.get(0);
    }

    @Override
    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    @Override
    public Optional<Registration> getRegistrationById(Long id) {
        return registrationRepository.findById(id);
    }

    @Override
    @Transactional
    public void deleteRegistration(Long id) {
        if (registrationRepository.existsById(id)) {
            registrationRepository.deleteById(id);
        } else {
            throw new RuntimeException("Registration record not found with ID: " + id);
        }
    }

    @Override
    public List<Registration> getRegistrationsByDepartmentId(Long deptId) {
        return registrationRepository.findByEvent_DepartmentId(deptId);
    }

    @Override
    public List<Registration> getRegistrationsByStudentRollNumber(String rollNumber) {
        return registrationRepository.findByStudentRollNumberIgnoreCase(rollNumber.trim());
    }

    @Override
    @Transactional
    public Registration checkInParticipant(Long id) {
        return registrationRepository.findById(id).map(reg -> {
            reg.setStatus("Checked In / Attended");
            return registrationRepository.save(reg);
        }).orElseThrow(() -> new RuntimeException("Registration not found for check-in with ID: " + id));
    }
}
