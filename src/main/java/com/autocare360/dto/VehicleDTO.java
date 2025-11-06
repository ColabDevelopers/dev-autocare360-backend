package com.autocare360.dto;

public class VehicleDTO {
  private Long id;
  private String vin;
  private String make;
  private String model;
  private int year;
  private String plateNumber;
  private String color;

  // Constructor
  public VehicleDTO(
      Long id, String vin, String make, String model, int year, String plateNumber, String color) {
    this.id = id;
    this.vin = vin;
    this.make = make;
    this.model = model;
    this.year = year;
    this.plateNumber = plateNumber;
    this.color = color;
  }

  // Getters only
  public Long getId() {
    return id;
  }

  public String getVin() {
    return vin;
  }

  public String getMake() {
    return make;
  }

  public String getModel() {
    return model;
  }

  public int getYear() {
    return year;
  }

  public String getPlateNumber() {
    return plateNumber;
  }

  public String getColor() {
    return color;
  }
}
