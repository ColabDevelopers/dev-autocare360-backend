package com.autocare360.repo;

import com.autocare360.entity.AdminAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminAnalyticsRepository extends JpaRepository<AdminAnalytics, Long> {
    
    // Find latest analytics report by type
    Optional<AdminAnalytics> findFirstByReportTypeOrderByReportDateDesc(String reportType);
    
    // Find analytics for a specific date range
    List<AdminAnalytics> findByReportDateBetweenAndReportTypeOrderByReportDateDesc(
            LocalDate startDate, LocalDate endDate, String reportType);
    
    // Get trend data for a specific metric
    @Query("SELECT a.reportDate, a.employeeUtilizationRate FROM AdminAnalytics a " +
           "WHERE a.reportDate BETWEEN :startDate AND :endDate " +
           "AND a.reportType = :reportType " +
           "ORDER BY a.reportDate")
    List<Object[]> getUtilizationTrend(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("reportType") String reportType);
    
    // Check if analytics exists for a specific date and type
    boolean existsByReportDateAndReportType(LocalDate reportDate, String reportType);
}