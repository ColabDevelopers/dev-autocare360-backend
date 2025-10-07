package com.example.autocare360.dto.sample;

import java.time.Instant;
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
public class SampleItemResponse {
	private Long id;
	private String name;
	private String description;
	private Instant createdAt;
}


