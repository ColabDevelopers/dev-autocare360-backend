package com.autocare360.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerformanceDTO {
    private String metric;
    private int value;
}
