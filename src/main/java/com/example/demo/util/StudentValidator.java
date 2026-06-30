package com.example.demo.util;

import com.example.demo.model.Department;
import com.example.demo.model.Student;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.StudentRepository;
import java.util.Optional;

public class StudentValidator {

    public static void validateStudentRegistration(
            String studentName, 
            String rollNumber, 
            String department, 
            DepartmentRepository departmentRepository, 
            StudentRepository studentRepository) {
        
        if (rollNumber == null || rollNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Roll number is required.");
        }
        if (studentName == null || studentName.trim().isEmpty()) {
            throw new IllegalArgumentException("Student name is required.");
        }
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department is required.");
        }

        String cleanRoll = rollNumber.trim().toUpperCase();
        String cleanDept = DepartmentNormalizer.normalize(department.trim());

        // 1. Cross-department uniqueness check
        Optional<Student> existingStudentOpt = studentRepository.findByRollNumberIgnoreCase(cleanRoll);
        if (existingStudentOpt.isPresent()) {
            throw new IllegalArgumentException("Student already registered.");
        }

        // 2. Roll Number & Department Validation
        Optional<Department> deptOpt = departmentRepository.findByNameIgnoreCase(cleanDept);
        if (deptOpt.isEmpty()) {
            deptOpt = departmentRepository.findByCode(cleanDept.toUpperCase());
        }
        if (deptOpt.isEmpty()) {
            // Try matching normalized department name
            deptOpt = departmentRepository.findAll().stream()
                    .filter(d -> DepartmentNormalizer.normalize(d.getName()).equalsIgnoreCase(cleanDept) 
                            || d.getCode().equalsIgnoreCase(cleanDept))
                    .findFirst();
        }

        if (deptOpt.isPresent()) {
            String deptCode = deptOpt.get().getCode().toUpperCase();
            
            // Extract alphabetic department code from Roll Number
            // e.g. 24AI001 -> AI
            String embeddedCode = cleanRoll.replaceAll("[^A-Z]", "");
            if (!embeddedCode.equals(deptCode)) {
                throw new IllegalArgumentException("The Roll Number " + rollNumber + " does not belong to the selected department: " + department + " (Expected code: " + deptCode + ")");
            }
        } else {
            throw new IllegalArgumentException("The selected department does not exist.");
        }
    }
}
