package com.example.demo.repository;

import com.example.demo.model.AchievementCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AchievementCategoryRepository extends JpaRepository<AchievementCategory, Long> {
    Optional<AchievementCategory> findByNameIgnoreCase(String name);
}
