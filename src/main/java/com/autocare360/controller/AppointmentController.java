package com.autocare360.controller;

import com.autocare360.dto.AppointmentRequest;
import com.autocare360.dto.AppointmentResponse;
import com.autocare360.dto.AvailabilityResponse;
import com.autocare360.security.JwtService;
import com.autocare360.service.AppointmentService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AppointmentController {

  private final AppointmentService appointmentService;
  private final JwtService jwtService;

  /** Get appointments for the authenticated user */
  @GetMapping("/appointments")
  public ResponseEntity<List<AppointmentResponse>> getMyAppointments(
      @RequestHeader(value = "Authorization", required = false) String authorization) {

    if (authorization == null || !authorization.startsWith("Bearer ")) {
      return ResponseEntity.status(401).build();
    }

    String token = authorization.substring(7);
    if (!jwtService.isTokenValid(token)) {
      return ResponseEntity.status(401).build();
    }

    Long userId = Long.valueOf(jwtService.extractSubject(token));
    List<AppointmentResponse> appointments = appointmentService.listByUser(userId);
    return ResponseEntity.ok(appointments);
  }

  /** Create a new appointment */
  @PostMapping("/appointments")
  public ResponseEntity<AppointmentResponse> createAppointment(
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @Valid @RequestBody AppointmentRequest request) {

    if (authorization == null || !authorization.startsWith("Bearer ")) {
      return ResponseEntity.status(401).build();
    }

    String token = authorization.substring(7);
    if (!jwtService.isTokenValid(token)) {
      return ResponseEntity.status(401).build();
    }

    Long userId = Long.valueOf(jwtService.extractSubject(token));
    request.setUserId(userId); // Override userId with authenticated user

    AppointmentResponse response = appointmentService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /** Update an existing appointment */
  @PutMapping("/appointments/{id}")
  public ResponseEntity<AppointmentResponse> updateAppointment(
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @PathVariable Long id,
      @Valid @RequestBody AppointmentRequest request) {

    if (authorization == null || !authorization.startsWith("Bearer ")) {
      return ResponseEntity.status(401).build();
    }

    String token = authorization.substring(7);
    if (!jwtService.isTokenValid(token)) {
      return ResponseEntity.status(401).build();
    }

    AppointmentResponse response = appointmentService.update(id, request);
    return ResponseEntity.ok(response);
  }

  /** Delete an appointment */
  @DeleteMapping("/appointments/{id}")
  public ResponseEntity<Void> deleteAppointment(
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @PathVariable Long id) {

    if (authorization == null || !authorization.startsWith("Bearer ")) {
      return ResponseEntity.status(401).build();
    }

    String token = authorization.substring(7);
    if (!jwtService.isTokenValid(token)) {
      return ResponseEntity.status(401).build();
    }

    appointmentService.delete(id);
    return ResponseEntity.noContent().build();
  }

  /** Get available time slots and technicians for a given date */
  @GetMapping("/availability")
  public ResponseEntity<AvailabilityResponse> getAvailability(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required = false) String technician) {

    AvailabilityResponse response = appointmentService.getAvailability(date, technician);
    return ResponseEntity.ok(response);
  }
}
