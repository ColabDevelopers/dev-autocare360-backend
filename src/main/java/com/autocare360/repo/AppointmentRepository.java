package com.autocare360.repo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.autocare360.entity.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
  List<Appointment> findByStatusOrderByDateAscTimeAsc(String status);

  // Get all appointments ordered by date and time (ascending - oldest first)
  List<Appointment> findAllByOrderByDateAscTimeAsc();

  // Customer queries
  List<Appointment> findByUser_IdOrderByDateDescTimeDesc(Long userId);

  // Employee queries - Find by assigned user (employee from users table)
  List<Appointment> findByAssignedUser_IdAndDateOrderByTimeAsc(Long userId, LocalDate date);

  // Employee queries - Find by assigned user, date and status  
  List<Appointment> findByAssignedUser_IdAndDateAndStatusInOrderByTimeAsc(
      Long userId, LocalDate date, List<String> statuses);

  List<Appointment> findByAssignedUser_IdOrderByDateAscTimeAsc(Long userId);

  // Find appointments by date and technician for availability check
  List<Appointment> findByDateAndTechnicianAndStatusNot(LocalDate date, String technician, String excludeStatus);

  // Find appointments by date and time for availability check
  List<Appointment> findByDateAndTimeAndStatusNot(LocalDate date, java.time.LocalTime time, String excludeStatus);

  // Employee Dashboard Queries - Find by assigned user (from users table) and status
  List<Appointment> findByAssignedUser_IdAndStatusInOrderByDateAscTimeAsc(
      Long userId, List<String> statuses);

  // Employee Dashboard Queries - Find by assigned user (from users table) and single status
  List<Appointment> findByAssignedUser_IdAndStatusOrderByDateAscTimeAsc(
      Long userId, String status);

  // Employee Dashboard Queries - Find by assigned user and date range
  List<Appointment> findByAssignedUser_IdAndDateBetween(
      Long userId, LocalDate startDate, LocalDate endDate);

  // DEPRECATED - Employee Dashboard Queries - Find by employee and status
  List<Appointment> findByAssignedEmployee_IdAndStatusInOrderByDateAscTimeAsc(
      Long employeeId, List<String> statuses);

  List<Appointment> findByAssignedEmployee_IdAndStatusOrderByDateAscTimeAsc(
      Long employeeId, String status);

  List<Appointment> findByAssignedEmployee_IdOrderByDateAscTimeAsc(Long employeeId);

  // Employee Dashboard Queries - Find by employee and date
  List<Appointment> findByAssignedEmployee_IdAndDateOrderByTimeAsc(Long employeeId, LocalDate date);

  // Employee Dashboard Queries - Find by employee, date and status
  List<Appointment> findByAssignedEmployee_IdAndDateAndStatusInOrderByTimeAsc(
      Long employeeId, LocalDate date, List<String> statuses);

  // Find appointments by employee email and date (for users logged in as employees)
  List<Appointment> findByAssignedEmployee_EmailAndDateOrderByTimeAsc(String email, LocalDate date);

  List<Appointment> findByAssignedEmployee_IdAndDateBetween(
      Long employeeId, LocalDate startDate, LocalDate endDate);

  // Employee Dashboard Queries - Count completed by employee in date range
  @Query(
      "SELECT COUNT(a) FROM Appointment a WHERE a.assignedEmployee.id = :employeeId "
          + "AND a.status = :status AND a.updatedAt BETWEEN :start AND :end")
  Integer countByAssignedEmployeeIdAndStatusAndUpdatedAtBetween(
      @Param("employeeId") Long employeeId,
      @Param("status") String status,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  // Employee Dashboard Queries - Count completed by user in date range  
  @Query(
      "SELECT COUNT(a) FROM Appointment a WHERE a.assignedUser.id = :userId "
          + "AND a.status = :status AND a.updatedAt BETWEEN :start AND :end")
  Integer countByAssignedUserIdAndStatusAndUpdatedAtBetween(
      @Param("userId") Long userId,
      @Param("status") String status,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);
}
