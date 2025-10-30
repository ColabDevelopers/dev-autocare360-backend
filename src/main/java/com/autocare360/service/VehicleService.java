package com.autocare360.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.autocare360.entity.Vehicle;
import com.autocare360.repo.VehicleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public boolean existsByVinAndUserId(String vin, Long userId) {
        return vehicleRepository.existsByVinAndUser_Id(vin, userId);
    }

    public Vehicle create(Vehicle v) {
        return vehicleRepository.save(v);
    }

    public Vehicle get(Long id) {
        return vehicleRepository.findById(id).orElse(null);
    }

    public List<Vehicle> listByUser(Long userId) {
        return vehicleRepository.findByUser_Id(userId);
    }

    public Vehicle update(Long id, Vehicle patch) {
        return vehicleRepository.findById(id).map(existing -> {
            if (patch.getMake() != null) existing.setMake(patch.getMake());
            if (patch.getModel() != null) existing.setModel(patch.getModel());
            if (patch.getYear() != 0) existing.setYear(patch.getYear());
            if (patch.getVin() != null) existing.setVin(patch.getVin());
            if (patch.getPlateNumber() != null) existing.setPlateNumber(patch.getPlateNumber());
            if (patch.getColor() != null) existing.setColor(patch.getColor());
            return vehicleRepository.save(existing);
        }).orElse(null);
    }

    public void delete(Long id) {
        vehicleRepository.deleteById(id);
    }

    public Vehicle linkVehicleToUser(Long vehicleId, Long userId) {
        Vehicle v = vehicleRepository.findById(vehicleId).orElse(null);
        if (v == null) return null;
        v.setUserId(userId);
        return vehicleRepository.save(v);
    }

    public List<Vehicle> listAll() {
        return vehicleRepository.findAll();
    }
}

