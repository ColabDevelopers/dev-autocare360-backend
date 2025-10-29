package com.autocare360.controller;

import com.autocare360.dto.*;
import com.autocare360.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
		UserResponse resp = authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(resp);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		AuthResponse resp = authService.login(request);
		return ResponseEntity.ok(resp);
	}

	@PostMapping("/refresh")
	public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
		// Simple stateless: issue a new access token if needed in future (stubbed for now)
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout() {
		return ResponseEntity.noContent().build();
	}
}


