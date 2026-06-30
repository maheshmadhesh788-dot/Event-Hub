package com.example.demo.controller;

import com.example.demo.model.AchievementCategory;
import com.example.demo.repository.AchievementCategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/achievements/categories")
public class AchievementCategoryController {

    private final AchievementCategoryRepository categoryRepository;

    public AchievementCategoryController(AchievementCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ResponseEntity<List<AchievementCategory>> getAllCategories() {
        List<AchievementCategory> list = categoryRepository.findAll();
        if (list.isEmpty()) {
            // Seed default categories
            List<String> defaults = Arrays.asList(
                "Debate", "Conference", "Workshop", "Symposium", 
                "Seminar", "Hackathon", "Sports", "Cultural", "Others"
            );
            for (String name : defaults) {
                if (categoryRepository.findByNameIgnoreCase(name).isEmpty()) {
                    categoryRepository.save(new AchievementCategory(name));
                }
            }
            list = categoryRepository.findAll();
        }
        list.sort(Comparator.comparing(AchievementCategory::getName));
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<?> addCategory(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Category name is required"));
        }
        String cleanName = name.trim();
        Optional<AchievementCategory> existing = categoryRepository.findByNameIgnoreCase(cleanName);
        if (existing.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Category name already exists"));
        }
        AchievementCategory saved = categoryRepository.save(new AchievementCategory(cleanName));
        return ResponseEntity.ok(saved);
    }
}
