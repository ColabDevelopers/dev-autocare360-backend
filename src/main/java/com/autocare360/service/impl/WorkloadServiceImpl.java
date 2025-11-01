package com.autocare360.service.impl;

import com.autocare360.dto.*;
import com.autocare360.entity.Employee;
import com.autocare360.entity.JobAssignment;
import com.autocare360.entity.WorkItem;
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
        log.info("Assigning work item {} to employee {}", request.getWorkItemId(), request.getEmployeeId());
        WorkItem workItem = workItemRepository.findById(request.getWorkItemId())
            .orElseThrow(() -> new RuntimeException("Work item not found"));
        Employee employee = employeeRepository.findById(request.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        JobAssignment existing = jobAssignmentRepository.findActiveByWorkItemIdAndEmployeeId(
            request.getWorkItemId(), request.getEmployeeId());
        if (existing != null) {
            throw new RuntimeException("Work item already assigned to this employee");
        }
        JobAssignment assignment = JobAssignment.builder()
            .workItemId(request.getWorkItemId())
            .employeeId(request.getEmployeeId())
            .roleOnJob(request.getRoleOnJob() != null ? request.getRoleOnJob() : "Technician")
            .active(true)
            .assignedAt(LocalDateTime.now())
            .build();
        jobAssignmentRepository.save(assignment);
        log.info("Successfully assigned work item {} to employee {}", request.getWorkItemId(), request.getEmployeeId());
    }

    @Override
    public List<WorkloadResponse> getEmployeeAvailability() {
        return getAllEmployeeWorkloads().stream()
            .sorted((a, b) -> Double.compare(a.getCapacityUtilization(), b.getCapacityUtilization()))
            .collect(Collectors.toList());
    }

    private WorkloadResponse buildWorkloadResponse(Employee employee) {
        Long employeeId = employee.getId();
        List<JobAssignment> activeAssignments = jobAssignmentRepository.findActiveByEmployeeId(employeeId);
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
                TaskDto task = TaskDto.builder()
                    .id(assignment.getId())
                    .workItemId(workItem.getId())
                    .title(workItem.getTitle())
                    .type(workItem.getType())
                    .scheduledDate(workItem.getCreatedAt().toString())
                    .estimatedHours(workItem.getEstimatedHours() != null ? workItem.getEstimatedHours() : 10.0)
                    .status(workItem.getStatus())
                    .customerName("Customer")
                    .build();
                upcomingTasks.add(task);
            }
        }
        LocalDateTime startOfWeek = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime endOfWeek = startOfWeek.plusDays(7);
        Double minutesThisWeek = timeLogRepository.sumMinutesByEmployeeIdAndDateRange(employeeId, startOfWeek, endOfWeek);
        double hoursThisWeek = (minutesThisWeek != null ? minutesThisWeek : 0.0) / 60.0;
        LocalDateTime startOfMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        Double minutesThisMonth = timeLogRepository.sumMinutesByEmployeeIdAndDateRange(employeeId, startOfMonth, endOfMonth);
        double hoursThisMonth = (minutesThisMonth != null ? minutesThisMonth : 0.0) / 60.0;
        double capacityUtilization = (hoursThisWeek / STANDARD_WEEKLY_HOURS) * 100.0;
        capacityUtilization = Math.min(capacityUtilization, 100.0);
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
