package com.autocare360.controller;

import com.autocare360.dto.EmployeeDTO;
import com.autocare360.dto.WorkItemDTO;
import com.autocare360.service.AdminWorkloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/workload")
@RequiredArgsConstructor
public class AdminWorkloadController {

    private final AdminWorkloadService service;

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        return ResponseEntity.ok(service.getAllEmployees());
    }

    @GetMapping("/employee/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getEmployeeById(id));
    }

    @GetMapping("/availability")
    public ResponseEntity<List<EmployeeDTO>> getAvailableEmployees() {
        return ResponseEntity.ok(service.getAvailableEmployees());
    }

    @PostMapping("/assign")
    public ResponseEntity<WorkItemDTO> assignWork(@RequestBody WorkItemDTO workItemDTO) {
        return ResponseEntity.ok(service.assignWorkItem(workItemDTO));
    }
}
