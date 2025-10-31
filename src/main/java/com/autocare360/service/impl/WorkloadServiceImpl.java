package com.autocare360.service.impl;

import com.autocare360.dto.*;
import com.autocare360.model.Employee;
import com.autocare360.model.JobAssignment;
import com.autocare360.model.WorkItem;
import com.autocare360.repo.EmployeeRepository;
import com.autocare360.repo.JobAssignmentRepository;
import com.autocare360.repo.TimeLogRepository;
import com.autocare360.repo.WorkItemRepository;
import com.autocare360.service.WorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadServiceImpl implements WorkloadService {
    
    private final EmployeeRepository employeeRepository;
    private final TimeLogRepository timeLogRepository;
    private final JobAssignmentRepository jobAssignmentRepository;
    private final WorkItemRepository workItemRepository;
    
    // Standard work hours per week (40 hours)
    private static final double STANDARD_WEEKLY_HOURS = 40.0;
    private static final double STANDARD_MONTHLY_HOURS = 160.0;
    
    @Override
    public List<WorkloadResponse> getAllEmployeeWorkloads() {
        log.info("Fetching workload for all employees");
        
        List<Employee> employees = employeeRepository.findAll();
        
        return employees.stream()
            .map(this::buildWorkloadResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public WorkloadResponse getEmployeeWorkload(Long employeeId) {
        log.info("Fetching workload for employee ID: {}", employeeId);
        
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));
        
        return buildWorkloadResponse(employee);
    }
    
    @Override
    public CapacityMetrics getCapacityMetrics() {
        log.info("Calculating capacity metrics");
        
        List<WorkloadResponse> allWorkloads = getAllEmployeeWorkloads();
        
        int totalEmployees = allWorkloads.size();
        int availableEmployees = (int) allWorkloads.stream()
            .filter(w -> "available".equals(w.getStatus()))
            .count();
        int busyEmployees = (int) allWorkloads.stream()
            .filter(w -> "busy".equals(w.getStatus()))
            .count();
        int overloadedEmployees = (int) allWorkloads.stream()
            .filter(w -> "overloaded".equals(w.getStatus()))
            .count();
        
        double averageCapacity = allWorkloads.stream()
            .mapToDouble(WorkloadResponse::getCapacityUtilization)
            .average()
            .orElse(0.0);
        
        Long totalActiveWorkItems = workItemRepository.countActiveWorkItems();
        
        return CapacityMetrics.builder()
            .totalEmployees(totalEmployees)
            .availableEmployees(availableEmployees)
            .busyEmployees(busyEmployees)
            .overloadedEmployees(overloadedEmployees)
            .averageCapacity(Math.round(averageCapacity * 100.0) / 100.0)
            .totalActiveWorkItems(totalActiveWorkItems.intValue())
            .build();
    }
    
    @Override
    @Transactional
    public void assignTask(AssignTaskRequest request) {
        log.info("Assigning work item {} to employee {}", 
            request.getWorkItemId(), request.getEmployeeId());
        
        // Verify work item exists
        WorkItem workItem = workItemRepository.findById(request.getWorkItemId())
            .orElseThrow(() -> new RuntimeException("Work item not found"));
        
        // Verify employee exists
        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        // Check if already assigned
        JobAssignment existing = jobAssignmentRepository.findActiveByWorkItemIdAndEmployeeId(
            request.getWorkItemId(), request.getEmployeeId());
        
        if (existing != null) {
            throw new RuntimeException("Work item already assigned to this employee");
        }
        
        // Create new assignment
        JobAssignment assignment = new JobAssignment();
        assignment.setWorkItemId(request.getWorkItemId());
        assignment.setEmployeeId(request.getEmployeeId());
        assignment.setRoleOnJob(request.getRoleOnJob() != null ? request.getRoleOnJob() : "Technician");
        assignment.setActive(true);
        assignment.setAssignedAt(LocalDateTime.now());
        
        jobAssignmentRepository.save(assignment);
        
        log.info("Successfully assigned work item {} to employee {}", 
            request.getWorkItemId(), request.getEmployeeId());
    }
    
    @Override
    public List<WorkloadResponse> getEmployeeAvailability() {
        // Return employees sorted by capacity (most available first)
        return getAllEmployeeWorkloads().stream()
            .sorted((a, b) -> Double.compare(a.getCapacityUtilization(), b.getCapacityUtilization()))
            .collect(Collectors.toList());
    }
    
    // Helper method to build WorkloadResponse for an employee
    private WorkloadResponse buildWorkloadResponse(Employee employee) {
        Long employeeId = employee.getId();
        
        // Get active assignments
        List<JobAssignment> activeAssignments = jobAssignmentRepository.findActiveByEmployeeId(employeeId);
        
        // Count appointments and projects
        int activeAppointments = 0;
        int activeProjects = 0;
        List<TaskDto> upcomingTasks = new ArrayList<>();
        
        for (JobAssignment assignment : activeAssignments) {
            WorkItem workItem = workItemRepository.findById(assignment.getWorkItemId()).orElse(null);
            if (workItem != null && !"completed".equalsIgnoreCase(workItem.getStatus())) {
                if ("appointment".equalsIgnoreCase(workItem.getType())) {
                    activeAppointments++;
                } else if ("project".equalsIgnoreCase(workItem.getType())) {
                    activeProjects++;
                }
                
                // Build task DTO
                TaskDto task = TaskDto.builder()
                    .id(assignment.getId())
                    .workItemId(workItem.getId())
                    .title(workItem.getTitle())
                    .type(workItem.getType())
                    .scheduledDate(workItem.getCreatedAt().toString())
                    .estimatedHours(10.0) // Default, calculate from service catalog if available
                    .status(workItem.getStatus())
                    .customerName("Customer") // Get from customer profile if available
                    .build();
                
                upcomingTasks.add(task);
            }
        }
        
        // Calculate hours logged this week
        LocalDateTime startOfWeek = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime endOfWeek = startOfWeek.plusDays(7);
        
        Double minutesThisWeek = timeLogRepository.sumMinutesByEmployeeIdAndDateRange(
            employeeId, startOfWeek, endOfWeek);
        double hoursThisWeek = (minutesThisWeek != null ? minutesThisWeek : 0.0) / 60.0;
        
        // Calculate hours logged this month
        LocalDateTime startOfMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        
        Double minutesThisMonth = timeLogRepository.sumMinutesByEmployeeIdAndDateRange(
            employeeId, startOfMonth, endOfMonth);
        double hoursThisMonth = (minutesThisMonth != null ? minutesThisMonth : 0.0) / 60.0;
        
        // Calculate capacity utilization (based on weekly hours)
        double capacityUtilization = (hoursThisWeek / STANDARD_WEEKLY_HOURS) * 100.0;
        capacityUtilization = Math.min(capacityUtilization, 100.0); // Cap at 100%
        
        // Determine status
        String status;
        if (capacityUtilization < 60) {
            status = "available";
        } else if (capacityUtilization < 90) {
            status = "busy";
        } else {
            status = "overloaded";
        }
        
        return WorkloadResponse.builder()
            .employeeId(employeeId)
            .name(employee.getName())
            .email(employee.getEmail())
            .department(employee.getDepartment() != null ? employee.getDepartment() : "General")
            .activeAppointments(activeAppointments)
            .activeProjects(activeProjects)
            .hoursLoggedThisWeek(Math.round(hoursThisWeek * 100.0) / 100.0)
            .hoursLoggedThisMonth(Math.round(hoursThisMonth * 100.0) / 100.0)
            .capacityUtilization(Math.round(capacityUtilization * 100.0) / 100.0)
            .status(status)
            .upcomingTasks(upcomingTasks)
            .build();
    }
}