package com.autocare360.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "appointments")
public class Appointment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnore // Prevent circular reference
  private User user;

  @Column(nullable = false)
  private String service;

  @Column(nullable = false)
  private String vehicle;

  @Column(nullable = false)
  private LocalDate date;

  @Column(nullable = false)
  private LocalTime time;

  @Column(nullable = false, length = 20)
  private String status = "PENDING";

  @Column(length = 1000)
  private String notes;

  @Column(name = "progress")
  private Integer progress = 0; // Progress percentage (0-100), defaults to 0

  @Column(name = "due_date")
  private LocalDate dueDate; // When the job should be completed

  @Column(name = "special_instructions", length = 1000)
  private String specialInstructions; // Special instructions for technician

  private String
      technician; // DEPRECATED: Keep for backward compatibility, use assignedEmployee instead

  // Employee reference (from Employee table - for backward compatibility)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id")
  @JsonIgnore
  private Employee assignedEmployee;

  // Assigned User (employee from User table)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assigned_user_id")
  @JsonIgnore
  private User assignedUser;

  // NEW: Project tracking fields
  @Column(name = "estimated_hours", precision = 5, scale = 2)
  private BigDecimal estimatedHours;

  @Column(name = "actual_hours", precision = 5, scale = 2)
  private BigDecimal actualHours = BigDecimal.ZERO;

  // NEW: Relationships for time tracking
  @OneToMany(mappedBy = "appointment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JsonIgnore
  private List<TimeLog> timeLogs;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // Constructors
  public Appointment() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public Appointment(User user, String service, String vehicle, LocalDate date, LocalTime time) {
    this();
    this.user = user;
    this.service = service;
    this.vehicle = vehicle;
    this.date = date;
    this.time = time;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  // Helper method to get userId
  public Long getUserId() {
    return user != null ? user.getId() : null;
  }

  // Helper method to set userId
  public void setUserId(Long userId) {
    if (this.user == null) {
      this.user = new User();
    }
    this.user.setId(userId);
  }

  public String getService() {
    return service;
  }

  public void setService(String service) {
    this.service = service;
  }

  public String getVehicle() {
    return vehicle;
  }

  public void setVehicle(String vehicle) {
    this.vehicle = vehicle;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public LocalTime getTime() {
    return time;
  }

  public void setTime(LocalTime time) {
    this.time = time;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getTechnician() {
    return technician;
  }

  public void setTechnician(String technician) {
    this.technician = technician;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  // NEW: Getters and setters for Employee relationship
  public Employee getAssignedEmployee() {
    return assignedEmployee;
  }

  public void setAssignedEmployee(Employee assignedEmployee) {
    this.assignedEmployee = assignedEmployee;
  }

  // Getters and setters for assigned User (employee from users table)
  public User getAssignedUser() {
    return assignedUser;
  }

  public void setAssignedUser(User assignedUser) {
    this.assignedUser = assignedUser;
  }

  // Helper method to get assigned user ID
  public Long getAssignedUserId() {
    return assignedUser != null ? assignedUser.getId() : null;
  }

  // Helper method to get employeeId
  public Long getEmployeeId() {
    return assignedEmployee != null ? assignedEmployee.getId() : null;
  }

  // Helper method to get employee name (for backward compatibility with frontend)
  public String getEmployeeName() {
    return assignedEmployee != null ? assignedEmployee.getName() : technician;
  }

  // NEW: Getters and setters for project tracking
  public BigDecimal getEstimatedHours() {
    return estimatedHours;
  }

  public void setEstimatedHours(BigDecimal estimatedHours) {
    this.estimatedHours = estimatedHours;
  }

  public BigDecimal getActualHours() {
    return actualHours;
  }

  public void setActualHours(BigDecimal actualHours) {
    this.actualHours = actualHours;
  }

  public List<TimeLog> getTimeLogs() {
    return timeLogs;
  }

  public void setTimeLogs(List<TimeLog> timeLogs) {
    this.timeLogs = timeLogs;
  }

  // NEW: Getters and setters for dashboard fields
  public Integer getProgress() {
    return progress;
  }

  public void setProgress(Integer progress) {
    this.progress = progress;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public String getSpecialInstructions() {
    return specialInstructions;
  }

  public void setSpecialInstructions(String specialInstructions) {
    this.specialInstructions = specialInstructions;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
