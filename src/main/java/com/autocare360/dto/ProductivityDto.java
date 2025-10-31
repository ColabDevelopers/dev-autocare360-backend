package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductivityDto {
    private Long employeeId;
    private String name;
    private Double hoursLogged;
    private Integer tasksCompleted;
    private Double averageTaskTime;
    private Double efficiency;
}