package com.autocare360.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEmployeeRequest {
  @NotBlank private String name;

  @NotBlank private String department;

  @NotBlank private String status; // ACTIVE / INACTIVE
}
