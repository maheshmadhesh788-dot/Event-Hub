package com.example.demo.config;

import com.example.demo.model.Department;
import com.example.demo.model.Event;
import com.example.demo.model.Notification;
import com.example.demo.model.Student;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final EventRepository eventRepository;
    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(EventRepository eventRepository,
                           NotificationRepository notificationRepository,
                           StudentRepository studentRepository,
                           DepartmentRepository departmentRepository,
                           PasswordEncoder passwordEncoder) {
        this.eventRepository = eventRepository;
        this.notificationRepository = notificationRepository;
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @SuppressWarnings("null")
    public void run(String... args) throws Exception {

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
                    null, null)
            };

            for (Department d : departments) {
                departmentRepository.save(d);
            }

            System.out.println("──────────────────────────────────────────────────");
            System.out.println("  DEFAULT DEPARTMENT ACCOUNTS");
            System.out.println("  Login with the department name or code + password 'dept123'");
            System.out.println("  • Information Technology  (IT)");
            System.out.println("  • Computer Science        (CS)");
            System.out.println("  • Electronics and Communication (EC)");
            System.out.println("  • Mathematics             (MATH)");
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
                    "9876543213", "divya.nair@kasc.ac.in",     "student123")
            };

            for (Student s : students) {
                studentRepository.save(s);
            }

            System.out.println("──────────────────────────────────────────────────");
            System.out.println("  DEFAULT STUDENT ACCOUNTS  (password: student123)");
            System.out.println("  • 22IT001 – Arun Kumar         (Information Technology)");
            System.out.println("  • 22CS001 – Priya Sharma       (Computer Science)");
            System.out.println("  • 22EC001 – Rahul Menon        (Electronics and Communication)");
            System.out.println("  • 22MA001 – Divya Nair         (Mathematics)");
            System.out.println("──────────────────────────────────────────────────");
        }

        // ─────────────────────────────────────────────────────────────
        // 3. Admin credentials reminder (configured via properties)
        // ─────────────────────────────────────────────────────────────
        System.out.println("  ADMIN ACCOUNT: username=admin  password=admin123");
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
}
