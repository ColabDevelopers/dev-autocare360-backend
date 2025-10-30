package com.autocare360.dto;

import java.time.LocalDateTime;

public class JobActionResponseDTO {
    private Long jobId;
    private String status;
    private String message;
    private LocalDateTime startedAt;
    
    public JobActionResponseDTO() {}
    
    // Getters and Setters
    public Long getJobId() {
        return jobId;
    }
    
    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }
}
