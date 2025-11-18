package com.autocare360.controller;

import com.autocare360.dto.CapacityDistributionDto;
import com.autocare360.dto.TeamStatusDto;
import com.autocare360.dto.WorkloadResponse;
import com.autocare360.dto.WorkloadSummaryDto;
import com.autocare360.service.WorkloadMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/workload")
@RequiredArgsConstructor
public class WorkloadMonitoringController {

    private final WorkloadMonitoringService workloadService;

    @GetMapping("/summary")
    public WorkloadSummaryDto getSummary() {
        return workloadService.getWorkloadSummary();
    }

    @GetMapping("/team")
    public List<WorkloadResponse> getTeamWorkload() {
        return workloadService.getAllEmployeeWorkloads();
    }

    @GetMapping("/capacity")
    public List<CapacityDistributionDto> getCapacityDistribution() {
        return workloadService.getCapacityDistribution();
    }

    @GetMapping("/team-status")
    public TeamStatusDto getTeamStatus() {
        return workloadService.getTeamStatus();
    }

    @GetMapping("/overloaded")
    public List<WorkloadResponse> getOverloadedEmployees() {
        return workloadService.getOverloadedEmployees();
    }

    @GetMapping("/available")
    public List<WorkloadResponse> getAvailableEmployees() {
        return workloadService.getAvailableEmployees();
    }

    @GetMapping("/department/{department}")
    public List<WorkloadResponse> getByDepartment(@PathVariable String department) {
        return workloadService.getWorkloadByDepartment(department);
    }
}
