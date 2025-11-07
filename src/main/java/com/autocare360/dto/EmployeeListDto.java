package com.autocare360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeListDto {
    private Long id;
    private String name;
    private String email;
    private String department;
    private String status; // AVAILABLE, BUSY, OVERLOADED
    private Integer activeTaskCount;
    private Double capacityUtilization;
}