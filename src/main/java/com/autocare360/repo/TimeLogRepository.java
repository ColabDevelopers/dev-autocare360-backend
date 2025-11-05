package com.autocare360.repo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.autocare360.entity.TimeLog;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLog, Long> {

    // Find all time logs for a specific employee
    List<TimeLog> findByEmployee_IdOrderByDateDescCreatedAtDesc(Long employeeId);

    // Find time logs by employee and date range
    List<TimeLog> findByEmployee_IdAndDateBetweenOrderByDateDescCreatedAtDesc(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    // Pageable queries for time logs
    Page<TimeLog> findByEmployee_Id(Long employeeId, Pageable pageable);

    Page<TimeLog> findByEmployee_IdAndDateBetween(
            Long employeeId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    // Find time logs by employee and specific date
    List<TimeLog> findByEmployee_IdAndDate(Long employeeId, LocalDate date);

    // Sum hours for employee on specific date
    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM TimeLog t WHERE t.employee.id = :employeeId AND t.date = :date")
    BigDecimal sumHoursByEmployeeAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    // Sum hours for employee in date range
    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM TimeLog t WHERE t.employee.id = :employeeId AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumHoursByEmployeeAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Count total entries for employee
    Long countByEmployee_Id(Long employeeId);

    // Employee Dashboard Queries - Sum hours by appointment
    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM TimeLog t " +
            "WHERE t.employee.id = :employeeId AND t.appointment.id = :appointmentId")
    BigDecimal sumHoursByEmployeeAndAppointment(
            @Param("employeeId") Long employeeId,
            @Param("appointmentId") Long appointmentId);

    // Employee Dashboard Queries - Find by date range ordered by date (for weekly workload chart)
    List<TimeLog> findByEmployee_IdAndDateBetweenOrderByDateAsc(
            Long employeeId, LocalDate startDate, LocalDate endDate);
}