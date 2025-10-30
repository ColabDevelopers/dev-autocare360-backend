package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

    private Long id;
    private String type;
    private String title;
    private String message;
    private Long userId;
    private Instant timestamp;
    private Boolean isRead;
    private Map<String, Object> data; // Additional context data
}

