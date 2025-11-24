package com.autocare360.repo;

import com.autocare360.entity.ProjectRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProjectRequestRepository extends JpaRepository<ProjectRequest, Long> {
    
    // Find by customer
    List<ProjectRequest> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);
    
    // Find by status
    List<ProjectRequest> findByStatusOrderByCreatedAtDesc(String status);
    
    // Find by project type
    List<ProjectRequest> findByProjectTypeOrderByCreatedAtDesc(String projectType);
    
    // Find by assigned employee
    List<ProjectRequest> findByAssignedEmployee_IdOrderByCreatedAtDesc(Long employeeId);
    
    // Find by priority
    List<ProjectRequest> findByPriorityOrderByCreatedAtDesc(String priority);
    
    // Find by multiple statuses
    List<ProjectRequest> findByStatusInOrderByCreatedAtDesc(List<String> statuses);
    
    // Find projects created within date range
    List<ProjectRequest> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find pending projects older than specified days
    @Query("SELECT pr FROM ProjectRequest pr WHERE pr.status = 'PENDING' AND pr.createdAt < :cutoffDate")
    List<ProjectRequest> findPendingProjectsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Search projects by name or description
    @Query("SELECT pr FROM ProjectRequest pr WHERE " +
           "LOWER(pr.projectName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pr.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY pr.createdAt DESC")
    List<ProjectRequest> searchByNameOrDescription(@Param("searchTerm") String searchTerm);
    
    // Get projects by customer with pagination
    Page<ProjectRequest> findByCustomer_IdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
    
    // Get projects by status with pagination
    Page<ProjectRequest> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    
    // Find projects with estimated cost range
    @Query("SELECT pr FROM ProjectRequest pr WHERE " +
           "(pr.estimatedCost >= :minCost OR :minCost IS NULL) AND " +
           "(pr.estimatedCost <= :maxCost OR :maxCost IS NULL) " +
           "ORDER BY pr.createdAt DESC")
    List<ProjectRequest> findByEstimatedCostRange(@Param("minCost") Double minCost, @Param("maxCost") Double maxCost);
    
    // Count projects by status
    Long countByStatus(String status);
    
    // Count projects by customer
    Long countByCustomer_Id(Long customerId);
}