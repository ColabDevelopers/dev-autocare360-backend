package com.example.autocare360.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.autocare360.entity.Appointment;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    List<Appointment> findByUser_IdOrderByDateAscTimeAsc(Long userId);
    
    List<Appointment> findByUser_IdAndStatusOrderByDateAscTimeAsc(Long userId, String status);
    
    @Query("SELECT a FROM Appointment a WHERE a.date = :date AND a.time = :time AND a.status != 'CANCELLED'")
    List<Appointment> findByDateAndTimeAndStatusNotCancelled(@Param("date") LocalDate date, @Param("time") LocalTime time);
    
    @Query("SELECT a FROM Appointment a WHERE a.date = :date AND a.time = :time AND a.technician = :technician AND a.status != 'CANCELLED'")
    List<Appointment> findByDateAndTimeAndTechnicianAndStatusNotCancelled(@Param("date") LocalDate date, @Param("time") LocalTime time, @Param("technician") String technician);
    
    @Query("SELECT a FROM Appointment a WHERE a.date = :date AND a.status != 'CANCELLED' ORDER BY a.time")
    List<Appointment> findByDateAndStatusNotCancelledOrderByTime(@Param("date") LocalDate date);
    
    List<Appointment> findByDateBetweenOrderByDateAscTimeAsc(LocalDate startDate, LocalDate endDate);
    
    // method for checking time conflicts
    List<Appointment> findByDateAndTime(LocalDate date, LocalTime time);
    
    // Method to find appointments by status
    List<Appointment> findByStatusOrderByDateAscTimeAsc(String status);
}