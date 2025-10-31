package com.autocare360.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StopTimerRequestDTO {
    
    @NotNull(message = "Timer ID is required")
    private Long timerId;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    // Constructors
    public StopTimerRequestDTO() {
    }
    
    // Getters and Setters
    public Long getTimerId() {
        return timerId;
    }
    
    public void setTimerId(Long timerId) {
        this.timerId = timerId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
