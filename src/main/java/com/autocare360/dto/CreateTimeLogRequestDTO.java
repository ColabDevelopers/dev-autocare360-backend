package com.autocare360.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateTimeLogRequestDTO {

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @NotNull(message = "Hours is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Hours must be greater than 0")
    private BigDecimal hours;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private String status; // Optional, defaults to COMPLETED

    // Constructors
    public CreateTimeLogRequestDTO() {
    }

    // Getters and Setters
    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}