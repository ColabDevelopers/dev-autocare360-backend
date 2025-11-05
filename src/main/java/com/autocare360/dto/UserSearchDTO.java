package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchDTO {
  private Long id;
  private String name;
  private String email;
  private String role;
}
