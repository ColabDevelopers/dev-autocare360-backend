package com.autocare360.controller;

import com.autocare360.dto.AdminDashboardStatsDTO;
import com.autocare360.dto.AdminAnalyticsResponseDTO;
import com.autocare360.service.AdminDashboardService;
import com.autocare360.service.AdminAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final AdminAnalyticsService adminAnalyticsService;

    @GetMapping("/stats")
    public ResponseEntity<AdminDashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(adminDashboardService.getDashboardStats());
    }

    @GetMapping("/analytics")
    public ResponseEntity<AdminAnalyticsResponseDTO> getAnalytics(
            @RequestParam(defaultValue = "day") String period) {
        return ResponseEntity.ok(adminAnalyticsService.getAnalytics(period));
    }
}