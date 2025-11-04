package com.autocare360.dto;

import java.util.List;

import lombok.Data;

@Data
public class AvailabilityResponse {
	private List<String> timeSlots;
	private List<String> availableTechnicians;
}
