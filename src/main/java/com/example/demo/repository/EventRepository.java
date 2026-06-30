package com.example.demo.repository;

import com.example.demo.model.Event;
import com.example.demo.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByType(String type);
    List<Event> findByDepartment(Department department);
    List<Event> findByDepartmentId(Long departmentId);
    List<Event> findByNameContainingIgnoreCase(String name);
    java.util.Optional<Event> findByNameIgnoreCase(String name);
}
