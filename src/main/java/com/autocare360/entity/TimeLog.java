package com.autocare360.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_logs")
public class TimeLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employee_id", nullable = false)
  @JsonIgnore
  private Employee employee;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "appointment_id", nullable = false)
  @JsonIgnore
  private Appointment appointment;

  @Column(nullable = false)
  private LocalDate date;

  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal hours; // e.g., 2.50 hours

  @Column(nullable = false, length = 2000)
  private String description;

  @Column(nullable = false, length = 20)
  private String status = "COMPLETED"; // COMPLETED, IN_PROGRESS, PENDING_APPROVAL

  @Column(name = "is_billable")
  private Boolean isBillable = true;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // Constructors
  public TimeLog() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public TimeLog(
      Employee employee,
      Appointment appointment,
      LocalDate date,
      BigDecimal hours,
      String description) {
    this();
    this.employee = employee;
    this.appointment = appointment;
    this.date = date;
    this.hours = hours;
    this.description = description;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Employee getEmployee() {
    return employee;
  }

  public void setEmployee(Employee employee) {
    this.employee = employee;
  }

  // Helper method to get employeeId
  public Long getEmployeeId() {
    return employee != null ? employee.getId() : null;
  }

  // Helper method to get employee name
  public String getEmployeeName() {
    return employee != null ? employee.getName() : null;
  }

  public Appointment getAppointment() {
    return appointment;
  }

  public void setAppointment(Appointment appointment) {
    this.appointment = appointment;
  }

  // Helper method to get appointmentId
  public Long getAppointmentId() {
    return appointment != null ? appointment.getId() : null;
  }

  // Helper method to get appointment/project name
  public String getProjectName() {
    return appointment != null ? appointment.getService() + " - " + appointment.getVehicle() : null;
  }

  // Helper method to get customer name
  public String getCustomerName() {
    return appointment != null && appointment.getUser() != null
        ? appointment.getUser().getName()
        : null;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public BigDecimal getHours() {
    return hours;
  }

  public void setHours(BigDecimal hours) {
    this.hours = hours;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Boolean getIsBillable() {
    return isBillable;
  }

  public void setIsBillable(Boolean isBillable) {
    this.isBillable = isBillable;
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

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
