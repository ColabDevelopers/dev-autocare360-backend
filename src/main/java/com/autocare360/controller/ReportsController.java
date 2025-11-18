package com.autocare360.controller;

import com.autocare360.dto.*;
import com.autocare360.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/summary")
    public SummaryDTO getSummary() {
        return reportsService.getSummary();
    }

    @GetMapping("/revenue-trend")
    public List<RevenueDTO> getRevenueTrend() {
        return reportsService.getRevenueTrend();
    }

    @GetMapping("/service-type-distribution")
    public List<ServiceTypeDTO> getServiceTypeDistribution() {
        return reportsService.getServiceTypeDistribution();
    }

    @GetMapping("/performance")
    public List<PerformanceDTO> getPerformanceMetrics() {
        return reportsService.getPerformanceMetrics();
    }
}
