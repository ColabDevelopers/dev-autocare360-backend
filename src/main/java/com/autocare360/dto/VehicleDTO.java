package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDTO {
  private Long id;
  private String vin;
  private String make;
  private String model;
  private Integer year;
  private String plateNumber;
  private String color;
}
