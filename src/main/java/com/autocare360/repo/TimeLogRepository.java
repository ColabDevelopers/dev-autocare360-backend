package com.autocare360.repo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.autocare360.entity.TimeLog;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLog, Long> {

    List<TimeLog> findByEmployee_IdOrderByDateDescCreatedAtDesc(Long employeeId);

    List<TimeLog> findByEmployee_IdAndDateBetweenOrderByDateDescCreatedAtDesc(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    List<TimeLog> findByEmployee_IdAndDate(Long employeeId, LocalDate date);

    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM TimeLog t WHERE t.employee.id = :employeeId AND t.date = :date")
    BigDecimal sumHoursByEmployeeAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM TimeLog t WHERE t.employee.id = :employeeId AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumHoursByEmployeeAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    Long countByEmployee_Id(Long employeeId);

    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM TimeLog t " +
            "WHERE t.employee.id = :employeeId AND t.appointment.id = :appointmentId")
    BigDecimal sumHoursByEmployeeAndAppointment(
            @Param("employeeId") Long employeeId,
            @Param("appointmentId") Long appointmentId);

    List<TimeLog> findByEmployee_IdAndDateBetweenOrderByDateAsc(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    // Sum all hours in date range (for overall analytics)
    @Query("SELECT COALESCE(SUM(t.hours), 0) FROM TimeLog t " +
           "WHERE t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumHoursByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}