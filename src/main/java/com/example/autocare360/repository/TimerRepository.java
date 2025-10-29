package com.example.autocare360.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.autocare360.entity.Timer;

@Repository
public interface TimerRepository extends JpaRepository<Timer, Long> {
    
    // Find active timer for an employee
    Optional<Timer> findByEmployee_IdAndIsActiveTrue(Long employeeId);
    
    // Check if employee has active timer
    boolean existsByEmployee_IdAndIsActiveTrue(Long employeeId);
}
