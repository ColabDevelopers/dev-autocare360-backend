package com.autocare360.dto;

public class ActiveProjectDTO {

  private Long id;
  private String name;
  private Long customerId;
  private String customer;

  // Constructors
  public ActiveProjectDTO() {}

  public ActiveProjectDTO(Long id, String name, Long customerId, String customer) {
    this.id = id;
    this.name = name;
    this.customerId = customerId;
    this.customer = customer;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Long customerId) {
    this.customerId = customerId;
  }

  public String getCustomer() {
    return customer;
  }

  public void setCustomer(String customer) {
    this.customer = customer;
  }
}
