package com.autocare360.controller;

import java.util.List;
import java.util.Map;

import com.autocare360.dto.VehicleDTO;
import com.autocare360.entity.Vehicle;
import com.autocare360.security.JwtService;
import com.autocare360.service.VehicleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final JwtService jwtService;

    private Long extractUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) return null;
        String token = authorization.substring(7);
        if (!jwtService.isTokenValid(token)) return null;
        return Long.valueOf(jwtService.extractSubject(token));
    }

    private VehicleDTO mapToDTO(Vehicle v) {
        return new VehicleDTO(
                v.getId(),
                v.getVin(),
                v.getMake(),
                v.getModel(),
                v.getYear(),
                v.getPlateNumber(),
                v.getColor()
        );
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = extractUserId(auth);
        if (userId == null) return ResponseEntity.status(401).build();

        boolean isAdmin = jwtService.hasRole(auth, "ADMIN");

        List<Vehicle> vehicles = isAdmin
                ? vehicleService.listAll()
                : vehicleService.listByUser(userId);

        List<VehicleDTO> dtos = vehicles.stream()
                .map(this::mapToDTO)
                .toList();

        return ResponseEntity.ok(Map.of(
                "items", dtos,
                "total", dtos.size(),
                "page", 1,
                "perPage", dtos.size()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleDTO> get(@PathVariable Long id) {
        Vehicle v = vehicleService.get(id);
        if (v == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(mapToDTO(v));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader(value = "Authorization", required = false) String auth,
                                    @Valid @RequestBody Vehicle v) {

        Long userId = extractUserId(auth);
        if (userId == null) return ResponseEntity.status(401).build();

        // prevent duplicate VIN for the same user
        if (vehicleService.existsByVinAndUserId(v.getVin(), userId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "You already have a vehicle with this VIN"));
        }

        v.setUserId(userId);
        Vehicle created = vehicleService.create(v);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleDTO> update(@PathVariable Long id,
                                             @RequestBody Map<String, Object> body) {
        Vehicle patch = new Vehicle();
        if (body.containsKey("make")) patch.setMake((String) body.get("make"));
        if (body.containsKey("model")) patch.setModel((String) body.get("model"));
        if (body.containsKey("year")) {
            Object yearVal = body.get("year");
            if (yearVal instanceof Number) patch.setYear(((Number) yearVal).intValue());
        }
        if (body.containsKey("vin")) patch.setVin((String) body.get("vin"));
        if (body.containsKey("plateNumber")) patch.setPlateNumber((String) body.get("plateNumber"));
        if (body.containsKey("color")) patch.setColor((String) body.get("color"));

        Vehicle updated = vehicleService.update(id, patch);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(mapToDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/link")
    public ResponseEntity<?> linkVehicle(@PathVariable Long id,
                                         @RequestHeader(value = "Authorization", required = false) String auth) {

        Long userId = extractUserId(auth);
        if (userId == null) return ResponseEntity.status(401).build();

        Vehicle v = vehicleService.get(id);
        if (v == null) return ResponseEntity.notFound().build();

        if (vehicleService.existsByVinAndUserId(v.getVin(), userId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "This vehicle is already linked to the user"));
        }

        Vehicle linked = vehicleService.linkVehicleToUser(id, userId);
        return ResponseEntity.ok(mapToDTO(linked));
    }
}
