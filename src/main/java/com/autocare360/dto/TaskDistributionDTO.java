package com.autocare360.dto;

public class TaskDistributionDTO {
    private String name;
    private Integer value;
    private String color;

    public TaskDistributionDTO() {}

    public TaskDistributionDTO(String name, Integer value, String color) {
        this.name = name;
        this.value = value;
        this.color = color;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}