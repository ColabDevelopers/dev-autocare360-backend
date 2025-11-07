package com.autocare360.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autocare360.entity.WorkItem;

@Repository
public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {
}
