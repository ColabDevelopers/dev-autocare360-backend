package com.autocare360.controller;

import com.autocare360.dto.CreateEmployeeRequest;
import com.autocare360.dto.EmployeeResponse;
import com.autocare360.dto.UpdateEmployeeRequest;
import com.autocare360.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminEmployeeController {

  private final EmployeeService employeeService;

  @PostMapping
  public ResponseEntity<EmployeeResponse> create(
      @Valid @RequestBody CreateEmployeeRequest request) {
    EmployeeResponse resp = employeeService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
  }

  @GetMapping
  public List<EmployeeResponse> list() {
    return employeeService.list();
  }

  @GetMapping("/{id}")
  public EmployeeResponse get(@PathVariable Long id) {
    return employeeService.get(id);
  }

  @PutMapping("/{id}")
  public EmployeeResponse update(
      @PathVariable Long id, @Valid @RequestBody UpdateEmployeeRequest request) {
    return employeeService.update(id, request);
  }

  @PostMapping("/{id}/reset-password")
  public EmployeeResponse resetPassword(@PathVariable Long id) {
    return employeeService.resetPassword(id);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    employeeService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
