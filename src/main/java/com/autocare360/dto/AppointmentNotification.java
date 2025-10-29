package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentNotification {

    private Long appointmentId;
    private String appointmentDate;
    private String appointmentTime;
    private String serviceType;
    private String vehicleNumber;
    private String status;
    private String message;
    private Long customerId;
}

