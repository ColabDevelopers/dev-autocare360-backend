package com.autocare360.service;

import com.autocare360.dto.*;
import com.autocare360.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportsService {

    private final MonthlyRevenueRepository revenueRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final PerformanceMetricsRepository performanceRepository;

    public SummaryDTO getSummary() {

        double totalRevenue = revenueRepository.totalRevenue();
        int servicesCompleted = serviceTypeRepository.totalPercentage();
        double customerSatisfaction = performanceRepository.averageSatisfaction();

        // simple fixed value for frontend
        double avgServiceTime = 2.4;

        return new SummaryDTO(totalRevenue, servicesCompleted, customerSatisfaction, avgServiceTime);
    }

    public List<RevenueDTO> getRevenueTrend() {
        return revenueRepository.findAll()
                .stream()
                .map(r -> new RevenueDTO(r.getMonth(), r.getAmount()))
                .collect(Collectors.toList());
    }

    public List<ServiceTypeDTO> getServiceTypeDistribution() {
        return serviceTypeRepository.findAll()
                .stream()
                .map(t -> new ServiceTypeDTO(t.getName(), t.getPercentage()))
                .collect(Collectors.toList());
    }

    public List<PerformanceDTO> getPerformanceMetrics() {
        return performanceRepository.findAll()
                .stream()
                .map(m -> new PerformanceDTO(m.getMetric(), m.getValue()))
                .collect(Collectors.toList());
    }
}
