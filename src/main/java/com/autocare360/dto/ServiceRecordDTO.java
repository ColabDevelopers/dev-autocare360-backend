package com.autocare360.dto;

import java.time.LocalDateTime;

public class ServiceRecordDTO {

    private Long id;
    private Long vehicleId;
    private String type;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime scheduledAt;
    private String notes;
    private String attachments;

    // Constructor
    public ServiceRecordDTO(Long id, Long vehicleId, String type, String status,
                            LocalDateTime requestedAt, LocalDateTime scheduledAt,
                            String notes, String attachments) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.type = type;
        this.status = status;
        this.requestedAt = requestedAt;
        this.scheduledAt = scheduledAt;
        this.notes = notes;
        this.attachments = attachments;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
}
