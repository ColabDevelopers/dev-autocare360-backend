package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
  private Long userId;
  private String name;
  private String role;
  private String lastMessage;
  private String time;
  private Long unreadCount;
  private String avatar;
}
