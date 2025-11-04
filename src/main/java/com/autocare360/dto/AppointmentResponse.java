package com.autocare360.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class AppointmentResponse {
	private Long id;
	private String service;
	private String vehicle;
	private LocalDate date;
	private LocalTime time;
	private String status;
	private String notes;
	private String technician;
	private UserInfo user;

	@Data
	public static class UserInfo {
		private Long id;
		private String name;
		private String email;
	}
}
