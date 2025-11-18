package com.autocare360.repo;

import com.autocare360.entity.MonthlyRevenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MonthlyRevenueRepository extends JpaRepository<MonthlyRevenue, Long> {

    @Query("SELECT COALESCE(SUM(m.amount),0) FROM MonthlyRevenue m")
    double totalRevenue();
}
