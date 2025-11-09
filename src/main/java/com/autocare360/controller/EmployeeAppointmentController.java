package com.autocare360.controller;

import com.autocare360.dto.AppointmentRequest;
import com.autocare360.dto.AppointmentResponse;
import com.autocare360.entity.User;
import com.autocare360.repo.UserRepository;
import com.autocare360.security.JwtService;
import com.autocare360.service.AppointmentService;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee/appointments")
@RequiredArgsConstructor
public class EmployeeAppointmentController {

  private final AppointmentService appointmentService;
  private final JwtService jwtService;
  private final UserRepository userRepository;

  /**
   * Get appointments for the logged-in employee Only returns CONFIRMED and IN_PROGRESS appointments
   */
  @GetMapping
  public ResponseEntity<?> getMyAppointments(
      @RequestHeader(value = "Authorization", required = false) String authorization) {

    if (authorization == null || !authorization.startsWith("Bearer ")) {
      return ResponseEntity.status(401)
          .body(
              java.util.Map.of(
                  "error", "Unauthorized",
                  "message", "No authorization header provided"));
    }

    String token = authorization.substring(7);
    if (!jwtService.isTokenValid(token)) {
      return ResponseEntity.status(401)
          .body(
              java.util.Map.of(
                  "error", "Unauthorized",
                  "message", "Invalid or expired token"));
    }

    Long userId = Long.valueOf(jwtService.extractSubject(token));

    // Verify user has EMPLOYEE role
    boolean isEmployee = jwtService.hasRole(authorization, "EMPLOYEE");
    if (!isEmployee) {
      return ResponseEntity.status(403)
          .body(
              java.util.Map.of(
                  "error", "Forbidden",
                  "message", "Access denied. Employee role required."));
    }

    // Get user to verify they are an employee
    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
      return ResponseEntity.status(404)
          .body(
              java.util.Map.of(
                  "error", "Not Found", "message", "User not found with ID: " + userId));
    }

    // Check if user has employee_no (employees only field)
    if (user.getEmployeeNo() == null || user.getEmployeeNo().isEmpty()) {
      return ResponseEntity.status(404)
          .body(
              java.util.Map.of(
                  "error",
                  "Not Found",
                  "message",
                  "Employee number not found for user: "
                      + user.getEmail()
                      + ". Please ensure the user has an employee_no assigned in the users table."));
    }

    // Get appointments for today with all active statuses.
    // Include CONFIRMED (used in DB) as well as APPROVED/PENDING/IN_PROGRESS/COMPLETED
    // We intentionally include all common status values so employees see today's jobs regardless of
    // small naming differences.
    List<String> allowedStatuses =
        Arrays.asList("PENDING", "APPROVED", "CONFIRMED", "IN_PROGRESS", "COMPLETED");
    List<AppointmentResponse> appointments =
        appointmentService.listByEmployeeAndStatus(user.getId(), allowedStatuses);

    return ResponseEntity.ok(appointments);
  }

  /**
   * Start service for an appointment (update status to IN_PROGRESS) Allows updating PENDING or
   * APPROVED appointments to IN_PROGRESS
   */
  @PutMapping("/{id}/start")
  public ResponseEntity<?> startService(
      @PathVariable Long id,
      @RequestHeader(value = "Authorization", required = false) String authorization) {

    if (authorization == null || !authorization.startsWith("Bearer ")) {
      return ResponseEntity.status(401)
          .body(
              java.util.Map.of(
                  "error", "Unauthorized",
                  "message", "No authorization header provided"));
    }

    String token = authorization.substring(7);
    if (!jwtService.isTokenValid(token)) {
      return ResponseEntity.status(401)
          .body(
              java.util.Map.of(
                  "error", "Unauthorized",
                  "message", "Invalid or expired token"));
    }

    // Verify user has EMPLOYEE role
    boolean isEmployee = jwtService.hasRole(authorization, "EMPLOYEE");
    if (!isEmployee) {
      return ResponseEntity.status(403)
          .body(
              java.util.Map.of(
                  "error", "Forbidden",
                  "message", "Access denied. Employee role required."));
    }

    try {
      // Create request to update status to IN_PROGRESS
      AppointmentRequest request = new AppointmentRequest();
      request.setStatus("IN_PROGRESS");

      AppointmentResponse response = appointmentService.update(id, request);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(400)
          .body(
              java.util.Map.of(
                  "error", "Bad Request", "message", "Failed to start service: " + e.getMessage()));
    }
  }
}
