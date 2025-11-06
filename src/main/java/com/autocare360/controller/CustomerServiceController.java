package com.autocare360.controller;

import com.autocare360.dto.CustomerServiceDTO;
import com.autocare360.entity.Appointment;
import com.autocare360.entity.User;
import com.autocare360.repo.AppointmentRepository;
import com.autocare360.security.JwtService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = {"http://localhost:3000", "https://autocare360.vercel.app"})
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceController {

  private final AppointmentRepository appointmentRepository;
  private final JwtService jwtService;

  /**
   * Get all services/appointments for the authenticated customer Organized by status with full
   * details
   */
  @GetMapping("/services")
  public ResponseEntity<Map<String, Object>> getCustomerServices(Authentication authentication) {
    try {
      // Get user ID from authentication
      String email = authentication.getName();
      log.info("Fetching services for customer: {}", email);

      // Get user from appointment to extract userId
      List<Appointment> appointments =
          appointmentRepository.findAll().stream()
              .filter(
                  apt -> {
                    User user = apt.getUser();
                    return user != null && email.equals(user.getEmail());
                  })
              .collect(Collectors.toList());

      if (appointments.isEmpty()) {
        log.info("No appointments found for customer: {}", email);
        return ResponseEntity.ok(createEmptyResponse());
      }

      Long userId = appointments.get(0).getUser().getId();
      log.info("Found {} appointments for customer ID: {}", appointments.size(), userId);

      // Convert to DTOs with all details
      List<CustomerServiceDTO> serviceDTOs =
          appointments.stream().map(this::convertToDTO).collect(Collectors.toList());

      // Categorize by status
      Map<String, List<CustomerServiceDTO>> categorized = new HashMap<>();
      categorized.put("SCHEDULED", filterByStatus(serviceDTOs, "SCHEDULED", "PENDING"));
      categorized.put("IN_PROGRESS", filterByStatus(serviceDTOs, "IN_PROGRESS"));
      categorized.put("COMPLETED", filterByStatus(serviceDTOs, "COMPLETED"));
      categorized.put("CANCELLED", filterByStatus(serviceDTOs, "CANCELLED"));

      // Create response
      Map<String, Object> response = new HashMap<>();
      response.put("userId", userId);
      response.put("totalServices", serviceDTOs.size());
      response.put("allServices", serviceDTOs);
      response.put("categorized", categorized);
      response.put(
          "counts",
          Map.of(
              "scheduled", categorized.get("SCHEDULED").size(),
              "inProgress", categorized.get("IN_PROGRESS").size(),
              "completed", categorized.get("COMPLETED").size(),
              "cancelled", categorized.get("CANCELLED").size()));

      log.info("Returning {} total services categorized by status", serviceDTOs.size());
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error fetching customer services: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError()
          .body(Map.of("error", "Failed to fetch services: " + e.getMessage()));
    }
  }

  private List<CustomerServiceDTO> filterByStatus(
      List<CustomerServiceDTO> services, String... statuses) {
    return services.stream()
        .filter(
            s -> {
              String serviceStatus =
                  s.getStatus() != null ? s.getStatus().toUpperCase() : "SCHEDULED";
              for (String status : statuses) {
                if (status.equalsIgnoreCase(serviceStatus)) {
                  return true;
                }
              }
              return false;
            })
        .collect(Collectors.toList());
  }

  private Map<String, Object> createEmptyResponse() {
    Map<String, Object> response = new HashMap<>();
    response.put("userId", null);
    response.put("totalServices", 0);
    response.put("allServices", new ArrayList<>());
    response.put(
        "categorized",
        Map.of(
            "SCHEDULED", new ArrayList<>(),
            "IN_PROGRESS", new ArrayList<>(),
            "COMPLETED", new ArrayList<>(),
            "CANCELLED", new ArrayList<>()));
    response.put(
        "counts",
        Map.of(
            "scheduled", 0,
            "inProgress", 0,
            "completed", 0,
            "cancelled", 0));
    return response;
  }

  private CustomerServiceDTO convertToDTO(Appointment appointment) {
    CustomerServiceDTO dto = new CustomerServiceDTO();

    // Basic info
    dto.setId(appointment.getId());
    dto.setService(appointment.getService());
    dto.setVehicle(appointment.getVehicle());
    dto.setStatus(appointment.getStatus() != null ? appointment.getStatus() : "SCHEDULED");
    dto.setProgress(appointment.getProgress() != null ? appointment.getProgress() : 0);

    // Date and time
    dto.setDate(appointment.getDate() != null ? appointment.getDate().toString() : null);
    dto.setTime(appointment.getTime() != null ? appointment.getTime().toString() : null);
    dto.setDueDate(appointment.getDueDate() != null ? appointment.getDueDate().toString() : null);
    dto.setCreatedAt(
        appointment.getCreatedAt() != null ? appointment.getCreatedAt().toString() : null);
    dto.setUpdatedAt(
        appointment.getUpdatedAt() != null ? appointment.getUpdatedAt().toString() : null);

    // Notes and instructions
    dto.setNotes(appointment.getNotes());
    dto.setSpecialInstructions(appointment.getSpecialInstructions());

    // Technician info
    if (appointment.getAssignedEmployee() != null) {
      dto.setTechnician(appointment.getAssignedEmployee().getName());
      dto.setTechnicianId(appointment.getAssignedEmployee().getId());
    } else if (appointment.getTechnician() != null) {
      dto.setTechnician(appointment.getTechnician());
    }

    // Hours tracking
    if (appointment.getEstimatedHours() != null) {
      dto.setEstimatedHours(appointment.getEstimatedHours().doubleValue());
    }
    if (appointment.getActualHours() != null) {
      dto.setActualHours(appointment.getActualHours().doubleValue());
    }

    // Customer info
    if (appointment.getUser() != null) {
      dto.setCustomerId(appointment.getUser().getId());
      dto.setCustomerName(appointment.getUser().getName());
      dto.setCustomerEmail(appointment.getUser().getEmail());
    }

    return dto;
  }
}
