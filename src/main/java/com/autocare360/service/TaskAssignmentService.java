package com.autocare360.service;

import com.autocare360.dto.TaskAssignmentRequest;
import com.autocare360.dto.UnassignedTaskDto;
import com.autocare360.entity.Appointment;
import com.autocare360.entity.Employee;
import com.autocare360.repo.AppointmentRepository;
import com.autocare360.repo.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskAssignmentService {

    private final AppointmentRepository appointmentRepository;
    private final EmployeeRepository employeeRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public List<UnassignedTaskDto> getUnassignedTasks() {
        // Uses the new repository method we just added
        List<Appointment> unassignedAppointments = appointmentRepository
                .findByAssignedEmployeeIsNullAndStatusIn(
                        Arrays.asList("PENDING", "SCHEDULED")
                );

        return unassignedAppointments.stream()
                .map(this::convertToUnassignedTaskDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignTask(TaskAssignmentRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        appointment.setAssignedEmployee(employee);
        appointment.setStatus("IN_PROGRESS");
        appointmentRepository.save(appointment);

        // Update employee status if needed
        updateEmployeeStatus(employee);

        // Send WebSocket notification for real-time update
        messagingTemplate.convertAndSend(
                "/topic/workload-update",
                "Task assigned to " + employee.getName()
        );
    }

    @Transactional
    public void unassignTask(Long taskId) {
        Appointment appointment = appointmentRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Employee employee = appointment.getAssignedEmployee();
        appointment.setAssignedEmployee(null);
        appointment.setStatus("PENDING");
        appointmentRepository.save(appointment);

        if (employee != null) {
            updateEmployeeStatus(employee);
        }

        // Send WebSocket notification
        messagingTemplate.convertAndSend(
                "/topic/workload-update",
                "Task unassigned"
        );
    }

    private void updateEmployeeStatus(Employee employee) {
        // Use the new countByAssignedEmployee_IdAndStatusIn method
        int activeCount = appointmentRepository.countByAssignedEmployee_IdAndStatusIn(
                employee.getId(),
                Arrays.asList("IN_PROGRESS", "PENDING")
        );

        if (activeCount == 0) {
            employee.setStatus("AVAILABLE");
        } else if (activeCount <= 3) {
            employee.setStatus("BUSY");
        } else {
            employee.setStatus("OVERLOADED");
        }

        employeeRepository.save(employee);
    }

    private UnassignedTaskDto convertToUnassignedTaskDto(Appointment appointment) {
        String vehicleInfo = appointment.getVehicle() != null
                ? appointment.getVehicle()
                : "N/A";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String dueDate = appointment.getDueDate() != null
                ? appointment.getDueDate().format(formatter)
                : appointment.getDate().format(formatter);

        Integer estimatedHours = appointment.getEstimatedHours() != null
                ? appointment.getEstimatedHours().intValue()
                : 2;

        return UnassignedTaskDto.builder()
                .id(appointment.getId())
                .title(appointment.getService())
                .customerName(appointment.getUser().getName())
                .priority("MEDIUM") // default priority, not in entity
                .type("appointment")
                .dueDate(dueDate)
                .estimatedHours(estimatedHours)
                .vehicleInfo(vehicleInfo)
                .description(appointment.getNotes())
                .build();
    }
}
