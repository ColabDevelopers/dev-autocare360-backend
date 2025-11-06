package com.autocare360.repo;

import com.autocare360.entity.Timer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimerRepository extends JpaRepository<Timer, Long> {

  // Find active timer for an employee
  Optional<Timer> findByEmployee_IdAndIsActiveTrue(Long employeeId);

  // Check if employee has active timer
  boolean existsByEmployee_IdAndIsActiveTrue(Long employeeId);
}
