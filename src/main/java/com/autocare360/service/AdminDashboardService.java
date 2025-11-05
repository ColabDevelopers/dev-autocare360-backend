package com.autocare360.service;

import com.autocare360.dto.AdminDashboardStatsDTO;
import com.autocare360.repo.AppointmentRepository;
import com.autocare360.repo.EmployeeRepository;
import com.autocare360.repo.UserRepository;
import com.autocare360.repo.TimeLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final EmployeeRepository employeeRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final TimeLogRepository timeLogRepository;

    @Transactional(readOnly = true)
    public AdminDashboardStatsDTO getDashboardStats() {
        // Calculate basic stats
        Integer totalEmployees = Math.toIntExact(employeeRepository.count());
        Integer activeEmployees = employeeRepository.countByActiveTrue();
        Integer totalCustomers = userRepository.countByRoleName("CUSTOMER");
        
        // Calculate appointment stats
        Integer totalAppointments = Math.toIntExact(appointmentRepository.count());
        Integer pendingAppointments = appointmentRepository.countByStatus("PENDING");
        Integer inProgressAppointments = appointmentRepository.countByStatus("IN_PROGRESS");
        Integer completedAppointments = appointmentRepository.countByStatus("COMPLETED");

        // Calculate active projects (appointments that are either pending or in progress)
        Integer activeProjects = appointmentRepository
                .countByStatusIn(Arrays.asList("PENDING", "IN_PROGRESS"));

        // Calculate average completion time in hours
        BigDecimal averageCompletionTime = appointmentRepository
                .calculateAverageCompletionTime()
                .orElse(BigDecimal.ZERO);

        // Calculate employee utilization rate
        Double employeeUtilizationRate = calculateEmployeeUtilizationRate();

        return new AdminDashboardStatsDTO(
                totalEmployees,
                activeEmployees,
                totalAppointments,
                pendingAppointments,
                inProgressAppointments,
                completedAppointments,
                averageCompletionTime,
                totalCustomers,
                employeeUtilizationRate,
                activeProjects
        );
    }

    private Double calculateEmployeeUtilizationRate() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(7);
        
        // Get total active employees
        int activeEmployees = employeeRepository.countByActiveTrue();
        if (activeEmployees == 0) {
            return 0.0;
        }

        // Calculate total worked hours this week
        BigDecimal totalWorkedHours = timeLogRepository
                .sumHoursByDateRange(weekStart, today);

        // Calculate expected hours (8 hours per day * 5 working days * number of active employees)
        BigDecimal expectedHours = BigDecimal.valueOf(40 * activeEmployees);

        // Calculate utilization rate
        if (expectedHours.compareTo(BigDecimal.ZERO) > 0) {
            return totalWorkedHours
                    .divide(expectedHours, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        return 0.0;
    }
}