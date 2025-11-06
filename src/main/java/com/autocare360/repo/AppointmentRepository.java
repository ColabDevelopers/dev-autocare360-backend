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

  // Customer queries
  List<Appointment> findByUser_IdOrderByDateDescTimeDesc(Long userId);

  // Find appointments by date and technician for availability check
  List<Appointment> findByDateAndTechnicianAndStatusNot(LocalDate date, String technician, String excludeStatus);

  // Find appointments by date and time for availability check
  List<Appointment> findByDateAndTimeAndStatusNot(LocalDate date, java.time.LocalTime time, String excludeStatus);

  // Employee Dashboard Queries - Find by employee and status
  List<Appointment> findByAssignedEmployee_IdAndStatusInOrderByDateAscTimeAsc(
      Long employeeId, List<String> statuses);

  List<Appointment> findByAssignedEmployee_IdAndStatusOrderByDateAscTimeAsc(
      Long employeeId, String status);

  List<Appointment> findByAssignedEmployee_IdOrderByDateAscTimeAsc(Long employeeId);

  // Employee Dashboard Queries - Find by employee and date
  List<Appointment> findByAssignedEmployee_IdAndDateOrderByTimeAsc(Long employeeId, LocalDate date);

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
}
