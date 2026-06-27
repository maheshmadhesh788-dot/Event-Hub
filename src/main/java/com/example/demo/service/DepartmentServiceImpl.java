package com.example.demo.service;

import com.example.demo.model.Department;
import com.example.demo.model.Event;
import com.example.demo.model.Registration;
import com.example.demo.model.User;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository,
                                 EventRepository eventRepository,
                                 RegistrationRepository registrationRepository,
                                 UserRepository userRepository,
                                 PasswordEncoder passwordEncoder) {
        this.departmentRepository = departmentRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
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
        dept.setCode(dept.getCode().trim().toUpperCase());
        
        // Enforce password format for newly created departments: DepartmentShortCode@123
        String rawPassword = (dept.getPassword() != null && !dept.getPassword().trim().isEmpty()) ? dept.getPassword().trim() : (dept.getCode() + "@123");
        dept.setPassword(rawPassword); // Save raw password in department table
        
        // Populate missing description, logoUrl, coverImageUrl
        if (dept.getDescription() == null || dept.getDescription().trim().isEmpty()) {
            dept.setDescription(generateDescription(dept.getName()));
        }
        if (dept.getLogoUrl() == null || dept.getLogoUrl().trim().isEmpty()) {
            dept.setLogoUrl("/images/kasc_logo.jpg");
        }
        if (dept.getCoverImageUrl() == null || dept.getCoverImageUrl().trim().isEmpty()) {
            dept.setCoverImageUrl(generateCoverImageUrl(dept.getName()));
        }
        
        Department saved = departmentRepository.saveAndFlush(dept);

        // Sync to users table with hashed password
        User user = new User(saved.getName(), passwordEncoder.encode(rawPassword), "ROLE_DEPARTMENT", saved.getName());
        userRepository.saveAndFlush(user);

        return saved;
    }

    @Override
    @Transactional
    public Department updateDepartment(Long id, Department updatedData) {
        return departmentRepository.findById(id).map(dept -> {
            String oldName = dept.getName();
            if (updatedData.getName() != null && !updatedData.getName().trim().isEmpty()) {
                dept.setName(updatedData.getName().trim());
            }
            if (updatedData.getCode() != null && !updatedData.getCode().trim().isEmpty()) {
                dept.setCode(updatedData.getCode().trim().toUpperCase());
            }
            
            String rawPwd = null;
            if (updatedData.getPassword() != null && !updatedData.getPassword().trim().isEmpty()) {
                rawPwd = updatedData.getPassword().trim();
                dept.setPassword(rawPwd); // Save raw password in department table
            }
            
            final String finalRawPwd = rawPwd;
            
            if (updatedData.getDescription() != null) {
                dept.setDescription(updatedData.getDescription());
            }
            if (updatedData.getLogoUrl() != null) {
                dept.setLogoUrl(updatedData.getLogoUrl());
            }
            if (updatedData.getCoverImageUrl() != null) {
                dept.setCoverImageUrl(updatedData.getCoverImageUrl());
            }
            if (updatedData.getHodName() != null) {
                dept.setHodName(updatedData.getHodName());
            }
            if (updatedData.getContactNumber() != null) {
                dept.setContactNumber(updatedData.getContactNumber());
            }
            if (updatedData.getEmail() != null) {
                dept.setEmail(updatedData.getEmail());
            }
            Department saved = departmentRepository.saveAndFlush(dept);

            // Sync to users table
            userRepository.findByUsernameIgnoreCase(oldName).ifPresentOrElse(user -> {
                user.setUsername(saved.getName());
                if (finalRawPwd != null) {
                    user.setPassword(passwordEncoder.encode(finalRawPwd));
                }
                user.setName(saved.getName());
                userRepository.saveAndFlush(user);
            }, () -> {
                User user = new User(saved.getName(), finalRawPwd != null ? passwordEncoder.encode(finalRawPwd) : "", "ROLE_DEPARTMENT", saved.getName());
                userRepository.saveAndFlush(user);
            });

            return saved;
        }).orElseThrow(() -> new RuntimeException("Department workspace not found with ID: " + id));
    }

    private String generateDescription(String name) {
        String n = name.toLowerCase();
        if (n.contains("tamil")) {
            return "The Department of Tamil focuses on classical and modern Tamil literature, language studies, cultural heritage, and linguistic proficiency.";
        } else if (n.contains("english")) {
            return "The Department of English focuses on language proficiency, literary analysis, creative writing, and communication skills.";
        } else if (n.contains("mathematics") || n.contains("math")) {
            return "The Department of Mathematics offers training in pure and applied mathematics, statistics, and logical reasoning for research and industry applications.";
        } else if (n.contains("computer science") || n.contains("cs")) {
            return "The Department of Computer Science prepares students with strong fundamentals in algorithms, software development, data structures, and computer systems.";
        } else if (n.contains("information technology") || n.contains("it")) {
            return "The Department of Information Technology specializes in software engineering, database management, cloud computing, and cybersecurity.";
        } else if (n.contains("artificial intelligence") || n.contains("aiml") || n.contains("machine learning")) {
            return "The Department of Artificial Intelligence and Machine Learning focuses on neural networks, machine learning algorithms, deep learning, and data analytics.";
        } else if (n.contains("electronics")) {
            return "The Department of Electronics and Communication focuses on electronic circuits, microcontroller programming, communication systems, and embedded systems.";
        } else if (n.contains("physics")) {
            return "The Department of Physics provides deep insights into classical mechanics, electromagnetism, quantum mechanics, and experimental physics.";
        } else if (n.contains("chemistry")) {
            return "The Department of Chemistry focuses on organic, inorganic, physical, and analytical chemistry, along with practical laboratory work.";
        } else if (n.contains("biochemistry")) {
            return "The Department of Biochemistry explores the chemical processes within and relating to living organisms, molecular biology, and genetics.";
        } else if (n.contains("biotechnology")) {
            return "The Department of Biotechnology integrates biological sciences with engineering principles to develop advanced healthcare and agricultural solutions.";
        } else if (n.contains("botany")) {
            return "The Department of Botany specializes in plant biology, taxonomy, physiology, ecology, and agricultural applications.";
        } else if (n.contains("zoology")) {
            return "The Department of Zoology focuses on animal biology, physiology, genetics, ecology, and evolutionary studies.";
        } else if (n.contains("commerce") || n.contains("b.com") || n.contains("com")) {
            return "The Department of Commerce focuses on accounting standards, auditing, banking, finance, and corporate management strategies.";
        } else if (n.contains("business administration") || n.contains("bba") || n.contains("mba")) {
            return "The Department of Business Administration prepares future leaders through training in marketing, human resources, operations, and financial management.";
        } else if (n.contains("costume design") || n.contains("fashion")) {
            return "The Department of Costume Design and Fashion nurtures creativity in apparel design, textile science, merchandising, and fashion technologies.";
        } else if (n.contains("wildlife")) {
            return "The Department of Wildlife Biology specializes in animal ecology, habitat conservation, biodiversity studies, and forestry.";
        } else if (n.contains("nutrition")) {
            return "The Department of Clinical Nutrition and Dietetics covers human nutrition, diet planning, metabolic disorders, and community health.";
        }
        return "The Department of " + name + " is dedicated to academic excellence, innovative research, and fostering professional and personal growth among students.";
    }

    private String generateCoverImageUrl(String name) {
        String n = name.toLowerCase();
        // Science and technology departments
        if (n.contains("computer") || n.contains("science") || n.contains("it") || n.contains("technology") ||
            n.contains("ai") || n.contains("ml") || n.contains("physics") || n.contains("chemistry") ||
            n.contains("biology") || n.contains("biochem") || n.contains("biotech") || n.contains("botany") ||
            n.contains("zoology") || n.contains("nutrition") || n.contains("electronics") || n.contains("data") ||
            n.contains("engineering") || n.contains("engg")) {
            return "/images/research_labs.webp";
        }
        // Humanities, Commerce, and others
        return "/images/student_spaces.jpg";
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        departmentRepository.findById(id).ifPresent(dept -> {
            String deptName = dept.getName();
            // Delete all events (and their registrations) belonging to this department
            List<Event> events = eventRepository.findByDepartment(dept);
            for (Event event : events) {
                List<Registration> registrations = registrationRepository.findByEvent(event);
                registrationRepository.deleteAll(registrations);
                eventRepository.delete(event);
            }
            // Delete the workspace itself
            departmentRepository.delete(dept);

            // Sync to users table
            userRepository.findByUsernameIgnoreCase(deptName).ifPresent(userRepository::delete);
        });
    }
}
