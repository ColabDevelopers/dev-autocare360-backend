package com.autocare360.controller;

import com.autocare360.dto.CustomerDashboardDTO;
import com.autocare360.dto.VehicleDTO;
import com.autocare360.dto.CustomerAppointmentDTO;
import com.autocare360.entity.User;
import com.autocare360.repo.UserRepository;
import com.autocare360.service.CustomerDashboardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerDashboardController {

  private final CustomerDashboardService customerDashboardService;
  private final UserRepository userRepository;

  @GetMapping("/dashboard")
  public ResponseEntity<CustomerDashboardDTO> getDashboard(Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    CustomerDashboardDTO dashboard = customerDashboardService.getDashboardData(userId);
    return ResponseEntity.ok(dashboard);
  }

  @GetMapping("/vehicles")
  public ResponseEntity<List<VehicleDTO>> getVehicles(Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    List<VehicleDTO> vehicles = customerDashboardService.getCustomerVehicles(userId);
    return ResponseEntity.ok(vehicles);
  }

  @GetMapping("/appointments")
  public ResponseEntity<List<CustomerAppointmentDTO>> getAppointments(
      Authentication authentication,
      @RequestParam(required = false) String status) {
    Long userId = getUserIdFromAuth(authentication);
    List<CustomerAppointmentDTO> appointments = customerDashboardService.getCustomerAppointments(userId, status);
    return ResponseEntity.ok(appointments);
  }

  @GetMapping("/appointments/active")
  public ResponseEntity<List<CustomerAppointmentDTO>> getActiveAppointments(Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    List<CustomerAppointmentDTO> appointments = customerDashboardService.getActiveAppointments(userId);
    return ResponseEntity.ok(appointments);
  }

  @GetMapping("/appointments/history")
  public ResponseEntity<List<CustomerAppointmentDTO>> getServiceHistory(Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    List<CustomerAppointmentDTO> history = customerDashboardService.getServiceHistory(userId);
    return ResponseEntity.ok(history);
  }

  @GetMapping("/appointments/search")
  public ResponseEntity<List<CustomerAppointmentDTO>> searchAppointments(
      Authentication authentication,
      @RequestParam String query) {
    Long userId = getUserIdFromAuth(authentication);
    List<CustomerAppointmentDTO> results = customerDashboardService.searchAppointments(userId, query);
    return ResponseEntity.ok(results);
  }

  private Long getUserIdFromAuth(Authentication authentication) {
    // The authentication name is the email set by JwtAuthFilter
    String email = authentication.getName();
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    return user.getId();
  }
}
