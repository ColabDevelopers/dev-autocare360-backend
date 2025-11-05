package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProgressUpdate {

  private Long serviceId;
  private String vehicleNumber;
  private String serviceName;
  private String status;
  private Integer progressPercentage;
  private String currentStage;
  private String message;
  private Long customerId;
  private String estimatedCompletion;
}
