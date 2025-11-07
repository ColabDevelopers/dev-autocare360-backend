package com.autocare360.repo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.autocare360.entity.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // ==========================================
    // YOUR EXISTING METHODS - KEEP ALL OF THESE
    // ==========================================
    List<Appointment> findByStatusOrderByDateAscTimeAsc(String status);

    List<Appointment> findByAssignedEmployee_IdAndStatusInOrderByDateAscTimeAsc(
            Long employeeId, List<String> statuses);

    List<Appointment> findByAssignedEmployee_IdAndStatusOrderByDateAscTimeAsc(
            Long employeeId, String status);

    List<Appointment> findByAssignedEmployee_IdOrderByDateAscTimeAsc(Long employeeId);

    List<Appointment> findByAssignedEmployee_IdAndDateOrderByTimeAsc(
            Long employeeId, LocalDate date);

    List<Appointment> findByAssignedEmployee_IdAndDateBetween(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.assignedEmployee.id = :employeeId " +
            "AND a.status = :status AND a.updatedAt BETWEEN :start AND :end")
    Integer countByAssignedEmployeeIdAndStatusAndUpdatedAtBetween(
            @Param("employeeId") Long employeeId,
            @Param("status") String status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // ==========================================
    // NEW METHODS FOR WORKLOAD MONITORING
    // ==========================================
    
    // Find unassigned appointments (for task assignment)
    List<Appointment> findByAssignedEmployeeIsNullAndStatusIn(List<String> statuses);
    
    // Find appointments by employee and date range (for schedule)
    @Query("SELECT a FROM Appointment a WHERE a.assignedEmployee.id = :employeeId " +
           "AND a.date BETWEEN :startDate AND :endDate ORDER BY a.date ASC, a.time ASC")
    List<Appointment> findByEmployeeIdAndAppointmentDateBetween(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    // Count appointments by employee and statuses (for workload calculation)
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.assignedEmployee.id = :employeeId " +
           "AND a.status IN :statuses")
    Integer countByEmployeeIdAndStatusIn(@Param("employeeId") Long employeeId, 
                                         @Param("statuses") List<String> statuses);
    
    // Find appointments for schedule display
    @Query("SELECT a FROM Appointment a WHERE a.assignedEmployee.id = :employeeId " +
           "AND FUNCTION('DATE', a.date) BETWEEN :startDate AND :endDate " +
           "AND a.status IN :statuses ORDER BY a.date ASC, a.time ASC")
    List<Appointment> findByEmployeeIdAndAppointmentDateBetweenAndStatusIn(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("statuses") List<String> statuses);
    
    // Find by employee and status
    List<Appointment> findByAssignedEmployee_IdAndStatus(Long employeeId, String status);
    
    // Alias method for compatibility
    default List<Appointment> findByEmployeeIdAndStatus(Long employeeId, String status) {
        return findByAssignedEmployee_IdAndStatus(employeeId, status);
    }
    
    // Calculate average completion time
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, a.createdAt, a.updatedAt)) " +
           "FROM Appointment a WHERE a.status = 'COMPLETED' AND a.updatedAt IS NOT NULL")
    Optional<BigDecimal> calculateAverageCompletionTime();
}