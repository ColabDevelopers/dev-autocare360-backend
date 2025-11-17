package com.autocare360.controller;

import com.autocare360.dto.CustomerDashboardDTO;
import com.autocare360.dto.VehicleDTO;
import com.autocare360.entity.User;
import com.autocare360.repo.UserRepository;
import com.autocare360.service.CustomerDashboardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
