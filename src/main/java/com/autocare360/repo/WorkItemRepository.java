package com.autocare360.repo;

import com.autocare360.entity.WorkItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {
    
    // Find work items by customer
    List<WorkItem> findByCustomerId(Long customerId);
    
    // Find work items by status
    List<WorkItem> findByStatus(String status);
    
    // Find active work items (not completed or cancelled)
    @Query("SELECT w FROM WorkItem w WHERE w.status NOT IN ('completed', 'cancelled')")
    List<WorkItem> findActiveWorkItems();
    
    // Find work items by type
    List<WorkItem> findByType(String type);
    
    // Count active work items
    @Query("SELECT COUNT(w) FROM WorkItem w WHERE w.status NOT IN ('completed', 'cancelled')")
    Long countActiveWorkItems();
    
    // Find work items created in date range
    @Query("SELECT w FROM WorkItem w WHERE w.createdAt >= :startDate AND w.createdAt <= :endDate")
    List<WorkItem> findByCreatedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Count completed work items in date range
    @Query("SELECT COUNT(w) FROM WorkItem w WHERE w.status = 'completed' " +
           "AND w.updatedAt >= :startDate AND w.updatedAt <= :endDate")
    Long countCompletedInDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}