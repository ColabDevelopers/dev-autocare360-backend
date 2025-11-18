package com.autocare360.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "monthly_revenue")
@Data
public class MonthlyRevenue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String month;
    private double amount;
}
