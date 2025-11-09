package com.autocare360.controller;

import com.autocare360.dto.AppointmentRequest;
import com.autocare360.dto.AppointmentResponse;
import com.autocare360.service.AppointmentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/appointments")
@RequiredArgsConstructor
public class AdminAppointmentController {

  private final AppointmentService appointmentService;

  @GetMapping
  public ResponseEntity<List<AppointmentResponse>> listAll() {
    return ResponseEntity.ok(appointmentService.listAll());
  }

  @PostMapping
  public ResponseEntity<AppointmentResponse> createAppointment(
      @RequestBody AppointmentRequest request) {
    AppointmentResponse resp = appointmentService.create(request);
    return ResponseEntity.ok(resp);
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<AppointmentResponse> updateStatus(
      @PathVariable Long id, @RequestBody AppointmentRequest request) {
    // Only update status field is required - appointmentService.update handles partial updates
    AppointmentResponse resp = appointmentService.update(id, request);
    return ResponseEntity.ok(resp);
  }
}
