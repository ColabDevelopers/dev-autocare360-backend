package com.autocare360.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class AppointmentRequest {
	private Long userId;
	private String service;
	private String vehicle;
	private LocalDate date;
	private LocalTime time;
	private String status;
	private String notes;
	private String technician;
}
