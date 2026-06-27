package com.example.demo.repository;

import com.example.demo.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    List<Achievement> findByCreatedByUsername(String username);
    List<Achievement> findByDepartmentIgnoreCase(String department);
    List<Achievement> findByRegisterNumberIn(List<String> registerNumbers);
}
