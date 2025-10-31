package com.autocare360.dto;

import java.time.LocalDateTime;

public record ServiceRecordDTO(
        Long id,
        Long vehicleId,
        String name,              // Service name (e.g., "Oil Change")
        String type,              // Category (e.g., "maintenance", "safety", "diagnostics")
        String status,
        LocalDateTime requestedAt,
        LocalDateTime scheduledAt,
        String notes,             // Description
        String attachments,       // JSON string of attachments
        Double price,
        Double duration,          // Duration in hours
        Integer durationMinutes   // Duration in minutes for UI convenience
) {
    // Convenience constructor that calculates durationMinutes
    public ServiceRecordDTO(
            Long id,
            Long vehicleId,
            String name,
            String type,
            String status,
            LocalDateTime requestedAt,
            LocalDateTime scheduledAt,
            String notes,
            String attachments,
            Double price,
            Double duration
    ) {
        this(
                id,
                vehicleId,
                name,
                type,
                status,
                requestedAt,
                scheduledAt,
                notes,
                attachments,
                price,
                duration,
                duration != null ? (int) Math.round(duration * 60) : null
        );
    }
}