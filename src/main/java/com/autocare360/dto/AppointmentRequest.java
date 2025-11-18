package com.autocare360.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {
  private Long userId;
  private String service;
  private String vehicle;
  private LocalDate date;
  private LocalTime time;
  private String status;
  private String notes;
  private String technician;
}
