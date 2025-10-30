package com.autocare360.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autocare360.entity.ServiceRecord;

@Repository
public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {
    List<ServiceRecord> findByVehicle_IdOrderByRequestedAtDesc(Long vehicleId);
    List<ServiceRecord> findByStatusOrderByRequestedAtDesc(String status);
}
