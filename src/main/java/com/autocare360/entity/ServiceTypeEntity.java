package com.autocare360.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "service_types")
@Data
public class ServiceTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int percentage;
}
