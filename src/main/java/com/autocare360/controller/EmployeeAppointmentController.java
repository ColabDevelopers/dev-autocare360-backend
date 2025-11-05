package com.autocare360.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autocare360.dto.AppointmentRequest;
import com.autocare360.dto.AppointmentResponse;
import com.autocare360.entity.Employee;
import com.autocare360.entity.User;
import com.autocare360.repo.EmployeeRepository;
import com.autocare360.repo.UserRepository;
import com.autocare360.security.JwtService;
import com.autocare360.service.AppointmentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/employee/appointments")
@RequiredArgsConstructor
public class EmployeeAppointmentController {

    private final AppointmentService appointmentService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;

    /**
     * Get appointments for the logged-in employee
     * Only returns CONFIRMED and IN_PROGRESS appointments
     */
    @GetMapping
    public ResponseEntity<?> getMyAppointments(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(java.util.Map.of(
                "error", "Unauthorized",
                "message", "No authorization header provided"
            ));
        }

        String token = authorization.substring(7);
        if (!jwtService.isTokenValid(token)) {
            return ResponseEntity.status(401).body(java.util.Map.of(
                "error", "Unauthorized",
                "message", "Invalid or expired token"
            ));
        }

        Long userId = Long.valueOf(jwtService.extractSubject(token));
        
        // Verify user has EMPLOYEE role
        boolean isEmployee = jwtService.hasRole(authorization, "EMPLOYEE");
        if (!isEmployee) {
            return ResponseEntity.status(403).body(java.util.Map.of(
                "error", "Forbidden",
                "message", "Access denied. Employee role required."
            ));
        }

        // Get user to find their employee record
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(java.util.Map.of(
                "error", "Not Found",
                "message", "User not found with ID: " + userId
            ));
        }

        // Find employee record by email
        Employee employee = employeeRepository.findByEmail(user.getEmail());
        if (employee == null) {
            return ResponseEntity.status(404).body(java.util.Map.of(
                "error", "Not Found",
                "message", "Employee record not found for email: " + user.getEmail() + ". Please ensure an employee record exists in the employees table."
            ));
        }

        // Get only CONFIRMED and IN_PROGRESS appointments for this employee
        List<String> allowedStatuses = Arrays.asList("CONFIRMED", "IN_PROGRESS");
        List<AppointmentResponse> appointments = appointmentService.listByEmployeeAndStatus(
            employee.getId(), 
            allowedStatuses
        );

        return ResponseEntity.ok(appointments);
    }

    /**
     * Start service for an appointment (update status to IN_PROGRESS)
     * Only allows updating CONFIRMED appointments to IN_PROGRESS
     */
    @PutMapping("/{id}/start")
    public ResponseEntity<?> startService(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(java.util.Map.of(
                "error", "Unauthorized",
                "message", "No authorization header provided"
            ));
        }

        String token = authorization.substring(7);
        if (!jwtService.isTokenValid(token)) {
            return ResponseEntity.status(401).body(java.util.Map.of(
                "error", "Unauthorized",
                "message", "Invalid or expired token"
            ));
        }

        // Verify user has EMPLOYEE role
        boolean isEmployee = jwtService.hasRole(authorization, "EMPLOYEE");
        if (!isEmployee) {
            return ResponseEntity.status(403).body(java.util.Map.of(
                "error", "Forbidden",
                "message", "Access denied. Employee role required."
            ));
        }

        try {
            // Create request to update status to IN_PROGRESS
            AppointmentRequest request = new AppointmentRequest();
            request.setStatus("IN_PROGRESS");
            
            AppointmentResponse response = appointmentService.update(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(java.util.Map.of(
                "error", "Bad Request",
                "message", "Failed to start service: " + e.getMessage()
            ));
        }
    }
}

