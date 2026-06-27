package com.example.demo.repository;

import com.example.demo.model.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long> {
    Optional<StaffProfile> findByUsernameIgnoreCase(String username);
    List<StaffProfile> findByDepartmentIgnoreCase(String department);
}
