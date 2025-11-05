package com.autocare360.controller;

import com.autocare360.dto.UserResponse;
import com.autocare360.service.CustomerService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomerController {

  private final CustomerService customerService;

  @GetMapping
  public List<UserResponse> list() {
    return customerService.listCustomers();
  }

  @GetMapping("/{id}")
  public UserResponse get(@PathVariable Long id) {
    return customerService.getCustomer(id);
  }

  @PatchMapping("/{id}")
  public UserResponse patch(@PathVariable Long id, @RequestBody Map<String, Object> body) {
    String name = body.get("name") instanceof String s ? s : null;
    String phone = body.get("phone") instanceof String s ? s : null;
    String status = body.get("status") instanceof String s ? s : null;
    return customerService.updateCustomer(id, name, phone, status);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    customerService.deleteCustomer(id);
    return ResponseEntity.noContent().build();
  }
}
