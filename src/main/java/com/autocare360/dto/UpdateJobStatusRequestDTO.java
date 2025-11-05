package com.autocare360.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateJobStatusRequestDTO {

  @NotNull(message = "Status is required")
  private String status;

  @Min(0)
  @Max(100)
  private Integer progress;

  private String notes;

  public UpdateJobStatusRequestDTO() {}

  // Getters and Setters
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Integer getProgress() {
    return progress;
  }

  public void setProgress(Integer progress) {
    this.progress = progress;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
