package com.example.autocare360.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.autocare360.entity.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    Optional<Schedule> findByDayOfWeekAndAvailableTrue(String dayOfWeek);
    
    List<Schedule> findByAvailableTrueOrderByDayOfWeek();
    
    List<Schedule> findByDayOfWeekOrderById(String dayOfWeek);
}