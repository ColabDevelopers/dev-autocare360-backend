package com.autocare360.repo;

import com.autocare360.entity.ServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {

    // FIXED: Changed from findByVehicle_Id to findByVehicleId
    List<ServiceRecord> findByVehicleIdOrderByRequestedAtDesc(Long vehicleId);

    List<ServiceRecord> findByStatus(String status);

    List<ServiceRecord> findByVehicleId(Long vehicleId);
}