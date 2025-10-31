package com.autocare360.dto;

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
public class UserResponse {
	private Long id;
	private String email;
	private String name;
	private List<String> roles; // lower-case: admin/employee/customer
	private String status; // Active/Inactive
	private String phone; // optional
	private String employeeNo; // employees only
	private String department; // employees only
}


