package com.example.demo.config;

import com.example.demo.model.Department;
import com.example.demo.model.Event;
import com.example.demo.model.Notification;
import com.example.demo.model.Student;
import com.example.demo.model.User;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.StaffProfileRepository;
import com.example.demo.model.StaffProfile;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import com.example.demo.model.Achievement;
import com.example.demo.model.Registration;
import com.example.demo.model.EventParticipation;
import com.example.demo.repository.AchievementRepository;
import com.example.demo.repository.RegistrationRepository;
import com.example.demo.repository.EventParticipationRepository;
import com.example.demo.util.DepartmentNormalizer;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EventRepository eventRepository;
    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AchievementRepository achievementRepository;
    private final RegistrationRepository registrationRepository;
    private final EventParticipationRepository eventParticipationRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    @org.springframework.beans.factory.annotation.Value("${eventhub.admin.username:admin}")
    private String adminUsername;

    @org.springframework.beans.factory.annotation.Value("${eventhub.admin.password:admin123}")
    private String adminPassword;

    public DataInitializer(EventRepository eventRepository,
                           NotificationRepository notificationRepository,
                           StudentRepository studentRepository,
                           DepartmentRepository departmentRepository,
                           UserRepository userRepository,
                           StaffProfileRepository staffProfileRepository,
                           PasswordEncoder passwordEncoder,
                           AchievementRepository achievementRepository,
                           RegistrationRepository registrationRepository,
                           EventParticipationRepository eventParticipationRepository) {
        this.eventRepository = eventRepository;
        this.notificationRepository = notificationRepository;
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.achievementRepository = achievementRepository;
        this.registrationRepository = registrationRepository;
        this.eventParticipationRepository = eventParticipationRepository;
    }

    @Override
    @SuppressWarnings("null")
    @org.springframework.transaction.annotation.Transactional
    public void run(String... args) throws Exception {
        // Run department cleanup / normalization migration
        migrateDepartments();

        // Clean fake/test students
        cleanFakeStudents();

        // ─────────────────────────────────────────────────────────────
        // 0. Seed Admin & Super Admin Users in the users table
        // ─────────────────────────────────────────────────────────────
        userRepository.findByUsername(adminUsername).ifPresentOrElse(admin -> {
            admin.setPassword(passwordEncoder.encode(adminPassword));
            userRepository.save(admin);
        }, () -> {
            User admin = new User(adminUsername, passwordEncoder.encode(adminPassword), "ROLE_ADMIN", "College Admin");
            userRepository.save(admin);
        });
        userRepository.findByUsername("Maheshwaran").ifPresentOrElse(superAdmin -> {
            superAdmin.setPassword(passwordEncoder.encode("mahesh@2006"));
            userRepository.save(superAdmin);
        }, () -> {
            User superAdmin = new User("Maheshwaran", passwordEncoder.encode("mahesh@2006"), "ROLE_SUPER_ADMIN", "Super Admin");
            userRepository.save(superAdmin);
        });

        // ─────────────────────────────────────────────────────────────
        // 1. Seed Default Departments (ROLE_DEPARTMENT users)
        // ─────────────────────────────────────────────────────────────
        if (departmentRepository.count() == 0) {
            Department[] departments = {
                makeDept("Information Technology",      "IT",   "dept123",
                    "The IT department focuses on software engineering, networking, and emerging technologies.",
                    null, null),
                makeDept("Computer Science",            "CS",   "dept123",
                    "The CS department covers algorithms, data structures, AI, and theoretical computing.",
                    null, null),
                makeDept("Electronics and Communication", "EC", "dept123",
                    "The EC department specialises in embedded systems, VLSI, and wireless communication.",
                    null, null),
                makeDept("Mathematics",                 "MATH", "dept123",
                    "The Mathematics department covers pure and applied mathematics, statistics, and operations research.",
                    null, null),
                makeDept("Department User",             "DEPT1", "password123",
                    "Department for role testing",
                    null, null)
            };

            for (Department d : departments) {
                Department saved = departmentRepository.save(d);
                if (userRepository.findByUsername(saved.getName()).isEmpty()) {
                    User user = new User(saved.getName(), d.getPassword(), "ROLE_DEPARTMENT", saved.getName());
                    userRepository.save(user);
                }
            }

            System.out.println("──────────────────────────────────────────────────");
            System.out.println("  DEFAULT DEPARTMENT ACCOUNTS");
            System.out.println("  Login with the department name or code + password 'dept123'");
            System.out.println("  • Information Technology  (IT)");
            System.out.println("  • Computer Science        (CS)");
            System.out.println("  • Electronics and Communication (EC)");
            System.out.println("  • Mathematics             (MATH)");
            System.out.println("  • Department User         (DEPT1) - password 'password123'");
            System.out.println("──────────────────────────────────────────────────");
        }

        // ─────────────────────────────────────────────────────────────
        // 2. Seed Default Students (ROLE_STUDENT users)
        // ─────────────────────────────────────────────────────────────
        if (studentRepository.count() == 0) {
            Student[] students = {
                makeStudent("22IT001", "Arun Kumar",       "Information Technology",
                    "9876543210", "arun.kumar@kasc.ac.in",     "student123"),
                makeStudent("22CS001", "Priya Sharma",     "Computer Science",
                    "9876543211", "priya.sharma@kasc.ac.in",   "student123"),
                makeStudent("22EC001", "Rahul Menon",      "Electronics and Communication",
                    "9876543212", "rahul.menon@kasc.ac.in",    "student123"),
                makeStudent("22MA001", "Divya Nair",       "Mathematics",
                    "9876543213", "divya.nair@kasc.ac.in",     "student123"),
                makeStudent("STUDENT1", "Student User",     "Information Technology",
                    "9999999999", "student1@example.com",     "password123")
            };

            for (Student s : students) {
                Student saved = studentRepository.save(s);
                if (userRepository.findByUsername(saved.getRollNumber()).isEmpty()) {
                    User user = new User(saved.getRollNumber(), s.getPassword(), "ROLE_STUDENT", saved.getStudentName(), saved.getEmail(), saved.getContactNumber());
                    userRepository.save(user);
                }
            }

            System.out.println("──────────────────────────────────────────────────");
            System.out.println("  DEFAULT STUDENT ACCOUNTS  (password: student123)");
            System.out.println("  • 22IT001 – Arun Kumar         (Information Technology)");
            System.out.println("  • 22CS001 – Priya Sharma       (Computer Science)");
            System.out.println("  • 22EC001 – Rahul Menon        (Electronics and Communication)");
            System.out.println("  • 22MA001 – Divya Nair         (Mathematics)");
            System.out.println("  • STUDENT1 – Student User      (Information Technology) - password 'password123'");
            System.out.println("──────────────────────────────────────────────────");
        }

        // ─────────────────────────────────────────────────────────────
        // 2.5. Sync all existing departments and students to users table
        // ─────────────────────────────────────────────────────────────
        for (Department d : departmentRepository.findAll()) {
            boolean updated = false;
            if (d.getDescription() == null || d.getDescription().trim().isEmpty()) {
                d.setDescription(generateDescription(d.getName()));
                updated = true;
            }
            if (d.getLogoUrl() == null || d.getLogoUrl().trim().isEmpty()) {
                d.setLogoUrl("/images/kasc_logo.jpg");
                updated = true;
            }
            if (d.getCoverImageUrl() == null || d.getCoverImageUrl().trim().isEmpty()) {
                d.setCoverImageUrl(generateCoverImageUrl(d.getName()));
                updated = true;
            }
            
            // Standardize department passwords to "Department@123" (except B.Sc Artificial Intelligence & Data Science)
            if (!d.getName().equalsIgnoreCase("B.Sc Artificial Intelligence & Data Science")) {
                if (d.getPassword() == null || !d.getPassword().equals("Department@123")) {
                    d.setPassword("Department@123");
                    updated = true;
                }
            }
            
            if (updated) {
                departmentRepository.saveAndFlush(d);
            }

            // Sync credentials to users table
            String targetPassword = "Department@123";
            if (d.getName().equalsIgnoreCase("B.Sc Artificial Intelligence & Data Science")) {
                targetPassword = (d.getPassword() != null && !d.getPassword().trim().isEmpty()) ? d.getPassword().trim() : "dept123";
            }
            
            final String rawPassword = targetPassword;
            userRepository.findByUsernameIgnoreCase(d.getName()).ifPresentOrElse(user -> {
                user.setPassword(passwordEncoder.encode(rawPassword));
                userRepository.saveAndFlush(user);
            }, () -> {
                User user = new User(d.getName(), passwordEncoder.encode(rawPassword), "ROLE_DEPARTMENT", d.getName());
                userRepository.saveAndFlush(user);
            });
        }
        for (Student s : studentRepository.findAll()) {
            if (s.getPassword() != null && userRepository.findByUsername(s.getRollNumber()).isEmpty()) {
                User user = new User(s.getRollNumber(), s.getPassword(), "ROLE_STUDENT", s.getStudentName(), s.getEmail(), s.getContactNumber());
                userRepository.save(user);
            }
        }

        // ─────────────────────────────────────────────────────────────
        // 3. Admin & Super Admin credentials reminder
        // ─────────────────────────────────────────────────────────────
        System.out.println("  SUPER ADMIN: username=Maheshwaran  password=mahesh@2006");
        System.out.println("  ADMIN ACCOUNT: username=" + adminUsername + "  password=" + adminPassword);
        System.out.println("──────────────────────────────────────────────────");

        // ─────────────────────────────────────────────────────────────
        // 4. Seed Sample Events
        // ─────────────────────────────────────────────────────────────
        if (eventRepository.count() == 0) {
            Event collegeEvent1 = new Event();
            collegeEvent1.setName("Golden Jubilee Science Exhibition - KASC EXPO 2026");
            collegeEvent1.setVenue("KASC Main Auditorium");
            collegeEvent1.setDateTime(LocalDateTime.now().plusDays(5).withHour(9).withMinute(0).withSecond(0).withNano(0));
            collegeEvent1.setDescription("The mega annual science exhibition and project display at Kongunadu Arts and Science College. Open for schools and colleges across Tamil Nadu with attractive cash prizes.");
            collegeEvent1.setType("COLLEGE");
            collegeEvent1.setPosterUrl("/uploads/xenon.jpg");
            collegeEvent1.setInChargeStaffName("Dr. R. Maheshwari");
            collegeEvent1.setInChargeStaffContact("9876543210");
            eventRepository.save(collegeEvent1);

            // Seed IT department events
            Department itDept = departmentRepository.findByCode("IT").orElse(null);
            if (itDept != null) {
                // Future event
                Event futureEvent = new Event();
                futureEvent.setName("HackIT 2026 Symposium");
                futureEvent.setVenue("IT Lab 2");
                futureEvent.setDateTime(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0));
                futureEvent.setDescription("State level coding, web designing, and debugging symposium organized by Department of Information Technology.");
                futureEvent.setType("DEPARTMENT");
                futureEvent.setDepartment(itDept);
                futureEvent.setInChargeStaffName("Dr. S. Karthik");
                futureEvent.setInChargeStaffContact("9876543214");
                eventRepository.save(futureEvent);

                // Past event (Conducted)
                Event pastEvent = new Event();
                pastEvent.setName("Alumni Meet 2025");
                pastEvent.setVenue("Conference Hall");
                pastEvent.setDateTime(LocalDateTime.now().minusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0));
                pastEvent.setDescription("Annual alumni gathering of the IT department to share industry experiences with current students.");
                pastEvent.setType("DEPARTMENT");
                pastEvent.setDepartment(itDept);
                pastEvent.setInChargeStaffName("Dr. S. Karthik");
                pastEvent.setInChargeStaffContact("9876543214");
                eventRepository.save(pastEvent);
            }

            System.out.println("Initialized sample events.");
        }

        // ─────────────────────────────────────────────────────────────
        // 5. Seed Sample Notifications
        // ─────────────────────────────────────────────────────────────
        if (notificationRepository.count() == 0) {
            Long expoEventId = null;
            List<Event> events = eventRepository.findByNameContainingIgnoreCase("KASC EXPO 2026");
            if (!events.isEmpty()) {
                expoEventId = events.get(0).getId();
            }

            notificationRepository.save(new Notification(
                "Welcome to KASC Event Hub!",
                "The centralized platform for college and department-level event coordination at Kongunadu Arts and Science College is now live. Explore events, register for competitions, and stay updated with real-time notices!",
                "College Admin",
                LocalDateTime.now().minusHours(3)
            ));
            notificationRepository.save(new Notification(
                "KASC EXPO 2026 Project Registrations Open",
                "Registrations for the Golden Jubilee Science Exhibition - KASC EXPO 2026 are officially open! View details on the Events page and secure your slot today.",
                "College Admin",
                LocalDateTime.now().minusHours(2),
                expoEventId
            ));
            notificationRepository.save(new Notification(
                "HackIT 2026 Symposium Announced",
                "The Department of IT has published a new department-level event: State Level Symposium - HackIT 2026. Registration is open to all students. Check it out now!",
                "Department of IT",
                LocalDateTime.now().minusMinutes(45)
            ));
            System.out.println("Initialized default notifications.");
        }

    }

    // ── Helpers ──────────────────────────────────────────────────────

    private Department makeDept(String name, String code, String rawPassword,
                                String description, String logoUrl, String coverImageUrl) {
        Department d = new Department(name, code, passwordEncoder.encode(rawPassword));
        d.setDescription(description);
        d.setLogoUrl(logoUrl);
        d.setCoverImageUrl(coverImageUrl);
        return d;
    }

    private Student makeStudent(String rollNumber, String studentName, String department,
                                String contactNumber, String email, String rawPassword) {
        return new Student(rollNumber, studentName, department,
                contactNumber, email, passwordEncoder.encode(rawPassword));
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



    private void migrateDepartments() {
        System.out.println("Running department normalization migration...");
        List<Department> allDepts = departmentRepository.findAll();
        Map<String, Department> canonicalMap = new HashMap<>();

        for (Department d : allDepts) {
            String normalizedName = DepartmentNormalizer.normalize(d.getName());
            
            if (canonicalMap.containsKey(normalizedName)) {
                Department canonical = canonicalMap.get(normalizedName);
                System.out.println("Merging duplicate department: '" + d.getName() + "' (ID: " + d.getId() + ") into '" + canonical.getName() + "' (ID: " + canonical.getId() + ")");
                
                // 1. Update events pointing to duplicate dept
                List<Event> events = eventRepository.findByDepartment(d);
                for (Event e : events) {
                    e.setDepartment(canonical);
                    eventRepository.save(e);
                }
                
                // 2. Sync corresponding ROLE_DEPARTMENT user
                String oldName = d.getName();
                userRepository.findByUsernameIgnoreCase(oldName).ifPresent(user -> {
                    Optional<User> canonicalUserOpt = userRepository.findByUsernameIgnoreCase(canonical.getName());
                    if (canonicalUserOpt.isPresent()) {
                        userRepository.delete(user);
                    } else {
                        user.setUsername(canonical.getName());
                        user.setName(canonical.getName());
                        userRepository.save(user);
                    }
                });
                
                // 3. Delete duplicate department
                departmentRepository.delete(d);
            } else {
                if (!d.getName().equals(normalizedName)) {
                    String oldName = d.getName();
                    System.out.println("Renaming Department: '" + oldName + "' -> '" + normalizedName + "'");
                    d.setName(normalizedName);
                    departmentRepository.save(d);
                    
                    // Update user username as well
                    userRepository.findByUsernameIgnoreCase(oldName).ifPresent(user -> {
                        user.setUsername(normalizedName);
                        user.setName(normalizedName);
                        userRepository.save(user);
                    });
                }
                canonicalMap.put(normalizedName, d);
            }
        }
        
        // Save and flush any changes before normalizing other entities
        departmentRepository.flush();
        userRepository.flush();

        // 4. Normalize students table
        for (Student s : studentRepository.findAll()) {
            String norm = DepartmentNormalizer.normalize(s.getDepartment());
            if (s.getDepartment() == null || !norm.equals(s.getDepartment())) {
                s.setDepartment(norm);
                studentRepository.save(s);
            }
        }

        // 5. Normalize staff profiles table
        for (StaffProfile sp : staffProfileRepository.findAll()) {
            String norm = DepartmentNormalizer.normalize(sp.getDepartment());
            if (sp.getDepartment() == null || !norm.equals(sp.getDepartment())) {
                sp.setDepartment(norm);
                staffProfileRepository.save(sp);
            }
        }

        // 6. Normalize achievements table
        for (Achievement a : achievementRepository.findAll()) {
            String norm = DepartmentNormalizer.normalize(a.getDepartment());
            if (a.getDepartment() == null || !norm.equals(a.getDepartment())) {
                a.setDepartment(norm);
                achievementRepository.save(a);
            }
        }

        // 7. Normalize registrations table
        for (Registration r : registrationRepository.findAll()) {
            String norm = DepartmentNormalizer.normalize(r.getDepartment());
            if (r.getDepartment() == null || !norm.equals(r.getDepartment())) {
                r.setDepartment(norm);
                registrationRepository.save(r);
            }
        }

        // 8. Normalize event participation table
        for (EventParticipation ep : eventParticipationRepository.findAll()) {
            String norm = DepartmentNormalizer.normalize(ep.getDepartment());
            if (ep.getDepartment() == null || !norm.equals(ep.getDepartment())) {
                ep.setDepartment(norm);
                eventParticipationRepository.save(ep);
            }
        }
        
        System.out.println("Department normalization migration complete.");
    }

    @org.springframework.transaction.annotation.Transactional
    public void cleanFakeStudents() {
        System.out.println("Cleaning fake and invalid student profiles using EntityManager...");
        
        List<Object[]> studentsToClean = entityManager.createQuery(
            "SELECT s.id, s.rollNumber, s.studentName, s.department FROM Student s", Object[].class)
            .getResultList();
            
        List<String> officialDepts = List.of(
            "Information Technology",
            "Computer Science",
            "Electronics and Communication Engineering",
            "Mathematics",
            "B.Sc Artificial intelligence and machine learning",
            "B.Sc Artificial intelligence and data science",
            "Electrical and Electronics Engineering",
            "Bachelor of Computer Applications",
            "Tamil Department",
            "Department of Zoology",
            "Department User"
        );

        for (Object[] studentInfo : studentsToClean) {
            Long studentId = (Long) studentInfo[0];
            String roll = (String) studentInfo[1];
            String name = (String) studentInfo[2];
            String dept = (String) studentInfo[3];

            String normalizedDept = com.example.demo.util.DepartmentNormalizer.normalize(dept);

            boolean isFake = false;
            if (roll == null || name == null) {
                isFake = true;
            } else {
                String uRoll = roll.toUpperCase();
                String lName = name.toLowerCase();
                if (uRoll.contains("TEST") || uRoll.contains("DUMMY") || uRoll.contains("TEMP") || uRoll.contains("FAKE")) {
                    isFake = true;
                } else if (lName.contains("test") || lName.contains("dummy") || lName.contains("fake") || lName.contains("temp")) {
                    isFake = true;
                } else if (uRoll.equals("STUDENT1")) {
                    isFake = true;
                } else if (normalizedDept == null || !officialDepts.contains(normalizedDept)) {
                    isFake = true;
                }
            }

            if (isFake) {
                System.out.println("Deleting fake/invalid student: " + name + " (" + roll + ") with ID: " + studentId);
                
                entityManager.createQuery("DELETE FROM Attendance a WHERE a.student.id = :sid")
                    .setParameter("sid", studentId).executeUpdate();

                entityManager.createQuery("DELETE FROM Bookmark b WHERE b.student.id = :sid")
                    .setParameter("sid", studentId).executeUpdate();

                entityManager.createQuery("DELETE FROM Feedback f WHERE f.student.id = :sid")
                    .setParameter("sid", studentId).executeUpdate();

                entityManager.createQuery("DELETE FROM Certificate c WHERE c.student.id = :sid")
                    .setParameter("sid", studentId).executeUpdate();

                if (roll != null) {
                    entityManager.createQuery("DELETE FROM TutorStudentAssignment ta WHERE ta.studentRegisterNumber = :roll")
                        .setParameter("roll", roll).executeUpdate();
                }

                entityManager.createQuery("DELETE FROM Registration r WHERE r.student.id = :sid")
                    .setParameter("sid", studentId).executeUpdate();

                if (roll != null) {
                    entityManager.createQuery("DELETE FROM EventParticipation ep WHERE ep.rollNumber = :roll")
                        .setParameter("roll", roll).executeUpdate();
                }

                entityManager.createQuery("DELETE FROM Student s WHERE s.id = :sid")
                    .setParameter("sid", studentId).executeUpdate();

                if (roll != null) {
                    entityManager.createQuery("DELETE FROM User u WHERE LOWER(u.username) = :roll")
                        .setParameter("roll", roll.toLowerCase()).executeUpdate();
                }
            }
        }
        System.out.println("Fake student profiles cleanup complete.");
    }
}
