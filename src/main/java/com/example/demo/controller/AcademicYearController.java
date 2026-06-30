package com.example.demo.controller;

import com.example.demo.model.AcademicYear;
import com.example.demo.repository.AcademicYearRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/achievements/academic-years")
public class AcademicYearController {

    private final AcademicYearRepository academicYearRepository;

    public AcademicYearController(AcademicYearRepository academicYearRepository) {
        this.academicYearRepository = academicYearRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAcademicYears() {
        List<AcademicYear> list = academicYearRepository.findAll();
        if (list.isEmpty()) {
            // Seed default academic years representing 3-year academic cycles
            List<String> defaults = Arrays.asList("2022-2025", "2023-2026", "2024-2027", "2025-2028", "2026-2029");
            List<AcademicYear> seeded = new ArrayList<>();
            for (String yr : defaults) {
                AcademicYear academicYear = new AcademicYear(yr);
                seeded.add(academicYearRepository.save(academicYear));
            }
            return ResponseEntity.ok(seeded);
        }
        // Sort list naturally or chronologically
        list.sort(Comparator.comparing(AcademicYear::getName));
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<?> createAcademicYear(@RequestBody Map<String, String> payload, Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        
        // Only allow administrators to create a new academic year
        if (!"ROLE_ADMIN".equals(role) && !"ROLE_SUPER_ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access Denied: Only administrators can create new Academic Years."));
        }

        String name = payload.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Academic Year name is required."));
        }

        name = name.trim().replace("–", "-"); // Replace en-dash with standard hyphen if present

        // Validate format (YYYY-YYYY)
        if (!name.matches("^\\d{4}-\\d{4}$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid format. Expected YYYY-YYYY (e.g. 2024-2027)."));
        }

        String[] years = name.split("-");
        try {
            int startYear = Integer.parseInt(years[0]);
            int endYear = Integer.parseInt(years[1]);

            // Requirement: must represent the complete course duration (3 years)
            if (endYear - startYear != 3) {
                return ResponseEntity.badRequest().body(Map.of("error", "Academic Year must represent a complete 3-year course duration (e.g. 2024-2027)."));
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid years. Please ensure numeric years."));
        }

        if (academicYearRepository.findByNameIgnoreCase(name).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Academic Year already exists."));
        }

        AcademicYear saved = academicYearRepository.save(new AcademicYear(name));
        return ResponseEntity.ok(saved);
    }
}
