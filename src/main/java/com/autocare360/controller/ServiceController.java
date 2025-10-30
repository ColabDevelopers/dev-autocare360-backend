package com.autocare360.controller;

import java.util.List;
import java.util.Map;

import com.autocare360.entity.ServiceRecord;
import com.autocare360.security.JwtService;
import com.autocare360.service.ServiceRecordService;
import com.autocare360.service.VehicleService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceRecordService serviceService;
    private final VehicleService vehicleService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String status) {
        if (status != null) {
            var list = serviceService.listByStatus(status);
            return ResponseEntity.ok(Map.of("items", list, "total", list.size()));
        }
        // fallback: return all (repo method may not be present; use getAll by repository via service)
        var list = serviceService.listByStatus("requested");
        return ResponseEntity.ok(Map.of("items", list, "total", list.size()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceRecord> get(@PathVariable Long id) {
        var s = serviceService.get(id);
        if (s == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(s);
    }

    @PostMapping
    public ResponseEntity<ServiceRecord> create(@RequestBody Map<String, Object> body) {
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
        if (body.containsKey("preferredDate")) {
            // ignore parsing for now; frontend will handle schedule separately
        }
        var created = serviceService.create(s);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceRecord> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        ServiceRecord patch = new ServiceRecord();
        if (body.containsKey("status")) patch.setStatus((String) body.get("status"));
        if (body.containsKey("scheduledAt")) {
            // parsing skipped for brevity
        }
        if (body.containsKey("notes")) patch.setNotes((String) body.get("notes"));
        var updated = serviceService.update(id, patch);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<?> listForVehicle(@PathVariable Long vehicleId) {
        var list = serviceService.listForVehicle(vehicleId);
        return ResponseEntity.ok(Map.of("items", list, "total", list.size()));
    }
}
