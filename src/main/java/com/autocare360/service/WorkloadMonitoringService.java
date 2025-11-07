package com.autocare360.service;

import com.autocare360.dto.TaskSummaryDto;
import com.autocare360.dto.WorkloadResponse;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkloadMonitoringService {

    private final EmployeeRepository employeeRepository;
    private final AppointmentRepository appointmentRepository;
    private final TimeLogRepository timeLogRepository;

    // Workload thresholds
    private static final double NORMAL_CAPACITY = 40.0; // hours per week
    private static final double BUSY_THRESHOLD = 0.80; // 80% capacity
    private static final double OVERLOADED_THRESHOLD = 1.0; // 100% capacity

    @Transactional(readOnly = true)
    public List<WorkloadResponse> getAllEmployeeWorkloads() {
        List<Employee> employees = employeeRepository.findByActiveTrue();
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
        // Note: Employee entity doesn't have department field, using specialization instead
        List<Employee> employees = employeeRepository.findByActiveTrue();
        return employees.stream()
                .filter(e -> e.getSpecialization() != null && 
                           e.getSpecialization().contains(department))
                .map(this::buildWorkloadResponse)
                .collect(Collectors.toList());
    }

    private WorkloadResponse buildWorkloadResponse(Employee employee) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(7);
        LocalDate monthStart = today.minusDays(30);

        // Get active appointments
        Integer activeAppointments = appointmentRepository
                .countByEmployeeIdAndStatusIn(employee.getId(), 
                    List.of("PENDING", "IN_PROGRESS", "SCHEDULED"));

        // Calculate hours logged
        BigDecimal hoursWeek = timeLogRepository
                .sumHoursByEmployeeAndDateRange(employee.getId(), weekStart, today);
        BigDecimal hoursMonth = timeLogRepository
                .sumHoursByEmployeeAndDateRange(employee.getId(), monthStart, today);
        
        Double hoursThisWeek = hoursWeek != null ? hoursWeek.doubleValue() : 0.0;
        Double hoursThisMonth = hoursMonth != null ? hoursMonth.doubleValue() : 0.0;

        // Calculate capacity utilization
        Double capacityUtilization = calculateCapacityUtilization(hoursThisWeek);

        // Determine workload status
        String workloadStatus = determineWorkloadStatus(capacityUtilization, activeAppointments);

        // Get upcoming and active tasks
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

    private Double calculateCapacityUtilization(Double hoursWorked) {
        if (hoursWorked == null || hoursWorked == 0) {
            return 0.0;
        }
        return (hoursWorked / NORMAL_CAPACITY) * 100.0;
    }

    private String determineWorkloadStatus(Double utilization, Integer activeCount) {
        if (utilization >= OVERLOADED_THRESHOLD * 100 || activeCount > 5) {
            return "OVERLOADED";
        } else if (utilization >= BUSY_THRESHOLD * 100 || activeCount > 3) {
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
                .findByAssignedEmployee_IdAndStatus(employeeId, "IN_PROGRESS");
        
        return appointments.stream()
                .map(this::convertToTaskSummary)
                .collect(Collectors.toList());
    }

    private TaskSummaryDto convertToTaskSummary(Appointment appointment) {
        // Vehicle is a String, not an object
        String vehicleInfo = appointment.getVehicle() != null 
            ? appointment.getVehicle() 
            : "N/A";

        // Combine date and time into LocalDateTime
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
                .priority("MEDIUM") // Default priority
                .progress(appointment.getProgress() != null ? appointment.getProgress() : 0)
                .vehicleInfo(vehicleInfo)
                .build();
    }
}