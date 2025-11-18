package com.autocare360.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
  @NotBlank private String name;

  @Email @NotBlank private String email;

  @NotBlank
  @Size(min = 6)
  private String password;

  @NotBlank private String phone;

  @NotBlank private String vehicleMake;

  @NotBlank private String vehicleModel;

  @NotBlank private String vehicleYear;
}
