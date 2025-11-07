package com.autocare360.controller;

import com.autocare360.dto.*;
import com.autocare360.service.TaskAssignmentService;
import com.autocare360.service.EmployeeScheduleService;
import com.autocare360.service.WorkloadMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.autocare360.dto.WorkloadResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/workload")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = {"https://autocare360.vercel.app", "http://localhost:3000", "http://localhost:5173"})
public class WorkloadMonitoringController {

    private final WorkloadMonitoringService workloadMonitoringService;
    private final TaskAssignmentService taskAssignmentService;
    private final EmployeeScheduleService employeeScheduleService;

    /**
     * Dashboard Summary - Top metrics for Image 1
     * GET /api/admin/workload/dashboard-summary
     */
    @GetMapping("/dashboard-summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        List<WorkloadResponse> workloads = workloadMonitoringService.getAllEmployeeWorkloads();
        
        Integer totalEmployees = workloads.size();
        Double averageCapacity = workloads.stream()
                .mapToDouble(WorkloadResponse::getCapacityUtilization)
                .average()
                .orElse(0.0);
        
        Integer activeWorkItems = workloads.stream()
                .mapToInt(WorkloadResponse::getActiveAppointments)
                .sum();
        
        Double utilizationRate = workloads.stream()
                .filter(w -> w.getHoursLoggedThisWeek() != null)
                .mapToDouble(WorkloadResponse::getHoursLoggedThisWeek)
                .average()
                .orElse(0.0);

        DashboardSummaryDto summary = DashboardSummaryDto.builder()
                .totalEmployees(totalEmployees)
                .averageCapacity(Math.round(averageCapacity * 100.0) / 100.0)
                .activeWorkItems(activeWorkItems)
                .utilizationRate(Math.round(utilizationRate * 100.0) / 100.0)
                .build();

        return ResponseEntity.ok(summary);
    }

    /**
     * Get unassigned tasks - For task assignment section (Image 1)
     * GET /api/admin/workload/unassigned-tasks
     */
    @GetMapping("/unassigned-tasks")
    public ResponseEntity<List<UnassignedTaskDto>> getUnassignedTasks() {
        return ResponseEntity.ok(taskAssignmentService.getUnassignedTasks());
    }

    /**
     * Assign task to employee
     * POST /api/admin/workload/assign-task
     */
    @PostMapping("/assign-task")
    public ResponseEntity<String> assignTask(@RequestBody TaskAssignmentRequest request) {
        taskAssignmentService.assignTask(request);
        return ResponseEntity.ok("Task assigned successfully");
    }

    /**
     * Unassign task from employee
     * POST /api/admin/workload/unassign-task/{taskId}
     */
    @PostMapping("/unassign-task/{taskId}")
    public ResponseEntity<String> unassignTask(@PathVariable Long taskId) {
        taskAssignmentService.unassignTask(taskId);
        return ResponseEntity.ok("Task unassigned successfully");
    }

    /**
     * Get employee list - For employee selection (Image 2)
     * GET /api/admin/workload/employees
     */
    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeListDto>> getEmployeeList() {
        return ResponseEntity.ok(employeeScheduleService.getAllEmployees());
    }

    /**
     * Get employee schedule - Shows when admin selects employee (Image 2)
     * GET /api/admin/workload/employee/{employeeId}/schedule?days=7
     */
    @GetMapping("/employee/{employeeId}/schedule")
    public ResponseEntity<EmployeeScheduleDto> getEmployeeSchedule(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(employeeScheduleService.getEmployeeSchedule(employeeId, days));
    }

