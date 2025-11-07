package com.autocare360.service;

import com.autocare360.dto.EmployeeScheduleDto;
import com.autocare360.dto.DailyScheduleDto;
import com.autocare360.dto.ScheduledTaskDto;
import com.autocare360.dto.EmployeeListDto;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeScheduleService {

    private final EmployeeRepository employeeRepository;
    private final AppointmentRepository appointmentRepository;
    private final TimeLogRepository timeLogRepository;

    @Transactional(readOnly = true)
    public List<EmployeeListDto> getAllEmployees() {
        List<Employee> employees = employeeRepository.findByActiveTrue();
        
        return employees.stream()
                .map(this::convertToEmployeeListDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeScheduleDto getEmployeeSchedule(Long employeeId, int days) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(days);

        List<Appointment> appointments = appointmentRepository
                .findByAssignedEmployee_IdAndDateBetween(employeeId, today, futureDate);

        // Group appointments by date
        Map<LocalDate, List<Appointment>> appointmentsByDate = appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getDate));

        // Create daily schedules
        List<DailyScheduleDto> dailySchedules = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            List<Appointment> dayAppointments = appointmentsByDate.getOrDefault(date, new ArrayList<>());
            
            DailyScheduleDto daySchedule = createDailySchedule(date, dayAppointments);
            dailySchedules.add(daySchedule);
        }

        return EmployeeScheduleDto.builder()
                .employeeId(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .department(employee.getSpecialization() != null ? employee.getSpecialization() : "General")
                .status(employee.getStatus())
                .upcomingSchedule(dailySchedules)
                .build();
    }

    private DailyScheduleDto createDailySchedule(LocalDate date, List<Appointment> appointments) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        List<ScheduledTaskDto> tasks = appointments.stream()
                .map(this::convertToScheduledTask)
                .collect(Collectors.toList());

        Double totalHours = appointments.stream()
                .mapToDouble(a -> {
                    if (a.getEstimatedHours() != null) {
                        return a.getEstimatedHours().doubleValue();
                    }
                    return 2.0; // Default 2 hours
                })
                .sum();

        return DailyScheduleDto.builder()
                .date(date.format(dateFormatter))
                .dayOfWeek(dayOfWeek)
                .tasks(tasks)
                .totalHours(totalHours)
                .build();
    }

    private ScheduledTaskDto convertToScheduledTask(Appointment appointment) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        
        String vehicleInfo = appointment.getVehicle() != null
                ? appointment.getVehicle()
                : "N/A";

        LocalTime startTime = appointment.getTime() != null 
                ? appointment.getTime() 
                : LocalTime.of(9, 0);
        
        Integer estimatedHours = appointment.getEstimatedHours() != null 
                ? appointment.getEstimatedHours().intValue() 
                : 2;
        
        LocalTime endTime = startTime.plusHours(estimatedHours);

        return ScheduledTaskDto.builder()
                .taskId(appointment.getId())
                .taskName(appointment.getService())
                .startTime(startTime.format(timeFormatter))
                .endTime(endTime.format(timeFormatter))
                .duration(estimatedHours + "h")
                .status(appointment.getStatus())
                .priority("MEDIUM") // Default priority
                .customerName(appointment.getUser().getName())
                .vehicleInfo(vehicleInfo)
                .build();
    }

    private EmployeeListDto convertToEmployeeListDto(Employee employee) {
        Integer activeCount = appointmentRepository
                .countByEmployeeIdAndStatusIn(employee.getId(), 
                    List.of("PENDING", "IN_PROGRESS", "SCHEDULED"));

        LocalDate weekStart = LocalDate.now().minusDays(7);
        BigDecimal hoursLogged = timeLogRepository
                .sumHoursByEmployeeAndDateRange(employee.getId(), weekStart, LocalDate.now());
        
        Double capacity = hoursLogged != null 
                ? (hoursLogged.doubleValue() / 40.0) * 100.0 
                : 0.0;

        return EmployeeListDto.builder()
                .id(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .department(employee.getSpecialization() != null ? employee.getSpecialization() : "General")
                .status(employee.getStatus())
                .activeTaskCount(activeCount)
                .capacityUtilization(capacity)
                .build();
    }
}