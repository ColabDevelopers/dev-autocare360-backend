package com.autocare360.service;

import com.autocare360.dto.EmployeeDTO;
import com.autocare360.dto.WorkItemDTO;
import com.autocare360.entity.Employee;
import com.autocare360.entity.WorkItem;
import com.autocare360.repo.EmployeeRepository;
import com.autocare360.repo.WorkItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminWorkloadService {

    private final EmployeeRepository employeeRepository;
    private final WorkItemRepository workItemRepository;

    // Get all employees
    public List<EmployeeDTO> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(e -> EmployeeDTO.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .email(e.getEmail())
                        .phoneNumber(e.getPhoneNumber())
                        .status(e.getStatus())
                        .specialization(e.getSpecialization())
                        .build())
                .collect(Collectors.toList());
    }

    // Get single employee by ID
    public EmployeeDTO getEmployeeById(Long id) {
        Employee e = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return EmployeeDTO.builder()
                .id(e.getId())
                .name(e.getName())
                .email(e.getEmail())
                .phoneNumber(e.getPhoneNumber())
                .status(e.getStatus())
                .specialization(e.getSpecialization())
                .build();
    }

    // Get only active employees
    public List<EmployeeDTO> getAvailableEmployees() {
        return employeeRepository.findByStatus("ACTIVE").stream()
                .map(e -> EmployeeDTO.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .email(e.getEmail())
                        .phoneNumber(e.getPhoneNumber())
                        .status(e.getStatus())
                        .specialization(e.getSpecialization())
                        .build())
                .collect(Collectors.toList());
    }

    // Assign a work item to employee
    public WorkItemDTO assignWorkItem(WorkItemDTO workItemDTO) {
        Employee employee = employeeRepository.findById(workItemDTO.getAssignedEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        WorkItem workItem = WorkItem.builder()
                .title(workItemDTO.getTitle())
                .description(workItemDTO.getDescription())
                .startTime(workItemDTO.getStartTime())
                .endTime(workItemDTO.getEndTime())
                .assignedEmployee(employee)
                .build();

        WorkItem saved = workItemRepository.save(workItem);
        return WorkItemDTO.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .startTime(saved.getStartTime())
                .endTime(saved.getEndTime())
                .assignedEmployeeId(saved.getAssignedEmployee().getId())
                .build();
    }
}
