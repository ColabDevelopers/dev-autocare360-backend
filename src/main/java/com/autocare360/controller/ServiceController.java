package com.autocare360.controller;

import java.util.List;
import java.util.Map;

import com.autocare360.dto.ServiceRecordDTO;
import com.autocare360.entity.ServiceRecord;
import com.autocare360.security.JwtService;
import com.autocare360.service.ServiceRecordService;
import com.autocare360.service.VehicleService;

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
                s.getType(),
                s.getStatus(),
                s.getRequestedAt(),
                s.getScheduledAt(),
                s.getNotes(),
                s.getAttachments(),
                s.getPrice(),      // new
                s.getDuration()
                );
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String status,
                                  @RequestHeader(value = "Authorization", required = false) String auth) {

        boolean isAdmin = jwtService.hasRole(auth, "ADMIN");
        List<ServiceRecord> services;

        if (isAdmin) {
            services = serviceService.listAll();
        } else if (status != null) {
            services = serviceService.listByStatus(status);
        } else {
            services = serviceService.listByStatus("requested");
        }

        List<ServiceRecordDTO> dtos = services.stream()
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
        s.setType((String) body.get("type"));
        if (body.containsKey("description")) s.setNotes((String) body.get("description"));

        ServiceRecord created = serviceService.create(s);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(created));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ServiceRecordDTO> update(@PathVariable Long id,
                                                   @RequestBody Map<String, Object> body) {
        ServiceRecord patch = new ServiceRecord();
        if (body.containsKey("status")) patch.setStatus((String) body.get("status"));
        if (body.containsKey("scheduledAt")) {
            // parsing skipped for brevity
        }
        if (body.containsKey("notes")) patch.setNotes((String) body.get("notes"));

        ServiceRecord updated = serviceService.update(id, patch);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(mapToDTO(updated));
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<?> getServicesForVehicle(@PathVariable Long vehicleId,
                                                   @RequestParam(required = false) String status) {

        List<ServiceRecord> services = serviceService.listForVehicle(vehicleId);

        if (status != null) {
            services = services.stream()
                    .filter(s -> status.equalsIgnoreCase(s.getStatus()))
                    .toList();
        }

        List<ServiceRecordDTO> dtos = services.stream()
                .map(this::mapToDTO)
                .toList();

        return ResponseEntity.ok(Map.of(
                "items", dtos,
                "total", dtos.size(),
                "page", 1,
                "perPage", dtos.size()
        ));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getServiceSummary() {
        List<ServiceRecord> all = serviceService.listAll();

        // You’ll only get averages if you have these fields in your entity
        double avgPrice = all.stream()
                .filter(s -> s.getPrice() != null)
                .mapToDouble(ServiceRecord::getPrice)
                .average()
                .orElse(0.0);

        double avgDuration = all.stream()
                .filter(s -> s.getDuration() != null)
                .mapToDouble(ServiceRecord::getDuration)
                .average()
                .orElse(0.0);

        return ResponseEntity.ok(Map.of(
                "totalServices", all.size(),
                "averagePrice", avgPrice,
                "averageDuration", avgDuration
        ));
    }
}
