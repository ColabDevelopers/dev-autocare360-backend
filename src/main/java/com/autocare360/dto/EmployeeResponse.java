package com.autocare360.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {
	private Long id;
	private String email;
	private String name;
	private String employeeNo;
	private String department;
	private String status;
	private List<String> roles;
	private Instant createdAt;
}


