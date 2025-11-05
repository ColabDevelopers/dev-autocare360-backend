package com.autocare360.controller;

import com.autocare360.dto.AssignedJobDTO;
import com.autocare360.dto.EmployeeDashboardSummaryDTO;
import com.autocare360.dto.JobActionResponseDTO;
import com.autocare360.dto.JobStatusUpdateResponseDTO;
import com.autocare360.dto.TaskDistributionDTO;
import com.autocare360.dto.TodayAppointmentDTO;
import com.autocare360.dto.UpdateJobStatusRequestDTO;
import com.autocare360.dto.WeeklyWorkloadDTO;
import com.autocare360.entity.Appointment;
import com.autocare360.entity.Employee;
import com.autocare360.entity.TimeLog;
import com.autocare360.exception.ResourceNotFoundException;
import com.autocare360.repo.AppointmentRepository;
import com.autocare360.repo.EmployeeRepository;
import com.autocare360.repo.TimeLogRepository;
import com.autocare360.util.AuthUtil;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee/dashboard")
@CrossOrigin(origins = "http://localhost:3000")
public class EmployeeDashboardController {

  private static final Logger logger = LoggerFactory.getLogger(EmployeeDashboardController.class);

  @Autowired private AppointmentRepository appointmentRepository;

  @Autowired private EmployeeRepository employeeRepository;

  @Autowired private TimeLogRepository timeLogRepository;

    @Autowired
    private AuthUtil authUtil;

  // 1. GET /api/employee/dashboard/summary - Dashboard statistics
  @GetMapping("/summary")
  public ResponseEntity<EmployeeDashboardSummaryDTO> getDashboardSummary(
      Authentication authentication) {
    Long employeeId = authUtil.getUserIdFromAuth(authentication);

    // Get employee
    Employee employee =
        employeeRepository
            .findById(employeeId)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

    // 1. Count active jobs (IN_PROGRESS or APPROVED)
    List<Appointment> activeJobs =
        appointmentRepository.findByAssignedEmployee_IdAndStatusInOrderByDateAscTimeAsc(
            employeeId, Arrays.asList("IN_PROGRESS", "APPROVED"));
    Integer activeJobsCount = activeJobs.size();
    Integer jobsInProgress =
        (int) activeJobs.stream().filter(a -> "IN_PROGRESS".equals(a.getStatus())).count();

    // 2. Calculate today's hours
    LocalDate today = LocalDate.now();
    BigDecimal todayHours = timeLogRepository.sumHoursByEmployeeAndDate(employeeId, today);
    if (todayHours == null) todayHours = BigDecimal.ZERO;

    // 3. Target hours (8 hours per day)
    BigDecimal targetHours = new BigDecimal("8.0");

    // 4. Count completed jobs this month
    LocalDate monthStart = today.withDayOfMonth(1);
    LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
    Integer completedThisMonth =
        appointmentRepository.countByAssignedEmployeeIdAndStatusAndUpdatedAtBetween(
            employeeId, "COMPLETED", monthStart.atStartOfDay(), monthEnd.atTime(23, 59, 59));
    if (completedThisMonth == null) completedThisMonth = 0;

    // 5. Calculate efficiency rate (weekly hours / 40 hours * 100)
    LocalDate weekStart = today.with(DayOfWeek.MONDAY);
    LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);
    BigDecimal weekHours =
        timeLogRepository.sumHoursByEmployeeAndDateRange(employeeId, weekStart, weekEnd);
    if (weekHours == null) weekHours = BigDecimal.ZERO;

    BigDecimal expectedWeekHours = new BigDecimal("40.0");
    BigDecimal efficiencyRate = BigDecimal.ZERO;
    if (weekHours.compareTo(BigDecimal.ZERO) > 0) {
      efficiencyRate =
          weekHours
              .divide(expectedWeekHours, 4, RoundingMode.HALF_UP)
              .multiply(new BigDecimal("100"))
              .setScale(0, RoundingMode.HALF_UP);
    }

    // 6. Generate greeting
    LocalTime now = LocalTime.now();
    String greeting =
        now.isBefore(LocalTime.NOON)
            ? "Good morning"
            : now.isBefore(LocalTime.of(17, 0)) ? "Good afternoon" : "Good evening";

    // Build response
    EmployeeDashboardSummaryDTO summary =
        new EmployeeDashboardSummaryDTO(
            activeJobsCount,
            jobsInProgress,
            todayHours,
            targetHours,
            completedThisMonth,
            efficiencyRate,
            employee.getName(),
            greeting);

