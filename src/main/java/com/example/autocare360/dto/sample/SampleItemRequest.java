package com.example.autocare360.dto.sample;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SampleItemRequest {
	@NotBlank
	@Size(max = 150)
	private String name;

	@Size(max = 1000)
	private String description;
}


