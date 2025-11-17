package com.autocare360.repo;

import com.autocare360.entity.Vehicle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
  // Check if a vehicle with this VIN exists for a specific user
  boolean existsByVinAndUser_Id(String vin, Long userId);

  List<Vehicle> findByUser_Id(Long userId);
  
  // Count vehicles by user
  long countByUser_Id(Long userId);
}
