package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String senderName;
    private String senderRole;
    private String message;
    private LocalDateTime createdAt;
    private Boolean isRead;
}