    return ResponseEntity.ok(summary);
  }

  // 2. GET /api/employee/dashboard/assigned-jobs - Get assigned jobs
  @GetMapping("/assigned-jobs")
  public ResponseEntity<List<AssignedJobDTO>> getAssignedJobs(
      @RequestParam(required = false) String status,
      @RequestParam(required = false, defaultValue = "false") Boolean includeCompleted,
      Authentication authentication) {

    Long employeeId = authUtil.getUserIdFromAuth(authentication);

    // Fetch appointments
    List<Appointment> appointments;
    if (status != null) {
      appointments =
          appointmentRepository.findByAssignedEmployee_IdAndStatusOrderByDateAscTimeAsc(
              employeeId, status);
    } else if (!includeCompleted) {
      appointments =
          appointmentRepository.findByAssignedEmployee_IdAndStatusInOrderByDateAscTimeAsc(
              employeeId, Arrays.asList("IN_PROGRESS", "APPROVED", "PENDING"));
    } else {
      appointments =
          appointmentRepository.findByAssignedEmployee_IdOrderByDateAscTimeAsc(employeeId);
    }

    // Map to DTOs
    List<AssignedJobDTO> assignedJobs = new ArrayList<>();
    for (Appointment appointment : appointments) {
      // Calculate logged hours
      BigDecimal loggedHours =
          timeLogRepository.sumHoursByEmployeeAndAppointment(employeeId, appointment.getId());
      if (loggedHours == null) loggedHours = BigDecimal.ZERO;

      // Get progress from appointment or calculate
      Integer progress = appointment.getProgress();
      if (progress == null || progress == 0) {
        // Calculate from hours if not set
        if (appointment.getEstimatedHours() != null
            && appointment.getEstimatedHours().compareTo(BigDecimal.ZERO) > 0) {
          progress =
              loggedHours
                  .divide(appointment.getEstimatedHours(), 4, RoundingMode.HALF_UP)
                  .multiply(new BigDecimal("100"))
                  .min(new BigDecimal("100"))
                  .intValue();
        } else {
          progress = 0;
        }
      }

      // If completed, progress should be 100
      if ("COMPLETED".equals(appointment.getStatus())) {
        progress = 100;
      }

      // Build DTO
      AssignedJobDTO dto = new AssignedJobDTO();
      dto.setId(appointment.getId());
      dto.setType(appointment.getService());
      dto.setCustomer(appointment.getUser().getName());
      dto.setCustomerId(appointment.getUserId());
      dto.setVehicle(appointment.getVehicle());
      dto.setProgress(progress);
      dto.setStatus(appointment.getStatus());
      dto.setEstimatedHours(appointment.getEstimatedHours());
      dto.setLoggedHours(loggedHours);
      dto.setDueDate(appointment.getDueDate());
      dto.setAppointmentDate(appointment.getDate());
      dto.setAppointmentTime(appointment.getTime().toString());
      dto.setAppointmentId(appointment.getId());
      dto.setDescription(appointment.getNotes());
      dto.setUpdatedAt(appointment.getUpdatedAt());

      assignedJobs.add(dto);
    }

    return ResponseEntity.ok(assignedJobs);
  }

  // 3. GET /api/employee/dashboard/today-appointments - Get today's appointments
  @GetMapping("/today-appointments")
  public ResponseEntity<List<TodayAppointmentDTO>> getTodayAppointments(
      Authentication authentication) {
    Long employeeId = authUtil.getUserIdFromAuth(authentication);
    LocalDate today = LocalDate.now();

    // Fetch today's appointments
    List<Appointment> appointments =
        appointmentRepository.findByAssignedEmployee_IdAndDateOrderByTimeAsc(employeeId, today);

    // Map to DTOs
    List<TodayAppointmentDTO> todayAppointments =
        appointments.stream()
            .map(
                appointment -> {
                  TodayAppointmentDTO dto = new TodayAppointmentDTO();
                  dto.setId(appointment.getId());
                  dto.setService(appointment.getService());
                  dto.setCustomer(appointment.getUser().getName());
                  dto.setCustomerId(appointment.getUserId());
                  dto.setTime(appointment.getTime().format(DateTimeFormatter.ofPattern("h:mm a")));
                  dto.setVehicle(appointment.getVehicle());
                  dto.setStatus(appointment.getStatus());
                  dto.setSpecialInstructions(appointment.getSpecialInstructions());
                  return dto;
                })
            .collect(Collectors.toList());

    return ResponseEntity.ok(todayAppointments);
  }

  // 4. GET /api/employee/dashboard/weekly-workload - Get weekly hours chart
  @GetMapping("/weekly-workload")
  public ResponseEntity<List<WeeklyWorkloadDTO>> getWeeklyWorkload(
      @RequestParam(required = false, defaultValue = "0") Integer weekOffset,
      Authentication authentication) {

    Long employeeId = authUtil.getUserIdFromAuth(authentication);

    // Calculate week range
    LocalDate today = LocalDate.now();
    LocalDate weekStart = today.with(DayOfWeek.MONDAY).plusWeeks(weekOffset);
    LocalDate weekEnd = weekStart.plusDays(6);

    // Fetch time logs
    List<TimeLog> timeLogs =
        timeLogRepository.findByEmployee_IdAndDateBetweenOrderByDateAsc(
            employeeId, weekStart, weekEnd);

    // Group by date and sum hours
    Map<LocalDate, BigDecimal> hoursByDate =
        timeLogs.stream()
            .collect(
                Collectors.groupingBy(
                    TimeLog::getDate,
                    Collectors.reducing(BigDecimal.ZERO, TimeLog::getHours, BigDecimal::add)));

    // Build result for all 7 days
    List<WeeklyWorkloadDTO> workload = new ArrayList<>();
    String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    for (int i = 0; i < 7; i++) {
      LocalDate date = weekStart.plusDays(i);
      BigDecimal hours = hoursByDate.getOrDefault(date, BigDecimal.ZERO);

      WeeklyWorkloadDTO dto =
          new WeeklyWorkloadDTO(dayNames[i], date, hours, date.getDayOfWeek().getValue());
      workload.add(dto);
    }

    return ResponseEntity.ok(workload);
  }

  // 5. GET /api/employee/dashboard/task-distribution - Get task distribution pie chart
  @GetMapping("/task-distribution")
  public ResponseEntity<List<TaskDistributionDTO>> getTaskDistribution(
      @RequestParam(required = false, defaultValue = "month") String period,
      Authentication authentication) {

    Long employeeId = authUtil.getUserIdFromAuth(authentication);

    // Calculate date range
    LocalDate today = LocalDate.now();
    LocalDate startDate;
    LocalDate endDate;

    switch (period.toLowerCase()) {
      case "week":
        startDate = today.with(DayOfWeek.MONDAY);
        endDate = today.with(DayOfWeek.SUNDAY);
        break;
      case "year":
        startDate = today.withDayOfYear(1);
        endDate = today.withDayOfYear(today.lengthOfYear());
        break;
      case "month":
      default:
        startDate = today.withDayOfMonth(1);
        endDate = today.withDayOfMonth(today.lengthOfMonth());
        break;
    }

    // DEBUG: Log date range and employee ID
    logger.info("🔍 Task Distribution Debug:");
    logger.info("   Employee ID: {}", employeeId);
    logger.info("   Start Date: {}", startDate);
    logger.info("   End Date: {}", endDate);
    logger.info("   Period: {}", period);

    // Fetch appointments in range
    List<Appointment> appointments =
        appointmentRepository.findByAssignedEmployee_IdAndDateBetween(
            employeeId, startDate, endDate);

    // DEBUG: Log results
    logger.info("   Appointments Found: {}", appointments.size());
    if (!appointments.isEmpty()) {
      logger.info("   Sample appointments:");
      appointments.stream()
          .limit(3)
          .forEach(a -> logger.info("      - {} on {}", a.getService(), a.getDate()));
    }

    // Group by service and count
    Map<String, Long> countByService =
        appointments.stream()
            .collect(Collectors.groupingBy(Appointment::getService, Collectors.counting()));

    // Calculate total
    long total = appointments.size();

    // Define colors (matching frontend)
    Map<String, String> colorMap = new HashMap<>();
    colorMap.put("Oil Change", "#3b82f6");
    colorMap.put("Brake Service", "#10b981");
    colorMap.put("Tire Rotation", "#f59e0b");
    colorMap.put("Inspection", "#ef4444");
    colorMap.put("Custom Project", "#8b5cf6");
    colorMap.put("AC Service", "#06b6d4");
    String defaultColor = "#6b7280";

    // Build DTOs
    List<TaskDistributionDTO> distribution =
        countByService.entrySet().stream()
            .map(
                entry -> {
                  String taskType = entry.getKey();
                  Long count = entry.getValue();
                  Integer percentage = total > 0 ? (int) Math.round(count * 100.0 / total) : 0;

                  return new TaskDistributionDTO(
                      taskType, percentage, colorMap.getOrDefault(taskType, defaultColor));
                })
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .collect(Collectors.toList());

    return ResponseEntity.ok(distribution);
  }

  // 6. PUT /api/employee/jobs/{jobId}/status - Update job status
  @PutMapping("/jobs/{jobId}/status")
  public ResponseEntity<?> updateJobStatus(
      @PathVariable Long jobId,
      @Valid @RequestBody UpdateJobStatusRequestDTO request,
      Authentication authentication) {

    try {
      Long employeeId = authUtil.getUserIdFromAuth(authentication);

      // Fetch appointment
      Appointment appointment =
          appointmentRepository
              .findById(jobId)
              .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

      // Verify employee is assigned
      // Force load the employee relationship to check assignment
      if (appointment.getAssignedEmployee() == null
          || !appointment.getAssignedEmployee().getId().equals(employeeId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not assigned to this job");
      }

      // Validate and update status
      if (request.getStatus() != null) {
        if (!Arrays.asList("PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED")
            .contains(request.getStatus())) {
          return ResponseEntity.badRequest().body("Invalid status");
        }
        appointment.setStatus(request.getStatus());

        // If completed, set progress to 100
        if ("COMPLETED".equals(request.getStatus())) {
          appointment.setProgress(100);
        }
      }

      // Update progress if provided and not completed
      if (request.getProgress() != null && !"COMPLETED".equals(appointment.getStatus())) {
        if (request.getProgress() < 0 || request.getProgress() > 100) {
          return ResponseEntity.badRequest().body("Progress must be between 0 and 100");
        }
        appointment.setProgress(request.getProgress());
      }

      // Add notes
      if (request.getNotes() != null && !request.getNotes().isEmpty()) {
        String timestamp =
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String existingNotes = appointment.getNotes() != null ? appointment.getNotes() : "";
        String newNote =
            "\n["
                + timestamp
                + "] Progress: "
                + appointment.getProgress()
                + "% - "
                + request.getNotes();
        appointment.setNotes(existingNotes + newNote);
      }

            // Save
            Appointment updated = appointmentRepository.save(appointment);

      // Build response
      JobStatusUpdateResponseDTO response = new JobStatusUpdateResponseDTO();
      response.setJobId(updated.getId());
      response.setStatus(updated.getStatus());
      response.setProgress(updated.getProgress());
      response.setUpdatedAt(updated.getUpdatedAt());
      response.setMessage("Job status updated successfully");

      return ResponseEntity.ok(response);

    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error updating job status: " + e.getMessage());
    }
  }

  // 7. POST /api/employee/jobs/{jobId}/start - Start a job
  @PostMapping("/jobs/{jobId}/start")
  public ResponseEntity<?> startJob(@PathVariable Long jobId, Authentication authentication) {

    try {
      Long employeeId = authUtil.getUserIdFromAuth(authentication);

      // Fetch appointment
      Appointment appointment =
          appointmentRepository
              .findById(jobId)
              .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

      // Verify employee is assigned
      // Force load the employee relationship to check assignment
      if (appointment.getAssignedEmployee() == null
          || !appointment.getAssignedEmployee().getId().equals(employeeId)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not assigned to this job");
      }

      // Check current status
      if ("IN_PROGRESS".equals(appointment.getStatus())) {
        return ResponseEntity.badRequest().body("Job is already in progress");
      }

      if ("COMPLETED".equals(appointment.getStatus())) {
        return ResponseEntity.badRequest().body("Job is already completed");
      }

      // Update status to IN_PROGRESS
      appointment.setStatus("IN_PROGRESS");

      // Save
      Appointment updated = appointmentRepository.save(appointment);

      // Build response
      JobActionResponseDTO response = new JobActionResponseDTO();
      response.setJobId(updated.getId());
      response.setStatus(updated.getStatus());
      response.setMessage("Job started successfully");
      response.setStartedAt(LocalDateTime.now());

      return ResponseEntity.ok(response);

    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error starting job: " + e.getMessage());
    }
  }
}
