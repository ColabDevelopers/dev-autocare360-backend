package com.autocare360.repo;

import com.autocare360.entity.WorkItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {
    
    List<WorkItem> findByCustomerId(Long customerId);
    
    List<WorkItem> findByStatus(String status);
    
    List<WorkItem> findByType(String type);
    
    @Query("SELECT COUNT(w) FROM WorkItem w WHERE w.status NOT IN ('completed', 'cancelled')")
    Long countActiveWorkItems();
    
    @Query("SELECT w FROM WorkItem w WHERE w.status = :status AND w.type = :type")
    List<WorkItem> findByStatusAndType(@Param("status") String status, @Param("type") String type);
}
