package com.autocare360.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
