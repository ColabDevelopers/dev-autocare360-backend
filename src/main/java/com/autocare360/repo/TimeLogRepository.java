package com.autocare360.repo;

import com.autocare360.entity.TimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLog, Long> {
    
    // Find all time logs for a specific employee
    List<TimeLog> findByEmployeeId(Long employeeId);
    
    // Find time logs for an employee within a date range
    @Query("SELECT t FROM TimeLog t WHERE t.employeeId = :employeeId " +
           "AND t.startTime >= :startDate AND t.endTime <= :endDate")
    List<TimeLog> findByEmployeeIdAndDateRange(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Find time logs for a work item
    List<TimeLog> findByWorkItemId(Long workItemId);
    
    // Calculate total hours for employee in date range
    @Query("SELECT COALESCE(SUM(t.minutes), 0) FROM TimeLog t " +
           "WHERE t.employeeId = :employeeId " +
           "AND t.startTime >= :startDate AND t.endTime <= :endDate")
    Double sumMinutesByEmployeeIdAndDateRange(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Get total hours logged by all employees in date range
    @Query("SELECT t.employeeId, SUM(t.minutes) FROM TimeLog t " +
           "WHERE t.startTime >= :startDate AND t.endTime <= :endDate " +
           "GROUP BY t.employeeId")
    List<Object[]> sumMinutesGroupedByEmployee(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}