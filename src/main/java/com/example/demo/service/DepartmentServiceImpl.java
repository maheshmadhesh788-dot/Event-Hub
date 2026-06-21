package com.example.demo.service;

import com.example.demo.model.Department;
import com.example.demo.model.Event;
import com.example.demo.model.Registration;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.RegistrationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("null")
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final PasswordEncoder passwordEncoder;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 EventRepository eventRepository,
                                 RegistrationRepository registrationRepository,
                                 PasswordEncoder passwordEncoder) {
        this.departmentRepository = departmentRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Override
    public Optional<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }

    @Override
    public Optional<Department> findByName(String name) {
        return departmentRepository.findByNameIgnoreCase(name.trim());
    }

    @Override
    public Optional<Department> findByCode(String code) {
        return departmentRepository.findByCode(code.trim().toUpperCase());
    }

    @Override
    @Transactional
    public Department createDepartment(Department dept) {
        dept.setName(dept.getName().trim());
        dept.setPassword(passwordEncoder.encode(dept.getPassword().trim()));
        dept.setCode(dept.getCode().trim().toUpperCase());
        return departmentRepository.save(dept);
    }

    @Override
    @Transactional
    public Department updateDepartment(Long id, Department updatedData) {
        return departmentRepository.findById(id).map(dept -> {
            if (updatedData.getName() != null && !updatedData.getName().trim().isEmpty()) {
                dept.setName(updatedData.getName().trim());
            }
            if (updatedData.getCode() != null && !updatedData.getCode().trim().isEmpty()) {
                dept.setCode(updatedData.getCode().trim().toUpperCase());
            }
            if (updatedData.getPassword() != null && !updatedData.getPassword().trim().isEmpty()) {
                // If it is already BCrypt hashed (typically 60 characters starting with $2a$), do not rehash it
                String rawPwd = updatedData.getPassword().trim();
                if (rawPwd.startsWith("$2a$") && rawPwd.length() == 60) {
                    dept.setPassword(rawPwd);
                } else {
                    dept.setPassword(passwordEncoder.encode(rawPwd));
                }
            }
            if (updatedData.getDescription() != null) {
                dept.setDescription(updatedData.getDescription());
            }
            if (updatedData.getLogoUrl() != null) {
                dept.setLogoUrl(updatedData.getLogoUrl());
            }
            if (updatedData.getCoverImageUrl() != null) {
                dept.setCoverImageUrl(updatedData.getCoverImageUrl());
            }
            return departmentRepository.save(dept);
        }).orElseThrow(() -> new RuntimeException("Department workspace not found with ID: " + id));
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        departmentRepository.findById(id).ifPresent(dept -> {
            // Delete all events (and their registrations) belonging to this department
            List<Event> events = eventRepository.findByDepartment(dept);
            for (Event event : events) {
                List<Registration> registrations = registrationRepository.findByEvent(event);
                registrationRepository.deleteAll(registrations);
                eventRepository.delete(event);
            }
            // Delete the workspace itself
            departmentRepository.delete(dept);
        });
    }
}
