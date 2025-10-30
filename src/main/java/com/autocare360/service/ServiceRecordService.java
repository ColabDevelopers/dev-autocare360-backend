package com.autocare360.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.autocare360.entity.ServiceRecord;
import com.autocare360.repo.ServiceRecordRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceRecordService {

    private final ServiceRecordRepository repo;

    public List<ServiceRecord> listForVehicle(Long vehicleId) {
        return repo.findByVehicleIdOrderByRequestedAtDesc(vehicleId);
    }

    public List<ServiceRecord> listByStatus(String status) {
        return repo.findByStatusOrderByRequestedAtDesc(status);
    }

    public ServiceRecord get(Long id) {
        return repo.findById(id).orElse(null);
    }

    public ServiceRecord create(ServiceRecord s) {
        return repo.save(s);
    }

    public ServiceRecord update(Long id, ServiceRecord patch) {
        return repo.findById(id).map(existing -> {
            if (patch.getType() != null) existing.setType(patch.getType());
            if (patch.getStatus() != null) existing.setStatus(patch.getStatus());
            if (patch.getScheduledAt() != null) existing.setScheduledAt(patch.getScheduledAt());
            if (patch.getNotes() != null) existing.setNotes(patch.getNotes());
            return repo.save(existing);
        }).orElse(null);
    }

    public void delete(Long id) { repo.deleteById(id); }
}
