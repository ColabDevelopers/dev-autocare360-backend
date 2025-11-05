package com.autocare360.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

  private Long id;
  private String type;
  private String title;
  private String message;
  private Boolean isRead;
  private Instant createdAt;
  private String data; // JSON string with additional data
}
