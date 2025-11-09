package com.autocare360.controller;

import com.autocare360.dto.EmployeeResponse;
import com.autocare360.service.EmployeeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

  private final EmployeeService employeeService;

  /**
   * List all employees - accessible by all authenticated users This endpoint is used by customers
   * to select technicians when booking appointments
   */
  @GetMapping
  public List<EmployeeResponse> list() {
    return employeeService.list();
  }
}
