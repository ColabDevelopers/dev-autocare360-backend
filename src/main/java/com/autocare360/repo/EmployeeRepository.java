package com.autocare360.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autocare360.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Find active employees only
    List<Employee> findByStatus(String status);

    // Find employee by email
    Employee findByEmail(String email);
}