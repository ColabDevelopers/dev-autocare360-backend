package com.autocare360.controller;

import java.util.List;
import java.util.Map;

import com.autocare360.entity.Vehicle;
import com.autocare360.security.JwtService;
import com.autocare360.service.VehicleService;

import lombok.RequiredArgsConstructor;
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

    @GetMapping
    public ResponseEntity<?> list(@RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = extractUserId(auth);
        if (userId == null) return ResponseEntity.status(401).build();
        List<Vehicle> list = vehicleService.listByUser(userId);
        return ResponseEntity.ok(Map.of("items", list, "total", list.size(), "page", 1, "perPage", list.size()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> get(@PathVariable Long id) {
        Vehicle v = vehicleService.get(id);
        if (v == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(v);
    }

    @PostMapping
    public ResponseEntity<Vehicle> create(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> body) {
        Long userId = extractUserId(auth);
        if (userId == null) return ResponseEntity.status(401).build();
        Vehicle v = new Vehicle();
        if (body.containsKey("make")) v.setMake((String) body.get("make"));
        if (body.containsKey("model")) v.setModel((String) body.get("model"));
        if (body.containsKey("year")) v.setYear((Integer) body.get("year"));
        if (body.containsKey("vin")) v.setVin((String) body.get("vin"));
        if (body.containsKey("plateNumber")) v.setPlateNumber((String) body.get("plateNumber"));
        if (body.containsKey("color")) v.setColor((String) body.get("color"));
        v.setUserId(userId);
        Vehicle created = vehicleService.create(v);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Vehicle patch = new Vehicle();
        if (body.containsKey("make")) patch.setMake((String) body.get("make"));
        if (body.containsKey("model")) patch.setModel((String) body.get("model"));
        if (body.containsKey("year")) patch.setYear((Integer) body.get("year"));
        if (body.containsKey("vin")) patch.setVin((String) body.get("vin"));
        if (body.containsKey("plateNumber")) patch.setPlateNumber((String) body.get("plateNumber"));
        if (body.containsKey("color")) patch.setColor((String) body.get("color"));
        Vehicle updated = vehicleService.update(id, patch);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
