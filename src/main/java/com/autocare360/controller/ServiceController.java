package com.autocare360.controller;

import com.autocare360.dto.ServiceRecordDTO;
import com.autocare360.entity.ServiceRecord;
import com.autocare360.security.JwtService;
import com.autocare360.service.ServiceRecordService;
import com.autocare360.service.VehicleService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

  private final ServiceRecordService serviceService;
  private final VehicleService vehicleService;
  private final JwtService jwtService;

  private ServiceRecordDTO mapToDTO(ServiceRecord s) {
    return new ServiceRecordDTO(
        s.getId(),
        s.getVehicleId(),
        s.getName(), // Service name
        s.getType(), // Category
        s.getStatus(),
        s.getRequestedAt(),
        s.getScheduledAt(),
        s.getNotes(),
        s.getAttachments(), // Now String instead of List<String>
        s.getPrice(),
        s.getDuration() // Automatically calculates durationMinutes
        );
  }

  @GetMapping
  public ResponseEntity<?> list(
      @RequestParam(required = false) String status,
      @RequestHeader(value = "Authorization", required = false) String auth) {

    boolean isAdmin = jwtService.hasRole(auth, "ADMIN");
    System.out.println("Is Admin: " + isAdmin);
    System.out.println("Status param: " + status);

    List<ServiceRecord> services;

    if (isAdmin) {
      System.out.println("User is ADMIN — fetching all services...");
      services = serviceService.listAll();
      System.out.println("Admin - fetched all services: " + services.size());
    } else if (status != null) {
      System.out.println("Non-admin user with status filter: " + status);
      services = serviceService.listByStatus(status);
      System.out.println("Non-admin - fetched by status (" + status + "): " + services.size());
    } else {
      System.out.println("Non-admin user with NO status filter — fetching all services...");
      services = serviceService.listAll();
      System.out.println("Non-admin - fetched all services (no filter): " + services.size());
    }

    List<ServiceRecordDTO> dtos = services.stream().map(this::mapToDTO).toList();

    return ResponseEntity.ok(
        Map.of("items", dtos, "total", dtos.size(), "page", 1, "perPage", dtos.size()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ServiceRecordDTO> get(@PathVariable Long id) {
    ServiceRecord s = serviceService.get(id);
    if (s == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(mapToDTO(s));
  }

  @PostMapping
  public ResponseEntity<ServiceRecordDTO> create(@RequestBody Map<String, Object> body) {
    if (!body.containsKey("vehicleId") || !body.containsKey("type")) {
      return ResponseEntity.badRequest().build();
    }

    Long vehicleId = Long.valueOf(String.valueOf(body.get("vehicleId")));
    var vehicle = vehicleService.get(vehicleId);
    if (vehicle == null) return ResponseEntity.badRequest().build();

    ServiceRecord s = new ServiceRecord();
    s.setVehicleId(vehicleId);
    s.setName((String) body.get("name"));
    s.setType((String) body.get("type"));
    if (body.containsKey("description")) s.setNotes((String) body.get("description"));
    if (body.containsKey("notes")) s.setNotes((String) body.get("notes"));
    if (body.containsKey("price")) {
      Object priceVal = body.get("price");
      if (priceVal instanceof Number) s.setPrice(((Number) priceVal).doubleValue());
    }
    if (body.containsKey("duration")) {
      Object durationVal = body.get("duration");
      if (durationVal instanceof Number) s.setDuration(((Number) durationVal).doubleValue());
    }

    if (body.containsKey("status")) s.setStatus((String) body.get("status"));

    ServiceRecord created = serviceService.create(s);
    return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(created));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ServiceRecordDTO> update(
      @PathVariable Long id, @RequestBody Map<String, Object> body) {
    ServiceRecord patch = new ServiceRecord();
    if (body.containsKey("name")) patch.setName((String) body.get("name"));
    if (body.containsKey("type")) patch.setType((String) body.get("type"));
    if (body.containsKey("status")) patch.setStatus((String) body.get("status"));
    if (body.containsKey("scheduledAt")) {
      // parsing skipped for brevity - add proper LocalDateTime parsing here
    }
    if (body.containsKey("notes")) patch.setNotes((String) body.get("notes"));
    if (body.containsKey("price")) {
      Object priceVal = body.get("price");
      if (priceVal instanceof Number) patch.setPrice(((Number) priceVal).doubleValue());
    }
    if (body.containsKey("duration")) {
      Object durationVal = body.get("duration");
      if (durationVal instanceof Number) patch.setDuration(((Number) durationVal).doubleValue());
    }

    ServiceRecord updated = serviceService.update(id, patch);
    if (updated == null) return ResponseEntity.notFound().build();
    return ResponseEntity.ok(mapToDTO(updated));
  }

  @GetMapping("/vehicle/{vehicleId}")
  public ResponseEntity<?> getServicesForVehicle(
      @PathVariable Long vehicleId, @RequestParam(required = false) String status) {

    List<ServiceRecord> services = serviceService.listForVehicle(vehicleId);

    if (status != null) {
      services = services.stream().filter(s -> status.equalsIgnoreCase(s.getStatus())).toList();
    }

    List<ServiceRecordDTO> dtos = services.stream().map(this::mapToDTO).toList();

    return ResponseEntity.ok(
        Map.of("items", dtos, "total", dtos.size(), "page", 1, "perPage", dtos.size()));
  }

  @GetMapping("/summary")
  public ResponseEntity<?> getServiceSummary(
      @RequestHeader(value = "Authorization", required = false) String auth) {
    // Debug: Log if admin
    boolean isAdmin = jwtService.hasRole(auth, "ADMIN");
    System.out.println("Is Admin: " + isAdmin);

    List<ServiceRecord> all = serviceService.listAll();
    System.out.println("Total services found: " + all.size());

    double avgPrice =
        all.stream()
            .filter(s -> s.getPrice() != null)
            .mapToDouble(ServiceRecord::getPrice)
            .average()
            .orElse(0.0);

    double avgDuration =
        all.stream()
            .filter(s -> s.getDuration() != null)
            .mapToDouble(ServiceRecord::getDuration)
            .average()
            .orElse(0.0);

    // Convert average duration from hours to minutes
    int avgDurationMinutes = (int) Math.round(avgDuration * 60);

    return ResponseEntity.ok(
        Map.of(
            "totalServices", all.size(),
            "averagePrice", avgPrice,
            "averageDuration", avgDuration, // hours
            "averageDurationMinutes", avgDurationMinutes // minutes
            ));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(
      @PathVariable Long id,
      @RequestHeader(value = "Authorization", required = false) String auth) {
    // Check if the user is admin
    if (!jwtService.hasRole(auth, "ADMIN")) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403
    }

    boolean deleted = serviceService.delete(id); // implement delete in service
    if (!deleted) {
      return ResponseEntity.notFound().build(); // 404 if not found
    }

    return ResponseEntity.noContent().build(); // 204 on success
  }
}
