package com.example.autocare360.controller.sample;

import com.example.autocare360.dto.sample.SampleItemRequest;
import com.example.autocare360.dto.sample.SampleItemResponse;
import com.example.autocare360.service.sample.SampleItemService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sample-items")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SampleItemController {

	private final SampleItemService service;

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
	public List<SampleItemResponse> list(@RequestParam(name = "q", required = false) String search) {
		return service.list(search);
	}

	@GetMapping("/{id}")
	public SampleItemResponse getById(@PathVariable Long id) {
		return service.getById(id);
	}

	@PostMapping
	public ResponseEntity<SampleItemResponse> create(@Valid @RequestBody SampleItemRequest request) {
		SampleItemResponse created = service.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	public SampleItemResponse update(@PathVariable Long id, @Valid @RequestBody SampleItemRequest request) {
		return service.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}


