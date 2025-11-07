package com.autocare360.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String status;
    private String specialization;
}
