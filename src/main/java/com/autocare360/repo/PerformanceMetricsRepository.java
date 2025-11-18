package com.autocare360.repo;

import com.autocare360.entity.PerformanceMetricsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PerformanceMetricsRepository extends JpaRepository<PerformanceMetricsEntity, Long> {

    @Query("SELECT COALESCE(AVG(m.value),0) FROM PerformanceMetricsEntity m")
    double averageSatisfaction();
}
