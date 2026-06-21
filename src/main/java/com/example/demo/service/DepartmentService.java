package com.example.demo.service;

import com.example.demo.model.Department;
import java.util.List;
import java.util.Optional;

public interface DepartmentService {
    List<Department> getAllDepartments();
    Optional<Department> getDepartmentById(Long id);
    Optional<Department> findByName(String name);
    Optional<Department> findByCode(String code);
    Department createDepartment(Department dept);
    Department updateDepartment(Long id, Department updatedData);
    void deleteDepartment(Long id);
}
