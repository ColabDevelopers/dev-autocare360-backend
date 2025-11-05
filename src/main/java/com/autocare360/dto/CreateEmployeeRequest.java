package com.autocare360.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateEmployeeRequest {
  @NotBlank private String name;

  @Email @NotBlank private String email;

  @NotBlank private String department;
}
