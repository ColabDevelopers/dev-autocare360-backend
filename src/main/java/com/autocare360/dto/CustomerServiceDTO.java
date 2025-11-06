package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerServiceDTO {
  // Service details
  private Long id;
  private String service;
  private String vehicle;
  private String status;
  private Integer progress;

  // Dates and times
  private String date;
  private String time;
  private String dueDate;
  private String createdAt;
  private String updatedAt;

  // Notes and instructions
  private String notes;
  private String specialInstructions;

  // Technician information
  private String technician;
  private Long technicianId;

  // Time tracking
  private Double estimatedHours;
  private Double actualHours;

  // Customer information
  private Long customerId;
  private String customerName;
  private String customerEmail;
}
