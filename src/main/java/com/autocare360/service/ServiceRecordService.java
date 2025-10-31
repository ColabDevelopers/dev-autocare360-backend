package com.autocare360.service;

import com.autocare360.entity.ServiceRecord;
import com.autocare360.repo.ServiceRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServiceRecordService {

    private final ServiceRecordRepository repo;

    public List<ServiceRecord> listAll() {
        return repo.findAll();
    }

    public List<ServiceRecord> listForVehicle(Long vehicleId) {
        // FIXED: Changed from findByVehicle_IdOrderByRequestedAtDesc
        return repo.findByVehicleIdOrderByRequestedAtDesc(vehicleId);
    }

    public List<ServiceRecord> listByStatus(String status) {
        // FIXED: Changed from findByStatusOrderByRequestedAtDesc
        return repo.findByStatus(status);
    }

    public ServiceRecord get(Long id) {
        return repo.findById(id).orElse(null);
    }

    public ServiceRecord create(ServiceRecord s) {
        return repo.save(s);
    }

    public ServiceRecord update(Long id, ServiceRecord patch) {
        ServiceRecord existing = repo.findById(id).orElse(null);
        if (existing == null) return null;

        if (patch.getName() != null) existing.setName(patch.getName());
        if (patch.getType() != null) existing.setType(patch.getType());
        if (patch.getStatus() != null) existing.setStatus(patch.getStatus());
        if (patch.getScheduledAt() != null) existing.setScheduledAt(patch.getScheduledAt());
        if (patch.getNotes() != null) existing.setNotes(patch.getNotes());
        if (patch.getPrice() != null) existing.setPrice(patch.getPrice());
        if (patch.getDuration() != null) existing.setDuration(patch.getDuration());

        return repo.save(existing);
    }

    public boolean delete(Long id) {
        Optional<ServiceRecord> record = repo.findById(id);
        if (record.isPresent()) {
            repo.delete(record.get());
            return true;
        }
        return false;
    }

}