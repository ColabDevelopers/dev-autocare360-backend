package com.autocare360.service;

import com.autocare360.dto.AdminAnalyticsResponseDTO;
import com.autocare360.dto.AdminAnalyticsResponseDTO.*;
import com.autocare360.entity.AdminAnalytics;
import com.autocare360.repo.AdminAnalyticsRepository;
import com.autocare360.repo.AppointmentRepository;
import com.autocare360.repo.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {

    private final AdminAnalyticsRepository adminAnalyticsRepository;
    private final AppointmentRepository appointmentRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public void generateDailyAnalytics() {
        LocalDate today = LocalDate.now();
        if (adminAnalyticsRepository.existsByReportDateAndReportType(today, "DAILY")) {
            return; // Analytics already generated for today
        }

        AdminAnalytics analytics = new AdminAnalytics();
        analytics.setReportDate(today);
        analytics.setReportType("DAILY");
        
        // Calculate statistics
        analytics.setTotalEmployees(Math.toIntExact(employeeRepository.count()));
        analytics.setActiveEmployees(employeeRepository.countByActiveTrue());
        
        // Appointment stats
        analytics.setTotalAppointments(Math.toIntExact(appointmentRepository.count()));
        analytics.setCompletedAppointments(appointmentRepository.countByStatus("COMPLETED"));
        analytics.setPendingAppointments(appointmentRepository.countByStatus("PENDING"));
        analytics.setInProgressAppointments(appointmentRepository.countByStatus("IN_PROGRESS"));
        
        // Calculate averages
        analytics.setAverageCompletionTime(
                appointmentRepository.calculateAverageCompletionTime().orElse(BigDecimal.ZERO)
        );
        
        // Calculate utilization rate
        BigDecimal utilizationRate = calculateUtilizationRate();
        analytics.setEmployeeUtilizationRate(utilizationRate);

        adminAnalyticsRepository.save(analytics);
    }

    @Transactional(readOnly = true)
    public AdminAnalyticsResponseDTO getAnalytics(String period) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        String reportType;

        switch (period.toLowerCase()) {
            case "week":
                startDate = today.minusDays(7);
                reportType = "DAILY";
                break;
            case "month":
                startDate = today.minusDays(30);
                reportType = "DAILY";
                break;
            default:
                startDate = today;
                reportType = "DAILY";
        }

        // Get current stats
        Optional<AdminAnalytics> latestAnalytics = 
                adminAnalyticsRepository.findFirstByReportTypeOrderByReportDateDesc(reportType);

        if (latestAnalytics.isEmpty()) {
            return new AdminAnalyticsResponseDTO(); // Return empty response
        }

        AdminAnalytics current = latestAnalytics.get();

        // Build response
        AdminAnalyticsResponseDTO response = new AdminAnalyticsResponseDTO();
        response.setReportDate(current.getReportDate());
        response.setReportType(current.getReportType());

        // Set current stats
        response.setCurrentStats(new AnalyticsStats(
                current.getTotalEmployees(),
                current.getActiveEmployees(),
                current.getTotalAppointments(),
                current.getCompletedAppointments(),
                current.getPendingAppointments(),
                current.getInProgressAppointments(),
                current.getAverageCompletionTime(),
                current.getEmployeeUtilizationRate()
        ));

        // Get trend data
        List<Object[]> utilizationTrend = adminAnalyticsRepository.getUtilizationTrend(
                startDate, today, reportType);
        
        List<TrendPoint> trends = new ArrayList<>();
        for (Object[] point : utilizationTrend) {
            LocalDate date = (LocalDate) point[0];
            BigDecimal value = (BigDecimal) point[1];
            trends.add(new TrendPoint(date, value, "utilization"));
        }
        response.setTrends(trends);

        // Calculate comparison stats
        response.setComparison(calculateComparison(period));

        return response;
    }

    private BigDecimal calculateUtilizationRate() {
        int activeEmployees = employeeRepository.countByActiveTrue();
        if (activeEmployees == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalWorkedHours = appointmentRepository.calculateAverageCompletionTime()
                .orElse(BigDecimal.ZERO);
        
        BigDecimal expectedHours = BigDecimal.valueOf(40 * activeEmployees); // 40 hours per week
        
        if (expectedHours.compareTo(BigDecimal.ZERO) > 0) {
            return totalWorkedHours
                    .divide(expectedHours, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        
        return BigDecimal.ZERO;
    }

    private ComparisonStats calculateComparison(String period) {
        LocalDate today = LocalDate.now();
        LocalDate previousPeriodStart;
        LocalDate currentPeriodStart;

        switch (period.toLowerCase()) {
            case "week":
                currentPeriodStart = today.minusDays(7);
                previousPeriodStart = today.minusDays(14);
                break;
            case "month":
                currentPeriodStart = today.minusDays(30);
                previousPeriodStart = today.minusDays(60);
                break;
            default:
                currentPeriodStart = today;
                previousPeriodStart = today.minusDays(1);
        }

        List<AdminAnalytics> currentPeriod = adminAnalyticsRepository
                .findByReportDateBetweenAndReportTypeOrderByReportDateDesc(
                        currentPeriodStart, today, "DAILY");
        
        List<AdminAnalytics> previousPeriod = adminAnalyticsRepository
                .findByReportDateBetweenAndReportTypeOrderByReportDateDesc(
                        previousPeriodStart, currentPeriodStart.minusDays(1), "DAILY");

        // Calculate changes
        BigDecimal utilizationChange = calculatePercentageChange(
                getAverageUtilization(currentPeriod),
                getAverageUtilization(previousPeriod)
        );

        BigDecimal completionRateChange = calculatePercentageChange(
                getCompletionRate(currentPeriod),
                getCompletionRate(previousPeriod)
        );

        Integer appointmentsChange = calculateAppointmentsChange(currentPeriod, previousPeriod);

        return new ComparisonStats(
                utilizationChange,
                completionRateChange,
                appointmentsChange,
                "vs last " + period
        );
    }

    private BigDecimal getAverageUtilization(List<AdminAnalytics> analytics) {
        if (analytics.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return analytics.stream()
                .map(AdminAnalytics::getEmployeeUtilizationRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(analytics.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getCompletionRate(List<AdminAnalytics> analytics) {
        if (analytics.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int totalCompleted = analytics.stream()
                .mapToInt(AdminAnalytics::getCompletedAppointments)
                .sum();
        
        int totalAppointments = analytics.stream()
                .mapToInt(AdminAnalytics::getTotalAppointments)
                .sum();

        if (totalAppointments == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(totalCompleted)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalAppointments), 2, RoundingMode.HALF_UP);
    }

    private Integer calculateAppointmentsChange(
            List<AdminAnalytics> current,
            List<AdminAnalytics> previous
    ) {
        int currentTotal = current.stream()
                .mapToInt(AdminAnalytics::getTotalAppointments)
                .sum();
        
        int previousTotal = previous.stream()
                .mapToInt(AdminAnalytics::getTotalAppointments)
                .sum();

        return currentTotal - previousTotal;
    }

    private BigDecimal calculatePercentageChange(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? 
                    BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }

        return current.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, RoundingMode.HALF_UP);
    }
}