    /**
     * Team Status Overview - For status counts (Image 3)
     * GET /api/admin/workload/team-status
     */
    @GetMapping("/team-status")
    public ResponseEntity<TeamStatusDto> getTeamStatus() {
        List<WorkloadResponse> workloads = workloadMonitoringService.getAllEmployeeWorkloads();
        
        long available = workloads.stream()
                .filter(w -> "AVAILABLE".equals(w.getWorkloadStatus()))
                .count();
        
        long busy = workloads.stream()
                .filter(w -> "BUSY".equals(w.getWorkloadStatus()))
                .count();
        
        long overloaded = workloads.stream()
                .filter(w -> "OVERLOADED".equals(w.getWorkloadStatus()))
                .count();

        TeamStatusDto status = TeamStatusDto.builder()
                .available((int) available)
                .busy((int) busy)
                .overloaded((int) overloaded)
                .total(workloads.size())
                .build();

        return ResponseEntity.ok(status);
    }

    /**
     * Capacity Distribution - For chart data (Image 3)
     * GET /api/admin/workload/capacity-distribution
     */
    @GetMapping("/capacity-distribution")
    public ResponseEntity<List<CapacityDistributionDto>> getCapacityDistribution() {
        List<WorkloadResponse> workloads = workloadMonitoringService.getAllEmployeeWorkloads();
        
        List<CapacityDistributionDto> distribution = workloads.stream()
                .map(w -> CapacityDistributionDto.builder()
                        .employeeId(w.getEmployeeId())
                        .name(w.getName())
                        .department(w.getDepartment())
                        .capacityPercentage(Math.round(w.getCapacityUtilization() * 100.0) / 100.0)
                        .status(w.getWorkloadStatus())
                        .activeTaskCount(w.getActiveAppointments())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(distribution);
    }

    /**
     * Get all employee workloads
     * GET /api/admin/workload/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<WorkloadResponse>> getAllEmployeeWorkloads() {
        return ResponseEntity.ok(workloadMonitoringService.getAllEmployeeWorkloads());
    }

    /**
     * Get specific employee workload
     * GET /api/admin/workload/employee/{employeeId}
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<WorkloadResponse> getEmployeeWorkload(@PathVariable Long employeeId) {
        return ResponseEntity.ok(workloadMonitoringService.getEmployeeWorkload(employeeId));
    }

    /**
     * Get overloaded employees
     * GET /api/admin/workload/overloaded
     */
    @GetMapping("/overloaded")
    public ResponseEntity<List<WorkloadResponse>> getOverloadedEmployees() {
        return ResponseEntity.ok(workloadMonitoringService.getOverloadedEmployees());
    }

    /**
     * Get available employees
     * GET /api/admin/workload/available
     */
    @GetMapping("/available")
    public ResponseEntity<List<WorkloadResponse>> getAvailableEmployees() {
        return ResponseEntity.ok(workloadMonitoringService.getAvailableEmployees());
    }

    /**
     * Get workload by department
     * GET /api/admin/workload/department/{department}
     */
    @GetMapping("/department/{department}")
    public ResponseEntity<List<WorkloadResponse>> getWorkloadByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(workloadMonitoringService.getWorkloadByDepartment(department));
    }

    /**
     * Get workload summary
     * GET /api/admin/workload/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<WorkloadSummaryDto> getWorkloadSummary() {
        List<WorkloadResponse> allWorkloads = workloadMonitoringService.getAllEmployeeWorkloads();
        
        long availableCount = allWorkloads.stream()
                .filter(w -> "AVAILABLE".equals(w.getWorkloadStatus()))
                .count();
        
        long busyCount = allWorkloads.stream()
                .filter(w -> "BUSY".equals(w.getWorkloadStatus()))
                .count();
        
        long overloadedCount = allWorkloads.stream()
                .filter(w -> "OVERLOADED".equals(w.getWorkloadStatus()))
                .count();
        
        double avgUtilization = allWorkloads.stream()
                .mapToDouble(WorkloadResponse::getCapacityUtilization)
                .average()
                .orElse(0.0);
        
        int totalActiveTasks = allWorkloads.stream()
                .mapToInt(WorkloadResponse::getActiveAppointments)
                .sum();

        WorkloadSummaryDto summary = new WorkloadSummaryDto(
                allWorkloads.size(),
                (int) availableCount,
                (int) busyCount,
                (int) overloadedCount,
                avgUtilization,
                totalActiveTasks
        );
        
        return ResponseEntity.ok(summary);
    }
}