package com.autocare360.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "performance_metrics")
@Data
public class PerformanceMetricsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String metric;
    private int value;
}
