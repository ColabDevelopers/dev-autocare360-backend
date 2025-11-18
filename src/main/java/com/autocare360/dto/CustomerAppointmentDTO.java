package com.autocare360.dto;

import java.math.BigDecimal;
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
public class CustomerAppointmentDTO {
  private Long id;
  private String service;
  private String vehicle;
  private LocalDate date;
  private LocalTime time;
  private String status;
  private String notes;
  private Integer progress;
  private LocalDate dueDate;
  private String specialInstructions;
  private String assignedTechnician;
  private BigDecimal estimatedHours;
  private BigDecimal actualHours;
}
