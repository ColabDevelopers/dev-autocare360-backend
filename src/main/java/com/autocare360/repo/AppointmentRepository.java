package com.autocare360.repo;

import com.autocare360.entity.Appointment;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // ============================================================
    // General / status-based queries
    // ============================================================

    // Get all appointments ordered by date and time (ascending - oldest first)
    List<Appointment> findAllByOrderByDateAscTimeAsc();

    // Find by single status ordered by date/time
    List<Appointment> findByStatusOrderByDateAscTimeAsc(String status);

    // ============================================================
    // Customer queries
    // ============================================================

    // All appointments of a customer (User) ordered by newest first
    List<Appointment> findByUser_IdOrderByDateDescTimeDesc(Long userId);

    // ============================================================
    // Employee queries – using assignedUser (users table)
    // ============================================================

    // For a specific user (employee) and date
    List<Appointment> findByAssignedUser_IdAndDateOrderByTimeAsc(Long userId, LocalDate date);

    // For a specific user, date and multiple statuses
    List<Appointment> findByAssignedUser_IdAndDateAndStatusInOrderByTimeAsc(
            Long userId,
            LocalDate date,
            List<String> statuses
    );

    // All appointments for a user ordered by date/time
    List<Appointment> findByAssignedUser_IdOrderByDateAscTimeAsc(Long userId);

    // By user and multiple statuses
    List<Appointment> findByAssignedUser_IdAndStatusInOrderByDateAscTimeAsc(
            Long userId,
            List<String> statuses
    );

    // By user and single status
    List<Appointment> findByAssignedUser_IdAndStatusOrderByDateAscTimeAsc(
            Long userId,
            String status
    );

    // By user and date range
    List<Appointment> findByAssignedUser_IdAndDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    // Count completed by user in a date-time range
    @Query(
            "SELECT COUNT(a) FROM Appointment a " +
            "WHERE a.assignedUser.id = :userId " +
            "AND a.status = :status " +
            "AND a.updatedAt BETWEEN :start AND :end"
    )
    Integer countByAssignedUserIdAndStatusAndUpdatedAtBetween(
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // ============================================================
    // Employee queries – using assignedEmployee (legacy / technicians)
    // ============================================================

    // All appointments for an employee ordered by date/time
    List<Appointment> findByAssignedEmployee_IdOrderByDateAscTimeAsc(Long employeeId);

    // By employee and multiple statuses
    List<Appointment> findByAssignedEmployee_IdAndStatusInOrderByDateAscTimeAsc(
            Long employeeId,
            List<String> statuses
    );

    // By employee and single status
    List<Appointment> findByAssignedEmployee_IdAndStatusOrderByDateAscTimeAsc(
            Long employeeId,
            String status
    );

    // By employee and date
    List<Appointment> findByAssignedEmployee_IdAndDateOrderByTimeAsc(
            Long employeeId,
            LocalDate date
    );

    // By employee, date and multiple statuses
    List<Appointment> findByAssignedEmployee_IdAndDateAndStatusInOrderByTimeAsc(
            Long employeeId,
            LocalDate date,
            List<String> statuses
    );

    // By employee email and date (when the logged-in user is an employee)
    List<Appointment> findByAssignedEmployee_EmailAndDateOrderByTimeAsc(
            String email,
            LocalDate date
    );

    // By employee and date range
    List<Appointment> findByAssignedEmployee_IdAndDateBetween(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate
    );

    // Count completed by employee in a date-time range
    @Query(
            "SELECT COUNT(a) FROM Appointment a " +
            "WHERE a.assignedEmployee.id = :employeeId " +
            "AND a.status = :status " +
            "AND a.updatedAt BETWEEN :start AND :end"
    )
    Integer countByAssignedEmployeeIdAndStatusAndUpdatedAtBetween(
            @Param("employeeId") Long employeeId,
            @Param("status") String status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // ============================================================
    // Availability checks (technician / time-slot level)
    // ============================================================

    // Find appointments by date and technician for availability check
    List<Appointment> findByDateAndTechnicianAndStatusNot(
            LocalDate date,
            String technician,
            String excludeStatus
    );

    // Find appointments by date and time for availability check
    List<Appointment> findByDateAndTimeAndStatusNot(
            LocalDate date,
            LocalTime time,
            String excludeStatus
    );

    // ============================================================
    // Workload / task-assignment helpers (your part)
    // ============================================================

    /**
     * Find unassigned appointments with specific statuses
     * (used in workload / task assignment).
     */
    List<Appointment> findByAssignedEmployeeIsNullAndStatusIn(List<String> statuses);

    /**
     * Count active appointments for a given employee where status is in the given list
     * (e.g. PENDING, IN_PROGRESS, SCHEDULED).
     */
    int countByAssignedEmployee_IdAndStatusIn(Long employeeId, List<String> statuses);
}
