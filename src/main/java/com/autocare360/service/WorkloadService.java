package com.autocare360.service;

import com.autocare360.dto.AssignTaskRequest;
import com.autocare360.dto.CapacityMetrics;
import com.autocare360.dto.WorkloadResponse;

import java.util.List;

public interface WorkloadService {
    
    /**
     * Get workload information for all employees
     */
    List<WorkloadResponse> getAllEmployeeWorkloads();
    
    /**
     * Get workload information for a specific employee
     */
    WorkloadResponse getEmployeeWorkload(Long employeeId);
    
    /**
     * Get overall capacity metrics for the team
     */
    CapacityMetrics getCapacityMetrics();
    
    /**
     * Assign a work item to an employee
     */
    void assignTask(AssignTaskRequest request);
    
    /**
     * Get employee availability information
     */
    List<WorkloadResponse> getEmployeeAvailability();
}