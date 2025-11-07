package com.autocare360.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autocare360.dto.ActiveProjectDTO;
import com.autocare360.dto.CreateTimeLogRequestDTO;
import com.autocare360.dto.StartTimerRequestDTO;
import com.autocare360.dto.StopTimerRequestDTO;
import com.autocare360.dto.TimeLogResponseDTO;
import com.autocare360.dto.TimeLogSummaryDTO;
import com.autocare360.dto.TimerResponseDTO;
import com.autocare360.dto.UpdateTimeLogRequestDTO;
import com.autocare360.entity.Appointment;
import com.autocare360.entity.Employee;
import com.autocare360.entity.TimeLog;
import com.autocare360.entity.Timer;
import com.autocare360.exception.ResourceNotFoundException;
import com.autocare360.repo.AppointmentRepository;
import com.autocare360.repo.EmployeeRepository;
import com.autocare360.repo.TimeLogRepository;
import com.autocare360.repo.TimerRepository;
import com.autocare360.util.AuthUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/time-logs")
@CrossOrigin(origins = "http://localhost:3000")
public class TimeLogController {

    @Autowired
    private TimeLogRepository timeLogRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TimerRepository timerRepository;

    @Autowired
    private AuthUtil authUtil;

    // 1. GET /api/time-logs - Get all time logs for employee
    @GetMapping
    public ResponseEntity<List<TimeLogResponseDTO>> getTimeLogs(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Authentication authentication) {

        Long employeeId = authUtil.getUserIdFromAuth(authentication);
        List<TimeLog> timeLogs;

        if (startDate != null && endDate != null) {
            timeLogs = timeLogRepository.findByEmployee_IdAndDateBetweenOrderByDateDescCreatedAtDesc(
                    employeeId, startDate, endDate);
        } else {
            timeLogs = timeLogRepository.findByEmployee_IdOrderByDateDescCreatedAtDesc(employeeId);
        }

        List<TimeLogResponseDTO> response = timeLogs.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 2. POST /api/time-logs - Create new time log
    @PostMapping
    public ResponseEntity<?> createTimeLog(@Valid @RequestBody CreateTimeLogRequestDTO request,Authentication authentication) {
        try {
            Long employeeId = authUtil.getUserIdFromAuth(authentication);

            // Validate employee
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

            // Validate appointment
            Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

            // Validate hours
            if (request.getHours().compareTo(BigDecimal.ZERO) <= 0 ||
                    request.getHours().compareTo(new BigDecimal("24")) > 0) {
                return ResponseEntity.badRequest().body("Hours must be between 0 and 24");
            }

            // Validate date not in future
            if (request.getDate().isAfter(LocalDate.now())) {
                return ResponseEntity.badRequest().body("Cannot log time for future dates");
            }

            // Create time log
            TimeLog timeLog = new TimeLog();
            timeLog.setEmployee(employee);
            timeLog.setAppointment(appointment);
            timeLog.setHours(request.getHours());
            timeLog.setDescription(request.getDescription());
            timeLog.setDate(request.getDate());
            timeLog.setStatus(request.getStatus() != null ? request.getStatus() : "COMPLETED");

            TimeLog savedTimeLog = timeLogRepository.save(timeLog);

            // Update appointment actual hours
            updateAppointmentActualHours(appointment, employeeId);

            TimeLogResponseDTO response = mapToResponseDTO(savedTimeLog);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating time log: " + e.getMessage());
        }
    }

    // 3. PUT /api/time-logs/{id} - Update time log
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTimeLog(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTimeLogRequestDTO request,
            Authentication authentication) {
        try {
            Long employeeId = authUtil.getUserIdFromAuth(authentication);

            TimeLog timeLog = timeLogRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Time log not found"));

            // Verify ownership
            if (!timeLog.getEmployeeId().equals(employeeId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only edit your own time logs");
            }

            // Update fields if provided
            if (request.getAppointmentId() != null) {
                Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
                timeLog.setAppointment(appointment);
            }

            if (request.getHours() != null) {
                if (request.getHours().compareTo(BigDecimal.ZERO) <= 0) {
                    return ResponseEntity.badRequest().body("Hours must be greater than 0");
                }
                timeLog.setHours(request.getHours());
            }

            if (request.getDescription() != null) {
                timeLog.setDescription(request.getDescription());
            }

            if (request.getDate() != null) {
                if (request.getDate().isAfter(LocalDate.now())) {
                    return ResponseEntity.badRequest().body("Cannot log time for future dates");
                }
                timeLog.setDate(request.getDate());
            }

            if (request.getStatus() != null) {
                timeLog.setStatus(request.getStatus());
            }

            TimeLog updatedTimeLog = timeLogRepository.save(timeLog);

            // Update appointment actual hours
            updateAppointmentActualHours(timeLog.getAppointment(), employeeId);

            TimeLogResponseDTO response = mapToResponseDTO(updatedTimeLog);
            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating time log: " + e.getMessage());
        }
    }

    // 4. DELETE /api/time-logs/{id} - Delete time log
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTimeLog(@PathVariable Long id,
                                           Authentication authentication) {
        try {
            Long employeeId = authUtil.getUserIdFromAuth(authentication);

            TimeLog timeLog = timeLogRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Time log not found"));

            // Verify ownership
            if (!timeLog.getEmployeeId().equals(employeeId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only delete your own time logs");
            }

            Appointment appointment = timeLog.getAppointment();

            timeLogRepository.delete(timeLog);

            // Update appointment actual hours
            updateAppointmentActualHours(appointment, employeeId);

            return ResponseEntity.noContent().build();

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting time log: " + e.getMessage());
        }
    }

    // 5. GET /api/time-logs/summary - Get statistics
    @GetMapping("/summary")
    public ResponseEntity<TimeLogSummaryDTO> getTimeLogSummary(Authentication authentication) {
        Long employeeId = authUtil.getUserIdFromAuth(authentication);

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);

        // Calculate total hours today
        BigDecimal totalHoursToday = timeLogRepository
                .sumHoursByEmployeeAndDate(employeeId, today);

        // Calculate total hours this week
        BigDecimal totalHoursWeek = timeLogRepository
                .sumHoursByEmployeeAndDateRange(employeeId, weekStart, weekEnd);

        // Count total entries
        Long count = timeLogRepository.countByEmployee_Id(employeeId);
        Integer totalEntries = count != null ? count.intValue() : 0;

        // Calculate efficiency rate (Actual hours / Expected 40 hours * 100)
        BigDecimal expectedHoursWeek = new BigDecimal("40.0");
        BigDecimal efficiencyRate = BigDecimal.ZERO;
        if (totalHoursWeek.compareTo(BigDecimal.ZERO) > 0) {
            efficiencyRate = totalHoursWeek
                    .divide(expectedHoursWeek, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP);
        }

        TimeLogSummaryDTO summary = new TimeLogSummaryDTO(
                totalHoursToday, totalHoursWeek, totalEntries, efficiencyRate);

        return ResponseEntity.ok(summary);
    }

    // 6. GET /api/time-logs/active-projects - Get active appointments
    @GetMapping("/active-projects")
    public ResponseEntity<List<ActiveProjectDTO>> getActiveProjects() {
        // Find appointments that are IN_PROGRESS or APPROVED
        List<Appointment> appointments = appointmentRepository
                .findByStatusOrderByDateAscTimeAsc("IN_PROGRESS");

        // Also get APPROVED appointments
        List<Appointment> approvedAppointments = appointmentRepository
                .findByStatusOrderByDateAscTimeAsc("APPROVED");

        appointments.addAll(approvedAppointments);

        List<ActiveProjectDTO> response = appointments.stream()
                .map(appointment -> new ActiveProjectDTO(
                        appointment.getId(),
                        appointment.getService() + " - " + appointment.getVehicle(),
                        appointment.getUserId(),
                        appointment.getUser().getName()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 7. POST /api/time-logs/timer/start - Start timer
    @PostMapping("/timer/start")
    public ResponseEntity<?> startTimer(@Valid @RequestBody StartTimerRequestDTO request,
                                        Authentication authentication) {
        try {
            Long employeeId = authUtil.getUserIdFromAuth(authentication);

            // Check if employee already has active timer
            if (timerRepository.existsByEmployee_IdAndIsActiveTrue(employeeId)) {
                return ResponseEntity.badRequest().body("You already have an active timer running");
            }

            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

            Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

            // Create timer
            Timer timer = new Timer();
            timer.setEmployee(employee);
            timer.setAppointment(appointment);
            timer.setStartTime(LocalDateTime.now());
            timer.setIsActive(true);

            Timer savedTimer = timerRepository.save(timer);

            // Build response
            TimerResponseDTO response = new TimerResponseDTO();
            response.setTimerId(savedTimer.getId());
            response.setAppointmentId(savedTimer.getAppointmentId());
            response.setProjectName(savedTimer.getProjectName());
            response.setStartTime(savedTimer.getStartTime());
            response.setElapsedSeconds(0);
            response.setIsActive(true);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error starting timer: " + e.getMessage());
        }
    }

    // 8. POST /api/time-logs/timer/stop - Stop timer
    @PostMapping("/timer/stop")
    public ResponseEntity<?> stopTimer(@Valid @RequestBody StopTimerRequestDTO request, Authentication authentication) {
        try {
            Long employeeId = authUtil.getUserIdFromAuth(authentication);

            Timer timer = timerRepository.findById(request.getTimerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Timer not found"));

            // Verify ownership
            if (!timer.getEmployeeId().equals(employeeId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("This timer does not belong to you");
            }

            // Verify timer is active
            if (!timer.getIsActive()) {
                return ResponseEntity.badRequest().body("Timer is not active");
            }

            // Calculate elapsed time
            LocalDateTime now = LocalDateTime.now();
            long elapsedSeconds = Duration.between(timer.getStartTime(), now).getSeconds();
            BigDecimal hours = new BigDecimal(elapsedSeconds)
                    .divide(new BigDecimal("3600"), 2, RoundingMode.HALF_UP);

            // Validate minimum time (at least 1 minute)
            if (hours.compareTo(new BigDecimal("0.02")) < 0) {
                return ResponseEntity.badRequest()
                        .body("Timer must run for at least 1 minute");
            }

            // Create time log from timer
            TimeLog timeLog = new TimeLog();
            timeLog.setEmployee(timer.getEmployee());
            timeLog.setAppointment(timer.getAppointment());
            timeLog.setHours(hours);
            timeLog.setDescription(request.getDescription());
            timeLog.setDate(LocalDate.now());
            timeLog.setStatus("COMPLETED");

            TimeLog savedTimeLog = timeLogRepository.save(timeLog);

            // Mark timer as inactive
            timer.setIsActive(false);
            timer.setEndTime(now);
            timerRepository.save(timer);

            // Update appointment actual hours
            updateAppointmentActualHours(timer.getAppointment(), employeeId);

            TimeLogResponseDTO response = mapToResponseDTO(savedTimeLog);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error stopping timer: " + e.getMessage());
        }
    }

    // 9. GET /api/time-logs/timer/active - Get active timer
    @GetMapping("/timer/active")
    public ResponseEntity<TimerResponseDTO> getActiveTimer(Authentication authentication) {
        Long employeeId = authUtil.getUserIdFromAuth(authentication);

        // Find active timer for employee
        return timerRepository.findByEmployee_IdAndIsActiveTrue(employeeId)
                .map(timer -> {
                    // Calculate elapsed time
                    LocalDateTime now = LocalDateTime.now();
                    long elapsedSeconds = Duration.between(timer.getStartTime(), now).getSeconds();
                    BigDecimal elapsedHours = new BigDecimal(elapsedSeconds)
                            .divide(new BigDecimal("3600"), 2, RoundingMode.HALF_UP);

                    // Build response
                    TimerResponseDTO response = new TimerResponseDTO();
                    response.setTimerId(timer.getId());
                    response.setAppointmentId(timer.getAppointmentId());
                    response.setProjectName(timer.getProjectName());
                    response.setStartTime(timer.getStartTime());
                    response.setElapsedSeconds((int) elapsedSeconds);
                    response.setElapsedHours(elapsedHours);
                    response.setIsActive(true);

                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    // No active timer - return empty response
                    TimerResponseDTO response = new TimerResponseDTO();
                    response.setIsActive(false);
                    return ResponseEntity.ok(response);
                });
    }

    // Helper method to map TimeLog to DTO
    private TimeLogResponseDTO mapToResponseDTO(TimeLog timeLog) {
        TimeLogResponseDTO dto = new TimeLogResponseDTO();
        dto.setId(timeLog.getId());
        dto.setDate(timeLog.getDate());
        dto.setAppointmentId(timeLog.getAppointmentId());
        dto.setProject(timeLog.getProjectName());
        dto.setCustomerId(timeLog.getAppointment() != null ? timeLog.getAppointment().getUserId() : null);
        dto.setCustomer(timeLog.getCustomerName());
        dto.setHours(timeLog.getHours());
        dto.setDescription(timeLog.getDescription());
        dto.setStatus(timeLog.getStatus());
        dto.setEmployeeId(timeLog.getEmployeeId());
        dto.setEmployeeName(timeLog.getEmployeeName());
        dto.setCreatedAt(timeLog.getCreatedAt());
        dto.setUpdatedAt(timeLog.getUpdatedAt());
        return dto;
    }

    // Helper method to update appointment actual hours
    private void updateAppointmentActualHours(Appointment appointment, Long employeeId) {
        if (appointment != null) {
            List<TimeLog> appointmentTimeLogs = timeLogRepository
                    .findByEmployee_IdOrderByDateDescCreatedAtDesc(employeeId)
                    .stream()
                    .filter(tl -> tl.getAppointmentId().equals(appointment.getId()))
                    .collect(Collectors.toList());

            BigDecimal totalHours = appointmentTimeLogs.stream()
                    .map(TimeLog::getHours)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            appointment.setActualHours(totalHours);
            appointmentRepository.save(appointment);
        }
    }
}