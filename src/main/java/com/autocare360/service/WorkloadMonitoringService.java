package com.autocare360.service;

import com.autocare360.dto.CapacityDistributionDto;
import com.autocare360.dto.TaskSummaryDto;
import com.autocare360.dto.TeamStatusDto;
import com.autocare360.dto.WorkloadResponse;
import com.autocare360.dto.WorkloadSummaryDto;
import com.autocare360.entity.Appointment;
import com.autocare360.entity.Employee;
import com.autocare360.repo.AppointmentRepository;
import com.autocare360.repo.EmployeeRepository;
import com.autocare360.repo.TimeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkloadMonitoringService {

    private final EmployeeRepository employeeRepository;
    private final AppointmentRepository appointmentRepository;
    private final TimeLogRepository timeLogRepository;

    // Workload thresholds
    private static final double NORMAL_CAPACITY_HOURS_PER_WEEK = 40.0; // hours per week
    private static final double BUSY_THRESHOLD = 0.80;                 // 80% capacity
    private static final double OVERLOADED_THRESHOLD = 1.00;           // 100% capacity

    // ----------------------------------------------------------------
    // MAIN WORKLOAD QUERIES
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<WorkloadResponse> getAllEmployeeWorkloads() {
        // In this project "ACTIVE" employees are the ones in use
        List<Employee> employees = employeeRepository.findByStatus("ACTIVE");

        return employees.stream()
                .map(this::buildWorkloadResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkloadResponse getEmployeeWorkload(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + employeeId));
        return buildWorkloadResponse(employee);
    }

    @Transactional(readOnly = true)
    public List<WorkloadResponse> getOverloadedEmployees() {
        return getAllEmployeeWorkloads().stream()
                .filter(w -> "OVERLOADED".equals(w.getWorkloadStatus()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkloadResponse> getAvailableEmployees() {
        return getAllEmployeeWorkloads().stream()
                .filter(w -> "AVAILABLE".equals(w.getWorkloadStatus()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkloadResponse> getWorkloadByDepartment(String department) {
        List<Employee> employees = employeeRepository.findByStatus("ACTIVE");

        return employees.stream()
                .filter(e -> e.getSpecialization() != null &&
                        e.getSpecialization().toLowerCase().contains(department.toLowerCase()))
                .map(this::buildWorkloadResponse)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // SUMMARY DATA FOR DASHBOARD CARDS & CHARTS
    // ----------------------------------------------------------------

    /** Used by Capacity chart on dashboard */
    @Transactional(readOnly = true)
    public List<CapacityDistributionDto> getCapacityDistribution() {
        List<WorkloadResponse> workloads = getAllEmployeeWorkloads();

        List<CapacityDistributionDto> list = new ArrayList<>();
        for (WorkloadResponse w : workloads) {
            String color;
            switch (w.getWorkloadStatus()) {
                case "OVERLOADED" -> color = "#EF4444"; // red
                case "BUSY" -> color = "#F97316";       // orange
                default -> color = "#22C55E";           // green
            }

            list.add(
                    CapacityDistributionDto.builder()
                            .employeeId(w.getEmployeeId())
                            .name(w.getName())
                            .capacityPercentage(w.getCapacityUtilization())
                            .color(color)
                            .build()
            );
        }
        return list;
    }

    /** Used by "Team Status Overview" card */
    @Transactional(readOnly = true)
    public TeamStatusDto getTeamStatus() {
        List<WorkloadResponse> workloads = getAllEmployeeWorkloads();

        int available = 0;
        int busy = 0;
        int overloaded = 0;

        for (WorkloadResponse w : workloads) {
            switch (w.getWorkloadStatus()) {
                case "OVERLOADED" -> overloaded++;
                case "BUSY" -> busy++;
                default -> available++;
            }
        }

        int total = workloads.size();

        return TeamStatusDto.builder()
                .total(total)
                .availableCount(available)
                .busyCount(busy)
                .overloadedCount(overloaded)
                .build();
    }

    /** Used by summary numbers at the bottom of the Capacity section */
    @Transactional(readOnly = true)
    public WorkloadSummaryDto getWorkloadSummary() {
        List<WorkloadResponse> workloads = getAllEmployeeWorkloads();

        double totalCapacity = 0.0;
        int activeItems = 0;
        int totalEmployees = workloads.size();

        for (WorkloadResponse w : workloads) {
            totalCapacity += w.getCapacityUtilization();
            activeItems += w.getActiveAppointments();
        }

        double avgCapacity = totalEmployees > 0 ? totalCapacity / totalEmployees : 0.0;
        double utilization = avgCapacity; // same unit: %

        return WorkloadSummaryDto.builder()
                .totalEmployees(totalEmployees)
                .averageCapacity(avgCapacity)
                .activeItems(activeItems)
                .utilizationRate(utilization)
                .build();
    }

    // ----------------------------------------------------------------
    // INTERNAL HELPERS
    // ----------------------------------------------------------------

    private WorkloadResponse buildWorkloadResponse(Employee employee) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(7);
        LocalDate monthStart = today.minusDays(30);

        int activeAppointments = appointmentRepository
                .countByAssignedEmployee_IdAndStatusIn(
                        employee.getId(),
                        List.of("PENDING", "IN_PROGRESS", "SCHEDULED")
                );

        BigDecimal hoursWeek = timeLogRepository
                .sumHoursByEmployeeAndDateRange(employee.getId(), weekStart, today);
        BigDecimal hoursMonth = timeLogRepository
                .sumHoursByEmployeeAndDateRange(employee.getId(), monthStart, today);

        double hoursThisWeek = hoursWeek != null ? hoursWeek.doubleValue() : 0.0;
        double hoursThisMonth = hoursMonth != null ? hoursMonth.doubleValue() : 0.0;

        double capacityUtilization = calculateCapacityUtilization(hoursThisWeek);
        String workloadStatus = determineWorkloadStatus(capacityUtilization, activeAppointments);

        List<TaskSummaryDto> upcomingTasks = getUpcomingTasks(employee.getId());
        List<TaskSummaryDto> activeTasks = getActiveTasks(employee.getId());

        return WorkloadResponse.builder()
                .employeeId(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .department(employee.getSpecialization() != null ? employee.getSpecialization() : "General")
                .activeAppointments(activeAppointments)
                .activeProjects(activeAppointments)
                .hoursLoggedThisWeek(hoursThisWeek)
                .hoursLoggedThisMonth(hoursThisMonth)
                .capacityUtilization(capacityUtilization)
                .workloadStatus(workloadStatus)
                .upcomingTasks(upcomingTasks)
                .activeTasks(activeTasks)
                .build();
    }

    private double calculateCapacityUtilization(double hoursWorkedThisWeek) {
        if (hoursWorkedThisWeek <= 0) {
            return 0.0;
        }
        return (hoursWorkedThisWeek / NORMAL_CAPACITY_HOURS_PER_WEEK) * 100.0;
    }

    private String determineWorkloadStatus(double utilizationPercent, int activeCount) {
        if (utilizationPercent >= OVERLOADED_THRESHOLD * 100 || activeCount > 5) {
            return "OVERLOADED";
        } else if (utilizationPercent >= BUSY_THRESHOLD * 100 || activeCount > 3) {
            return "BUSY";
        } else {
            return "AVAILABLE";
        }
    }

    private List<TaskSummaryDto> getUpcomingTasks(Long employeeId) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(7);

        List<Appointment> appointments = appointmentRepository
                .findByAssignedEmployee_IdAndDateBetween(employeeId, today, futureDate);

        return appointments.stream()
                .filter(a -> "PENDING".equals(a.getStatus()) || "SCHEDULED".equals(a.getStatus()))
                .map(this::convertToTaskSummary)
                .collect(Collectors.toList());
    }

    private List<TaskSummaryDto> getActiveTasks(Long employeeId) {
        List<Appointment> appointments = appointmentRepository
                .findByAssignedEmployee_IdAndStatusOrderByDateAscTimeAsc(employeeId, "IN_PROGRESS");

        return appointments.stream()
                .map(this::convertToTaskSummary)
                .collect(Collectors.toList());
    }

    private TaskSummaryDto convertToTaskSummary(Appointment appointment) {
        String vehicleInfo = appointment.getVehicle() != null
                ? appointment.getVehicle()
                : "N/A";

        LocalDateTime scheduledDateTime = LocalDateTime.of(
                appointment.getDate(),
                appointment.getTime() != null ? appointment.getTime() : LocalTime.of(9, 0)
        );

        LocalDateTime dueDateTime = appointment.getDueDate() != null
                ? LocalDateTime.of(appointment.getDueDate(), LocalTime.of(17, 0))
                : null;

        return TaskSummaryDto.builder()
                .taskId(appointment.getId())
                .taskType(appointment.getService())
                .taskName(appointment.getService() + " - " + vehicleInfo)
                .status(appointment.getStatus())
                .scheduledDate(scheduledDateTime)
                .dueDate(dueDateTime)
                .priority("MEDIUM")
                .progress(appointment.getProgress() != null ? appointment.getProgress() : 0)
                .vehicleInfo(vehicleInfo)
                .build();
    }
}
