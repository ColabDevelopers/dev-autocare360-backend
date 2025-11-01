package com.autocare360.repo;

import com.autocare360.entity.JobAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobAssignmentRepository extends JpaRepository<JobAssignment, Long> {
    
    List<JobAssignment> findByEmployeeIdAndActiveTrue(Long employeeId);
    
    List<JobAssignment> findByWorkItemIdAndActiveTrue(Long workItemId);
    
    @Query("SELECT ja FROM JobAssignment ja WHERE ja.employeeId = :employeeId AND ja.active = true")
    List<JobAssignment> findActiveByEmployeeId(@Param("employeeId") Long employeeId);
    
    @Query("SELECT ja FROM JobAssignment ja WHERE ja.workItemId = :workItemId AND ja.employeeId = :employeeId AND ja.active = true")
    JobAssignment findActiveByWorkItemIdAndEmployeeId(
        @Param("workItemId") Long workItemId, 
        @Param("employeeId") Long employeeId
    );
}
