package com.autocare360.repo;

import com.autocare360.entity.ServiceTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ServiceTypeRepository extends JpaRepository<ServiceTypeEntity, Long> {

    @Query("SELECT COALESCE(SUM(s.percentage),0) FROM ServiceTypeEntity s")
    int totalPercentage();
}
