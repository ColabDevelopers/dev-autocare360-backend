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
        List<Appointment> unassignedAppointments = appointmentRepository
                .findByAssignedEmployeeIsNullAndStatusIn(List.of("PENDING", "SCHEDULED"));

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
        messagingTemplate.convertAndSend("/topic/workload-update", 
            "Task assigned to " + employee.getName());
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
        messagingTemplate.convertAndSend("/topic/workload-update", 
            "Task unassigned");
    }

    private void updateEmployeeStatus(Employee employee) {
        int activeCount = appointmentRepository
                .countByEmployeeIdAndStatusIn(employee.getId(), 
                    List.of("IN_PROGRESS", "PENDING"));

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
        // Vehicle is a String, not an object
        String vehicleInfo = appointment.getVehicle() != null 
                ? appointment.getVehicle() 
                : "N/A";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String dueDate = appointment.getDueDate() != null
                ? appointment.getDueDate().format(formatter)
                : appointment.getDate().format(formatter);

        // Get estimated hours or default to 2
        Integer estimatedHours = appointment.getEstimatedHours() != null 
                ? appointment.getEstimatedHours().intValue() 
                : 2;

        return UnassignedTaskDto.builder()
                .id(appointment.getId())
                .title(appointment.getService())
                .customerName(appointment.getUser().getName())
                .priority("MEDIUM") // Default priority since not in entity
                .type("appointment")
                .dueDate(dueDate)
                .estimatedHours(estimatedHours)
                .vehicleInfo(vehicleInfo)
                .description(appointment.getNotes())
                .build();
    }
}