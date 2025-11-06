package com.autocare360.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
  @NotBlank private String currentPassword;

  @NotBlank private String newPassword;
}
