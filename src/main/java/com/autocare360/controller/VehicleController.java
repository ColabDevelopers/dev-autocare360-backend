package com.autocare360.controller;

import com.autocare360.dto.VehicleCreateDTO;
import com.autocare360.dto.VehicleDTO;
import com.autocare360.entity.Vehicle;
import com.autocare360.security.JwtService;
import com.autocare360.service.VehicleService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class VehicleController {

  private final VehicleService vehicleService;
  private final JwtService jwtService;

  private Long extractUserId(String authorization) {
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      System.out.println("Missing or invalid Authorization header");
      return null;
    }
    String token = authorization.substring(7);

    // Check validity
    if (!jwtService.isTokenValid(token)) {
      System.out.println("Invalid token");
      return null;
    }

    String subject = jwtService.extractSubject(token);
    System.out.println("Extracted subject (userId): " + subject);
    return Long.valueOf(subject);
  }

  private VehicleDTO mapToDTO(Vehicle v) {
    return new VehicleDTO(
        v.getId(),
        v.getVin(),
        v.getMake(),
        v.getModel(),
        v.getYear(),
        v.getPlateNumber(),
        v.getColor());
  }

  @GetMapping
  public ResponseEntity<?> list(
      @RequestHeader(value = "Authorization", required = false) String auth) {
    Long userId = extractUserId(auth);
    if (userId == null) return ResponseEntity.status(401).build();

    boolean isAdmin = jwtService.hasRole(auth, "ADMIN");

    List<Vehicle> vehicles = isAdmin ? vehicleService.listAll() : vehicleService.listByUser(userId);

    List<VehicleDTO> dtos = vehicles.stream().map(this::mapToDTO).toList();

    return ResponseEntity.ok(
        Map.of("items", dtos, "total", dtos.size(), "page", 1, "perPage", dtos.size()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> get(
      @PathVariable Long id,
      @RequestHeader(value = "Authorization", required = false) String auth) {

    Long userId = extractUserId(auth);
    if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));

    Vehicle v = vehicleService.get(id);
    if (v == null) return ResponseEntity.notFound().build();

    boolean isAdmin = jwtService.hasRole(auth, "ADMIN");
    boolean isOwner = v.getUserId() != null && v.getUserId().equals(userId);

    if (!isAdmin && !isOwner) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

    return ResponseEntity.ok(mapToDTO(v));
  }

  @PostMapping
  public ResponseEntity<?> create(
      @RequestHeader(value = "Authorization", required = false) String auth,
      @Valid @RequestBody VehicleCreateDTO dto) {

    Long userId = extractUserId(auth);
    if (userId == null) return ResponseEntity.status(401).build();

    // Auto-generate VIN if not provided
    String vin = dto.getVin();
    if (vin == null || vin.trim().isEmpty()) {
      vin = "VIN-" + System.currentTimeMillis() + "-" + userId;
    }

    // prevent duplicate VIN for the same user
    if (vehicleService.existsByVinAndUserId(vin, userId)) {
      return ResponseEntity.status(HttpStatus.CONFLICT)
          .body(Map.of("error", "You already have a vehicle with this VIN"));
    }

    Vehicle v = new Vehicle();
    v.setVin(vin);
    v.setMake(dto.getMake());
    v.setModel(dto.getModel());
    v.setYear(dto.getYear());
    v.setPlateNumber(dto.getPlateNumber());
    v.setColor(dto.getColor());
    v.setUserId(userId);

    Vehicle created = vehicleService.create(v);
    return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(created));
  }

  @PutMapping("/{id}")
  public ResponseEntity<VehicleDTO> update(
      @PathVariable Long id, @RequestBody Map<String, Object> body) {
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
  public ResponseEntity<?> delete(
      @PathVariable Long id,
      @RequestHeader(value = "Authorization", required = false) String auth) {

    Long userId = extractUserId(auth);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
    }

    Vehicle v = vehicleService.get(id);
    if (v == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Vehicle not found"));
    }

    boolean isAdmin = jwtService.hasRole(auth, "ADMIN");
    boolean isOwner = v.getUserId() != null && v.getUserId().equals(userId);

    if (!isAdmin && !isOwner) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(Map.of("error", "You are not allowed to delete this vehicle"));
    }

    vehicleService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{id}/link")
  public ResponseEntity<?> linkVehicle(
      @PathVariable Long id,
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

  @GetMapping("/user/{userId}")
  public ResponseEntity<?> getVehiclesByUser(
      @PathVariable Long userId,
      @RequestHeader(value = "Authorization", required = false) String auth) {

    Long requestingUserId = extractUserId(auth);
    if (requestingUserId == null) return ResponseEntity.status(401).build();

    boolean isAdmin = jwtService.hasRole(auth, "ADMIN");
    if (!isAdmin) {
      return ResponseEntity.status(403).body(Map.of("error", "Admin access required"));
    }

    List<Vehicle> vehicles = vehicleService.listByUser(userId);
    List<VehicleDTO> dtos = vehicles.stream().map(this::mapToDTO).toList();

    return ResponseEntity.ok(Map.of("items", dtos, "total", dtos.size()));
  }
}
