package com.example.autocare360.dto;

import jakarta.validation.constraints.NotNull;

public class StartTimerRequestDTO {
    
    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;
    
    // Constructors
    public StartTimerRequestDTO() {
    }
    
    // Getters and Setters
    public Long getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
}
